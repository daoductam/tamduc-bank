package com.tamduc.tamducbank.transaction.service;

import com.tamduc.tamducbank.res.Response;
import com.tamduc.tamducbank.transaction.dtos.TransactionDTO;
import com.tamduc.tamducbank.transaction.dtos.TransactionRequest;

import java.util.List;

public interface TransactionService {
    Response<?> createTransaction(TransactionRequest transactionRequest);

    Response<List<TransactionDTO>> getTransactionsForMyAccount(String accountNumber, int page, int size);


}
