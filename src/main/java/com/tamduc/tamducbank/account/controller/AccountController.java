package com.tamduc.tamducbank.account.controller;

import com.tamduc.tamducbank.account.service.AccountService;
import com.tamduc.tamducbank.auth_users.dtos.UpdatePasswordRequest;
import com.tamduc.tamducbank.auth_users.dtos.UserDTO;
import com.tamduc.tamducbank.auth_users.service.UserService;
import com.tamduc.tamducbank.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<Response<?>> getMyAccounts() {
        return ResponseEntity.ok(accountService.getMyAccounts());
    }

    @GetMapping("/close/{accountNumber}")
    public ResponseEntity<Response<?>> closeAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.closeAccount(accountNumber));
    }

}
