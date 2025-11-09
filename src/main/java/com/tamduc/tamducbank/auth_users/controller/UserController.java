package com.tamduc.tamducbank.auth_users.controller;

import com.tamduc.tamducbank.auth_users.dtos.*;
import com.tamduc.tamducbank.auth_users.service.AuthService;
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
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Response<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<Response<UserDTO>> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/update-password")
    public ResponseEntity<Response<?>> updatePassword(@RequestBody @Valid UpdatePasswordRequest request) {
        return ResponseEntity.ok(userService.updatePassword(request));
    }

    @PutMapping("/profile-picture")
    public ResponseEntity<Response<?>> uploadProfilePicture(@RequestParam("file")MultipartFile file) {
        return ResponseEntity.ok(userService.uploadProfilePictureToS3(file));
    }
}
