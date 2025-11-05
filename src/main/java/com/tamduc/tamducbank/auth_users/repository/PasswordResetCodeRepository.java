package com.tamduc.tamducbank.auth_users.repository;

import com.tamduc.tamducbank.auth_users.entity.PasswordResetCode;
import com.tamduc.tamducbank.auth_users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findByCode(String code);
    void deleteByUserId(Long userId);
}
