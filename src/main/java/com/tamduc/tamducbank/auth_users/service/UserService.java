package com.tamduc.tamducbank.auth_users.service;

import com.tamduc.tamducbank.auth_users.dtos.UpdatePasswordRequest;
import com.tamduc.tamducbank.auth_users.dtos.UserDTO;
import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.res.Response;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<UserDTO> getMyProfile();

    Response<Page<UserDTO>> getAllUsers(int page, int size);

    Response<?> updatePassword(UpdatePasswordRequest updatePasswordRequest);

    Response<?> uploadProfilePicture(MultipartFile file);

    Response<?> uploadProfilePictureToS3(MultipartFile file);

}
