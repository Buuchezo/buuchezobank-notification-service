package com.buuchezo.notificationservice.service.impl;

import com.buuchezo.notificationservice.entity.Notification;
import com.buuchezo.notificationservice.enums.NotificationStatus;
import com.buuchezo.notificationservice.enums.NotificationType;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;
import com.buuchezo.notificationservice.repository.NotificationRepository;
import com.buuchezo.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;


    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
    public void sendWelcomeEmail(UserRegistrationEvent event) {
        try {
            Context context = new Context();
            context.setVariable("firstName", event.getFirstName());
            context.setVariable("lastName", event.getLastName());
            context.setVariable("email", event.getEmail());
            context.setVariable("accountNumber", event.getAccountNumber());
            context.setVariable("bankName", event.getBankName());

            String htmlTemplate = templateEngine.process("welcome-email", context);

            var notificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .notificationType(NotificationType.EMAIL)
                    .subject("Welcome to " + event.getBankName() + " Your account is ready")
                    .message(htmlTemplate)
                    .status(NotificationStatus.SENT)
                    .transactionReference(null)
                    .build();


            //Send email out
            sendEmailOut(
                    event.getEmail(),
                    notificationToSave.getSubject(),
                    htmlTemplate
            );
            // save to the notification database
            notificationRepository.save(notificationToSave);

        } catch (Exception e) {
            log.error("Error sending welcome email", e);
            Notification notificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .notificationType(NotificationType.EMAIL)
                    .subject("Welcome to " + event.getBankName() + " Your account is ready")
                    .message("Failed to send email")
                    .status(NotificationStatus.FAILED)
                    .transactionReference(null)
                    .build();
            notificationRepository.save(notificationToSave);
            throw new RuntimeException("Error sending email out", e);
        }

    }

    private void sendEmailOut(String to, String subject, String htmlTemplate) {

        try {
            MimeMessage mailMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    null;
            try {
                helper = new MimeMessageHelper(mailMessage, true, "UTF-8");
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlTemplate, true);

            javaMailSender.send(mailMessage);

        } catch (MessagingException e) {
            log.error("Error sending email to {}", to, e);
        }
    }

    @Override
    public void sendCreditAlert() {

    }

    @Override
    public void sendDebitAlert() {

    }
}
