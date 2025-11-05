package com.tamduc.tamducbank.notification.service;

import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.notification.dtos.NotificationDTO;

public interface NotificationService {
    void sendEmail(NotificationDTO notificationDTO, User user);
}
