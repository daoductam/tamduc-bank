package com.tamduc.tamducbank;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class TamducbankApplication {
//	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(TamducbankApplication.class, args);
	}

//	@Bean
//	CommandLineRunner runner() {
//		return args -> {
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("tamdao1742005@gmail.com")
//					.subject("Hello testing email")
//					.body("Hey, this is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO, new User());
//		};
//	}

}
