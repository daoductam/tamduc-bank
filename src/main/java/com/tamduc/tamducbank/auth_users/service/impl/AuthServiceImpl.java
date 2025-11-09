package com.tamduc.tamducbank.auth_users.service.impl;

import com.tamduc.tamducbank.account.entity.Account;
import com.tamduc.tamducbank.account.service.AccountService;
import com.tamduc.tamducbank.auth_users.dtos.LoginRequest;
import com.tamduc.tamducbank.auth_users.dtos.LoginResponse;
import com.tamduc.tamducbank.auth_users.dtos.RegistrationRequest;
import com.tamduc.tamducbank.auth_users.dtos.ResetPasswordRequest;
import com.tamduc.tamducbank.auth_users.entity.PasswordResetCode;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.auth_users.repository.PasswordResetCodeRepository;
import com.tamduc.tamducbank.auth_users.repository.UserRepository;
import com.tamduc.tamducbank.auth_users.service.AuthService;
import com.tamduc.tamducbank.auth_users.service.CodeGenerator;
import com.tamduc.tamducbank.enums.AccountType;
import com.tamduc.tamducbank.enums.Currency;
import com.tamduc.tamducbank.exceptions.BadRequestException;
import com.tamduc.tamducbank.exceptions.NotFoundException;
import com.tamduc.tamducbank.notification.dtos.NotificationDTO;
import com.tamduc.tamducbank.notification.service.NotificationService;
import com.tamduc.tamducbank.res.Response;
import com.tamduc.tamducbank.role.entity.Role;
import com.tamduc.tamducbank.role.repository.RoleRepository;
import com.tamduc.tamducbank.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final NotificationService notificationService;
    private final AccountService accountService;

    private final CodeGenerator codeGenerator;
    private final PasswordResetCodeRepository passwordResetCodeRepository;

    @Value("${password.reset.link}")
    private String resetLink;

    @Override
    @Transactional
    public Response<String> register(RegistrationRequest request) {
        List<Role> roles;

        if (request.getRoles()==null || request.getRoles().isEmpty()) {

            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("CUSTOMER ROLE NOT FOUND"));

            roles = Collections.singletonList(defaultRole);
        } else {
            roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new NotFoundException("CUSTOMER ROLE NOT FOUND"+roleName)))
                    .toList();
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email Already Present");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        // gererate an accountnum for the user
        Account savedAccount = accountService.createAccount(AccountType.SAVINGS, savedUser);

        //send welcome email
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", savedUser.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Chào mừng tới Tâm Đức Bank")
                .templateName("welcome")
                .templateVariables(vars)
                .build();

        notificationService.sendEmail(notificationDTO, savedUser);


        //send account creation/detail email
        Map<String, Object> accountVars = new HashMap<>();
        accountVars.put("name", savedUser.getFirstName());
        accountVars.put("accountNumber", savedAccount.getAccountNumber());
        accountVars.put("accountType", AccountType.SAVINGS.name());
        accountVars.put("currency", Currency.VND);

        NotificationDTO accountCreatedEmail = NotificationDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Tài khoản ngân hàng mới của bạn đã được tạo")
                .templateName("account-created")
                .templateVariables(accountVars)
                .build();

        notificationService.sendEmail(accountCreatedEmail, savedUser);

        return Response.<String>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Your account has been created completely")
//                .data("Email of your account details has been sent to you. Your account number is: "+savedAccount.getAccountNumber())
                .build();
    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Password doesn't match");
        }

        String token = tokenService.generateToken(user.getEmail());

        LoginResponse loginResponse = LoginResponse.builder()
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .token(token)
                .build();

        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login successfully")
                .data(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> forgetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User Not Found"));
        passwordResetCodeRepository.deleteByUserId(user.getId());

        String code = codeGenerator.generateUniqueCode();

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .user(user)
                .code(code)
                .expiryDate(calculateExpiryDate())
                .used(false)
                .build();

        passwordResetCodeRepository.save(resetCode);

        //send email reset link out
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name",user.getFirstName());
        templateVariables.put("resetLink", resetLink + code);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Mã reset mật khẩu")
                .templateName("password-reset")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password reset code sent to your email")
                .build();

    }

    private LocalDateTime calculateExpiryDate() {

        return LocalDateTime.now().plusHours(5);
    }

    @Override
    @Transactional
    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {
        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        //Find and validate code
        PasswordResetCode resetCode = passwordResetCodeRepository.findByCode(code)
                .orElseThrow(() -> new BadRequestException("Invalid reset code"));

        // check expiration first
        if (resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepository.delete(resetCode);
            throw new BadRequestException("Reset code has expired");
        }

        //update the password
        User user = resetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        //delete the code immediately after successful use
        passwordResetCodeRepository.delete(resetCode);

        //send confirmation email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name",user.getFirstName());

        NotificationDTO confirmationEmail = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Mật Khẩu đã cập nhật thành công")
                .templateName("password-update-confirmation")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(confirmationEmail, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Mật Khẩu đã cập nhật thành công")
                .build();
    }
}
