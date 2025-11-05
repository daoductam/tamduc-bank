package com.tamduc.tamducbank.auth_users.service;

import com.tamduc.tamducbank.auth_users.dtos.LoginRequest;
import com.tamduc.tamducbank.auth_users.dtos.LoginResponse;
import com.tamduc.tamducbank.auth_users.dtos.RegistrationRequest;
import com.tamduc.tamducbank.auth_users.dtos.ResetPasswordRequest;
import com.tamduc.tamducbank.res.Response;

public interface AuthService {
    Response<String> register(RegistrationRequest request);
    Response<LoginResponse> login(LoginRequest loginRequest);
    Response<?> forgetPassword(String email);
    Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest);
}
