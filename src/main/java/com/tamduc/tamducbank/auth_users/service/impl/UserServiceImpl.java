package com.tamduc.tamducbank.auth_users.service.impl;

import com.tamduc.tamducbank.auth_users.dtos.UpdatePasswordRequest;
import com.tamduc.tamducbank.auth_users.dtos.UserDTO;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.auth_users.repository.UserRepository;
import com.tamduc.tamducbank.auth_users.service.UserService;
import com.tamduc.tamducbank.aws.S3Service;
import com.tamduc.tamducbank.exceptions.BadRequestException;
import com.tamduc.tamducbank.exceptions.NotFoundException;
import com.tamduc.tamducbank.notification.dtos.NotificationDTO;
import com.tamduc.tamducbank.notification.service.NotificationService;
import com.tamduc.tamducbank.res.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private  final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;

//    private final String uploadDir = "uploads/profile-pictures/";

    private final String uploadDir = "d/phegon-bank-react/public/profile-picture/";


    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new NotFoundException("User is not authenticated");
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public Response<UserDTO> getMyProfile() {
        User user = getCurrentLoggedInUser();
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("User retrieved")
                .data(userDTO)
                .build();
    }

    @Override
    public Response<Page<UserDTO>> getAllUsers(int page, int size) {
        Page<User> users = userRepository.findAll(PageRequest.of(page, size));

        Page<UserDTO> userDTOS = users.map(user -> modelMapper.map(user, UserDTO.class));

        return Response.<Page<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users retrieved")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentLoggedInUser();

        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();

        if (oldPassword == null || newPassword == null) {
            throw new BadRequestException("Old and New password required");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old Password not correct");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        //send welcome email
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("name", user.getFirstName());

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Mật khẩu của bạn đã thay đổi thành công")
                .templateName("password-change")
                .templateVariables(templateVariables)
                .build();

        notificationService.sendEmail(notificationDTO, user);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Password Changed Successfully")
                .build();
    }

    @Override
    public Response<?> uploadProfilePicture(MultipartFile file) {
        User user = getCurrentLoggedInUser();

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                Path oldPath = Paths.get(user.getProfilePictureUrl());
                if (Files.exists(oldPath)) {
                    Files.delete(oldPath);
                }
            }

            //Generate a unique file name to avoid conflicts
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + fileExtension;
            Path filePath = uploadPath.resolve(newFileName);

            Files.copy(file.getInputStream(), filePath);

            String fileUrl = "profile-picture/"+newFileName;
//            String fileUrl = uploadDir + newFileName;

            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile picture uploaded successfully.")
                    .data(file)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Response<?> uploadProfilePictureToS3(MultipartFile file) {
        User user = getCurrentLoggedInUser();

        try {
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                s3Service.deleteFile(user.getProfilePictureUrl());
            }
            String s3Url = s3Service.uploadFile(file, "profile-pictures");

            user.setProfilePictureUrl(s3Url);
            userRepository.save(user);

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Profile Picture uploaded successfully")
                    .data(s3Url)
                    .build();
        }catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
