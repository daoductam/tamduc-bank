package com.tamduc.tamducbank.audit_dashboard.service;


import com.tamduc.tamducbank.account.dtos.AccountDTO;
import com.tamduc.tamducbank.account.repository.AccountRepository;
import com.tamduc.tamducbank.auth_users.dtos.UserDTO;
import com.tamduc.tamducbank.auth_users.repository.UserRepository;
import com.tamduc.tamducbank.transaction.dtos.TransactionDTO;
import com.tamduc.tamducbank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditorServiceImpl implements AuditorService{

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;

    @Override
    public Map<String, Long> getSystemTotals() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        return Map.of(
                "totalUsers",totalUsers,
                "totalAccounts", totalAccounts,
                "totalTransactions", totalTransactions
        );
    }

    @Override
    public Optional<UserDTO> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public Optional<AccountDTO> findAccountDetailsByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> modelMapper.map(account, AccountDTO.class));
    }

    @Override
    public List<TransactionDTO> findTransactionsByAccountNumber(String accountNumber) {
        return transactionRepository.findByAccount_AccountNumber(accountNumber).stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TransactionDTO> findTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class));
    }
}
