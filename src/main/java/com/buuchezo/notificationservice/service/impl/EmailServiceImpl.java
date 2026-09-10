package com.buuchezo.notificationservice.service.impl;

import com.buuchezo.notificationservice.entity.Notification;
import com.buuchezo.notificationservice.enums.NotificationStatus;
import com.buuchezo.notificationservice.enums.NotificationType;
import com.buuchezo.notificationservice.enums.transaction.TransactionDirection;
import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
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

import java.time.LocalDateTime;

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
            Notification errorNotificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .notificationType(NotificationType.EMAIL)
                    .subject("Welcome to " + event.getBankName() + " Your account is ready")
                    .message("Failed to send email")
                    .status(NotificationStatus.FAILED)
                    .transactionReference(null)
                    .build();
            notificationRepository.save(errorNotificationToSave);
            throw new RuntimeException("Error sending email out", e);
        }

    }

    @Override
    public void sendTransactionAlertEmail(BalanceUpdateEvent event) {
        try {
            Context context = new Context();
            context.setVariable("name",event.getFirstName());
            context.setVariable("bankName","BUUCHEZO BANK");
            context.setVariable("amount",event.getAmount());
            context.setVariable("currency",event.getCurrency());
            context.setVariable("reference",event.getReference());
            context.setVariable("accountNumber",event.getAccountNumber());
            context.setVariable("description",event.getDescription()
                    != null ? event.getDescription() : "Bank Transaction");
            context.setVariable("date", LocalDateTime.now().toString());
            context.setVariable("balance",event.getCurrentBalance());

            String templateName;
            String subject;

            if(event.getTransactionDirection().equals(TransactionDirection.CREDIT)){
                templateName = "credit-alert";
                subject = "Credit Alert: ["+event.getReference()+"]";
            }else {
                templateName = "debit-alert";
                subject = "Debit Alert: ["+event.getReference()+"]";
            }

            String htmlEmailTemplate = templateEngine.process(templateName, context);
            Notification notification = Notification
                    .builder()
                    .recipientEmail(event.getEmail())
                    .notificationType(NotificationType.EMAIL)
                    .subject(subject)
                    .message(htmlEmailTemplate)
                    .status(NotificationStatus.SENT)
                    .build();
            // Send email first before saving

            sendEmailOut(event.getEmail(), subject, htmlEmailTemplate);

            // save to database
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Error sending welcome email", e);
            Notification errorNotificationToSave = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .notificationType(NotificationType.EMAIL)
                    .subject("Transaction Alert Error ")
                    .message("Failed to send transaction email for this reference: {}" + event.getReference())
                    .status(NotificationStatus.FAILED)
                    .transactionReference(null)
                    .build();
            notificationRepository.save(errorNotificationToSave);
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


}
