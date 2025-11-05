package com.tamduc.tamducbank.notification.repository;

import com.tamduc.tamducbank.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
