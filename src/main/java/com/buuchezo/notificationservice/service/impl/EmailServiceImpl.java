package com.buuchezo.notificationservice.service.impl;

import com.buuchezo.notificationservice.entity.Notification;
import com.buuchezo.notificationservice.enums.NotificationChannel;
import com.buuchezo.notificationservice.enums.NotificationStatus;
import com.buuchezo.notificationservice.enums.NotificationType;
import com.buuchezo.notificationservice.enums.transaction.TransactionDirection;
import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.TanNotificationEvent;
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
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(UserRegistrationEvent event) {

        String title =
                "Welcome to " + event.getBankName() + " — Your account is ready";

        try {
            Context context = new Context();

            context.setVariable(
                    "firstName",
                    event.getFirstName()
            );

            context.setVariable(
                    "lastName",
                    event.getLastName()
            );

            context.setVariable(
                    "email",
                    event.getEmail()
            );

            context.setVariable(
                    "accountNumber",
                    event.getAccountNumber()
            );

            context.setVariable(
                    "bankName",
                    event.getBankName()
            );

            String htmlTemplate =
                    templateEngine.process(
                            "welcome-email",
                            context
                    );

            sendEmailOut(
                    event.getEmail(),
                    title,
                    htmlTemplate
            );

            Notification notification =
                    Notification.builder()
                            .recipientEmail(event.getEmail())
                            .notificationType(NotificationType.ACCOUNT)
                            .channel(NotificationChannel.EMAIL)
                            .title(title)
                            .message(htmlTemplate)
                            .status(NotificationStatus.SENT)
                            .read(true)
                            .transactionReference(null)
                            .createdAt(LocalDateTime.now())
                            .build();

            notificationRepository.save(notification);

            log.info(
                    "Welcome email sent successfully to {}",
                    event.getEmail()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send welcome email to {}",
                    event.getEmail(),
                    e
            );

            Notification errorNotification =
                    Notification.builder()
                            .recipientEmail(event.getEmail())
                            .notificationType(NotificationType.ACCOUNT)
                            .channel(NotificationChannel.EMAIL)
                            .title(title)
                            .message(
                                    "Failed to send welcome email: "
                                            + e.getMessage()
                            )
                            .status(NotificationStatus.FAILED)
                            .read(true)
                            .transactionReference(null)
                            .createdAt(LocalDateTime.now())
                            .build();

            notificationRepository.save(errorNotification);

            throw new RuntimeException(
                    "Error sending welcome email",
                    e
            );
        }
    }

    @Override
    public void sendTransactionAlertEmail(BalanceUpdateEvent event) {

        String reference = event.getReference();

        String title;

        if (event.getTransactionDirection()
                == TransactionDirection.CREDIT) {

            title =
                    "Credit Alert: ["
                            + reference
                            + "]";

        } else {

            title =
                    "Debit Alert: ["
                            + reference
                            + "]";
        }

        try {

            Context context = new Context();

            context.setVariable(
                    "name",
                    event.getFirstName()
            );

            context.setVariable(
                    "bankName",
                    "Buuchezo Bank"
            );

            context.setVariable(
                    "amount",
                    event.getAmount()
            );

            context.setVariable(
                    "currency",
                    event.getCurrency()
            );

            context.setVariable(
                    "reference",
                    event.getReference()
            );

            context.setVariable(
                    "accountNumber",
                    event.getAccountNumber()
            );

            context.setVariable(
                    "description",
                    event.getDescription() != null
                            ? event.getDescription()
                            : "Bank Transaction"
            );

            context.setVariable(
                    "date",
                    LocalDateTime.now()
            );

            context.setVariable(
                    "balance",
                    event.getCurrentBalance()
            );

            String templateName;

            if (event.getTransactionDirection()
                    == TransactionDirection.CREDIT) {

                templateName = "credit-alert";

            } else {

                templateName = "debit-alert";
            }

            String htmlEmailTemplate =
                    templateEngine.process(
                            templateName,
                            context
                    );

            sendEmailOut(
                    event.getEmail(),
                    title,
                    htmlEmailTemplate
            );

            Notification notification =
                    Notification.builder()
                            .recipientEmail(event.getEmail())
                            .notificationType(NotificationType.TRANSACTION)
                            .channel(NotificationChannel.EMAIL)
                            .title(title)
                            .message(htmlEmailTemplate)
                            .status(NotificationStatus.SENT)
                            .read(true)
                            .transactionReference(reference)
                            .createdAt(LocalDateTime.now())
                            .build();

            notificationRepository.save(notification);

            log.info(
                    "Transaction email sent successfully. Recipient: {}, Reference: {}",
                    event.getEmail(),
                    reference
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send transaction email. Recipient: {}, Reference: {}",
                    event.getEmail(),
                    reference,
                    e
            );

            Notification errorNotification =
                    Notification.builder()
                            .recipientEmail(event.getEmail())
                            .notificationType(NotificationType.TRANSACTION)
                            .channel(NotificationChannel.EMAIL)
                            .title(title)
                            .message(
                                    "Failed to send transaction email for reference: "
                                            + reference
                                            + ". Error: "
                                            + e.getMessage()
                            )
                            .status(NotificationStatus.FAILED)
                            .read(true)
                            .transactionReference(reference)
                            .createdAt(LocalDateTime.now())
                            .build();

            notificationRepository.save(errorNotification);

            throw new RuntimeException(
                    "Error sending transaction email",
                    e
            );
        }
    }

    private void sendEmailOut(
            String to,
            String subject,
            String htmlTemplate
    ) {

        try {

            MimeMessage mailMessage =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mailMessage,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(
                    htmlTemplate,
                    true
            );

            javaMailSender.send(mailMessage);

        } catch (MessagingException e) {

            log.error(
                    "Error creating email message for {}",
                    to,
                    e
            );

            throw new RuntimeException(
                    "Could not create email message",
                    e
            );

        } catch (Exception e) {

            log.error(
                    "Error sending email to {}",
                    to,
                    e
            );

            throw new RuntimeException(
                    "Could not send email",
                    e
            );
        }
    }

    @Override
    public void sendTanEmail(TanNotificationEvent event) {

        try {

            Context context = new Context();

            context.setVariable(
                    "name",
                    "Customer"
            );

            context.setVariable(
                    "bankName",
                    "BUUCHEZO BANK"
            );

            context.setVariable(
                    "tan",
                    event.getTan()
            );

            context.setVariable(
                    "operation",
                    event.getOperation()
            );

            context.setVariable(
                    "expiresAt",
                    event.getExpiresAt()
                            .format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "dd MMM yyyy, HH:mm"
                                    )
                            )
            );

            String htmlEmailTemplate =
                    templateEngine.process(
                            "tan-authorization",
                            context
                    );

            String subject =
                    "Transaction Authorization Required";

            sendEmailOut(
                    event.getUserEmail(),
                    subject,
                    htmlEmailTemplate
            );

            log.info(
                    "TAN email sent successfully. Recipient: {}, challengeId={}",
                    event.getUserEmail(),
                    event.getChallengeId()
            );

        } catch (Exception e) {

            /*
             * Never log the actual TAN.
             */

            log.error(
                    "Error sending TAN email. Recipient={}, challengeId={}",
                    event.getUserEmail(),
                    event.getChallengeId(),
                    e
            );

            throw new RuntimeException(
                    "Error sending TAN email",
                    e
            );
        }
    }
}