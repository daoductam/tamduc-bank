package com.tamduc.tamducbank.auth_users.controller;

import com.tamduc.tamducbank.auth_users.dtos.LoginRequest;
import com.tamduc.tamducbank.auth_users.dtos.LoginResponse;
import com.tamduc.tamducbank.auth_users.dtos.RegistrationRequest;
import com.tamduc.tamducbank.auth_users.dtos.ResetPasswordRequest;
import com.tamduc.tamducbank.auth_users.service.AuthService;
import com.tamduc.tamducbank.res.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<String>> createRole(@RequestBody @Valid RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> createRole(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<Response<?>> forgotPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.forgetPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response<?>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.updatePasswordViaResetCode(request));
    }
}
