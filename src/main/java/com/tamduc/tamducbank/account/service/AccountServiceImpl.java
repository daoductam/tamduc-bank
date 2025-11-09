package com.tamduc.tamducbank.account.service;

import com.tamduc.tamducbank.account.dtos.AccountDTO;
import com.tamduc.tamducbank.account.entity.Account;
import com.tamduc.tamducbank.account.repository.AccountRepository;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.auth_users.service.UserService;
import com.tamduc.tamducbank.enums.AccountStatus;
import com.tamduc.tamducbank.enums.AccountType;
import com.tamduc.tamducbank.enums.Currency;
import com.tamduc.tamducbank.exceptions.BadRequestException;
import com.tamduc.tamducbank.exceptions.NotFoundException;
import com.tamduc.tamducbank.res.Response;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    private final Random random = new Random();

    @Override
    public Account createAccount(AccountType accountType, User user) {
        log.info("Inside createAccount()");
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .currency(Currency.VND)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "66" + (random.nextInt(90000000)+10000000);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        log.info("account number generate {}",accountRepository);
        return accountNumber;
    }

    @Override
    public Response<List<AccountDTO>> getMyAccounts() {
        User user  = userService.getCurrentLoggedInUser();

        List<AccountDTO> accounts = accountRepository.findByUserId(user.getId())
                .stream()
                .map(account -> modelMapper.map(account, AccountDTO.class))
                .toList();

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User accounts fetched successfully")
                .data(accounts)
                .build();
    }

    @Override
    public Response<?> closeAccount(String accountNumber) {
        User user = userService.getCurrentLoggedInUser();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!user.getAccounts().contains(account)) {
            throw new NotFoundException("Account doesn't belong to you");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Account balance must be zero before closing");
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(LocalDateTime.now());
        accountRepository.save(account);

        return Response.<List<AccountDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account closed successfully")
                .build();
    }
}
