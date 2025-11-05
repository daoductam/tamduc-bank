package com.tamduc.tamducbank.auth_users.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.tamduc.tamducbank.role.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RegistrationRequest {
    @NotBlank(message = "FirstName is required")
    private String firstName;

    private String lastName;

    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private List<String> roles;

    @NotBlank(message = "Password is required")
    private String password;

}
