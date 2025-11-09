package com.tamduc.tamducbank.notification.service;

import com.tamduc.tamducbank.auth_users.entity.User;
import com.tamduc.tamducbank.enums.NotificationType;
import com.tamduc.tamducbank.notification.dtos.NotificationDTO;
import com.tamduc.tamducbank.notification.entity.Notification;
import com.tamduc.tamducbank.notification.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO, User user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(notificationDTO.getRecipient());
            helper.setSubject(notificationDTO.getSubject());

            //use template if provided
            if (notificationDTO.getTemplateName() != null) {
                Context context = new Context();
                context.setVariables(notificationDTO.getTemplateVariables());
                String htmlContext = templateEngine.process(notificationDTO.getTemplateName(), context);
                helper.setText(htmlContext, true);
            } else {
                // if no template send text body directly
                helper.setText(notificationDTO.getBody(), true);
            }

            mailSender.send(mimeMessage);

            // save to our db
            Notification notificationToSave = Notification.builder()
                    .recipient(notificationDTO.getRecipient())
                    .subject(notificationDTO.getSubject())
                    .body(notificationDTO.getBody())
                    .type(NotificationType.EMAIL)
                    .user(user)
                    .build();

            notificationRepository.save(notificationToSave);

        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }
}
