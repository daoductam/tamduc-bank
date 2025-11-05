package com.tamduc.tamducbank.account.service;

import com.tamduc.tamducbank.account.dtos.AccountDTO;
import com.tamduc.tamducbank.account.entity.Account;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.enums.AccountType;
import com.tamduc.tamducbank.res.Response;

import java.util.List;

public interface AccountService {
    Account createAccount(AccountType accountType, User user);
    Response<List<AccountDTO>> getMyAccounts();

    Response<?> closeAccount(String accountNumber);
}
