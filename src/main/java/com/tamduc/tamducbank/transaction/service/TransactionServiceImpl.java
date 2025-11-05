package com.tamduc.tamducbank.transaction.service;

import com.tamduc.tamducbank.account.entity.Account;
import com.tamduc.tamducbank.account.repository.AccountRepository;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.auth_users.service.UserService;
import com.tamduc.tamducbank.enums.TransactionStatus;
import com.tamduc.tamducbank.enums.TransactionType;
import com.tamduc.tamducbank.exceptions.BadRequestException;
import com.tamduc.tamducbank.exceptions.InsufficientBalanceException;
import com.tamduc.tamducbank.exceptions.InvalidTransactionException;
import com.tamduc.tamducbank.exceptions.NotFoundException;
import com.tamduc.tamducbank.notification.dtos.NotificationDTO;
import com.tamduc.tamducbank.notification.service.NotificationService;
import com.tamduc.tamducbank.res.Response;
import com.tamduc.tamducbank.transaction.dtos.TransactionDTO;
import com.tamduc.tamducbank.transaction.dtos.TransactionRequest;
import com.tamduc.tamducbank.transaction.entity.Transaction;
import com.tamduc.tamducbank.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService{

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public Response<?> createTransaction(TransactionRequest transactionRequest) {

        Transaction transaction = new Transaction();
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setDescription(transactionRequest.getDescription());

        switch (transactionRequest.getTransactionType()) {
            case DEPOSIT -> handleDeposit(transactionRequest, transaction);
            case WITHDRAWAL -> handleWithdrawal(transactionRequest, transaction);
            case TRANSFER -> handleTransfer(transactionRequest, transaction);
            default -> throw new InvalidTransactionException("Invalid transaction type");
        }

        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction savedTxn = transactionRepository.save(transaction);

        //send notification
        sendTransactionNotifications(savedTxn);

        return Response.builder()
                .statusCode(200)
                .message("Transaction successful")
                .build();
    }

    private void handleDeposit(TransactionRequest request, Transaction transaction) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));
        transaction.setAccount(account);
        accountRepository.save(account);
    }

    private void handleWithdrawal(TransactionRequest request, Transaction transaction) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        transaction.setAccount(account);
        accountRepository.save(account);
    }

    private void handleTransfer(TransactionRequest request, Transaction transaction) {
        Account sourceAccount = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Account destination = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElseThrow(() -> new NotFoundException("Destination Account not found"));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) <0) {
            throw new InsufficientBalanceException("Insufficient balance in source account");
        }

        //Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(sourceAccount);

        // Add to destication
        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepository.save(destination);

        transaction.setAccount(sourceAccount);
        transaction.setSourceAccount(sourceAccount.getAccountNumber());
        transaction.setDestinationAccount(destination.getAccountNumber());
    }

    private void sendTransactionNotifications(Transaction tnx) {
        User user = tnx.getAccount().getUser();
        String subject;
        String template;

        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirsName());
        templateVariables.put("amount", tnx.getAmount());
        templateVariables.put("accountNumber", tnx.getAccount().getAccountNumber());
        templateVariables.put("date", tnx.getTransactionDate());
        templateVariables.put("balance", tnx.getAccount().getBalance());

        if (tnx.getTransactionType() == TransactionType.DEPOSIT) {
            subject = "Credit Alert";
            template = "credit-alert";

            NotificationDTO notificationEmailToSendOut= NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();

            notificationService.sendEmail(notificationEmailToSendOut, user);
        } else if (tnx.getTransactionType() == TransactionType.WITHDRAWAL) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO notificationEmailToSendOut= NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();

            notificationService.sendEmail(notificationEmailToSendOut, user);
        } else if (tnx.getTransactionType() == TransactionType.TRANSFER) {
            subject = "Debit Alert";
            template = "debit-alert";

            NotificationDTO notificationEmailToSendOut= NotificationDTO.builder()
                    .recipient(user.getEmail())
                    .subject(subject)
                    .templateName(template)
                    .templateVariables(templateVariables)
                    .build();

            notificationService.sendEmail(notificationEmailToSendOut, user);

            //Receiver CREDIT alert
            Account destination = accountRepository.findByAccountNumber(tnx.getDestinationAccount())
                    .orElseThrow(() -> new NotFoundException("Destination account not found"));

            User reciever = destination.getUser();
            Map<String, Object> recvVars = new HashMap<>();
            recvVars.put("name", reciever.getFirsName());
            recvVars.put("amount", tnx.getAmount());
            recvVars.put("accountNumber", destination.getAccountNumber());
            recvVars.put("date", tnx.getTransactionDate());
            recvVars.put("balance", destination.getBalance());

            NotificationDTO notificationEmailToSendOutToReceiver= NotificationDTO.builder()
                    .recipient(reciever.getEmail())
                    .subject("Credit Alert")
                    .templateName("credit-alert")
                    .templateVariables(recvVars)
                    .build();

            notificationService.sendEmail(notificationEmailToSendOutToReceiver, reciever);

        }

    }

    @Override
    @Transactional
    public Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber, int page, int size) {

        //get the currently logged-in user
        User user = userService.getCurrentLoggedInUser();

        //Find the account by its number
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account Not Found"));

        //make sure he account belong to the user. an extra security check
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Account does not belong to the authenticated user");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<Transaction> txns = transactionRepository.findByAccount_AccountNumber(accountNumber, pageable);

        List<TransactionDTO> transactionDTOS = txns.getContent().stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();

        return Response.<List<TransactionDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transaction retrieved")
                .data(transactionDTOS)
                .meta(Map.of(
                        "currentPage", txns.getNumber(),
                        "total",txns.getTotalElements(),
                        "totalPages", txns.getTotalPages(),
                        "pageSize",txns.getSize()
                ))
                .build();
    }
}
