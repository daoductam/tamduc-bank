package com.tamduc.tamducbank.transaction.repository;

import com.tamduc.tamducbank.account.entity.Account;
import com.tamduc.tamducbank.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByAccount_AccountNumber(String accountNumber, Pageable pageable);

    List<Transaction> findByAccount_AccountNumber(String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber OR t.destinationAccount = :accountNumber")
    Page<Transaction> findAllTransactionsForAccount(
            @Param("accountNumber") String accountNumber,
            Pageable pageable
    );
}
