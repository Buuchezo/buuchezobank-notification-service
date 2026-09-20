package com.buuchezo.notificationservice.service.impl;

import com.buuchezo.notificationservice.dto.NotificationDto;
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
import com.buuchezo.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final EmailService emailService;

    @Override
    @Transactional
    public void processUserRegistration(
            UserRegistrationEvent event
    ) {

        log.info(
                "Processing user registration notification for {}",
                event.getEmail()
        );

        Notification notification =
                Notification.builder()
                        .recipientEmail(event.getEmail())
                        .notificationType(
                                NotificationType.ACCOUNT
                        )
                        .channel(
                                NotificationChannel.IN_APP
                        )
                        .title(
                                "Welcome to Buuchezo Bank"
                        )
                        .message(
                                "Welcome "
                                        + event.getFirstName()
                                        + "! Your Buuchezo Bank account "
                                        + event.getAccountNumber()
                                        + " has been successfully created."
                        )
                        .status(
                                NotificationStatus.SENT
                        )
                        .read(false)
                        .transactionReference(null)
                        .build();

        notificationRepository.save(
                notification
        );

        log.info(
                "In-app welcome notification saved for {}",
                event.getEmail()
        );

        emailService.sendWelcomeEmail(
                event
        );
    }

    @Override
    @Transactional
    public void processBalanceUpdate(
            BalanceUpdateEvent event
    ) {

        log.info(
                "Processing transaction notification for account {}",
                event.getAccountNumber()
        );

        String title =
                buildTransactionTitle(
                        event
                );

        String message =
                buildTransactionMessage(
                        event
                );

        Notification notification =
                Notification.builder()
                        .recipientEmail(
                                event.getEmail()
                        )
                        .notificationType(
                                NotificationType.TRANSACTION
                        )
                        .channel(
                                NotificationChannel.IN_APP
                        )
                        .title(title)
                        .message(message)
                        .status(
                                NotificationStatus.SENT
                        )
                        .read(false)
                        .transactionReference(
                                event.getReference()
                        )
                        .build();

        notificationRepository.save(
                notification
        );

        log.info(
                "In-app transaction notification saved. Reference: {}",
                event.getReference()
        );

        emailService.sendTransactionAlertEmail(
                event
        );
    }

    /**
     * Handles TAN notifications.
     * <p>
     * The TAN itself is deliberately NOT written
     * to the application log.
     */
    @Override
    @Transactional
    public void processTanNotification(
            TanNotificationEvent event
    ) {

        log.info(
                "Processing TAN notification. challengeId={}, user={}, operation={}",
                event.getChallengeId(),
                event.getUserEmail(),
                event.getOperation()
        );

        long expiresInSeconds =
                calculateRemainingSeconds(
                        event.getExpiresAt()
                );

        String title =
                "Buuchezo Bank TAN";

        String message =
                buildTanMessage(
                        event,
                        expiresInSeconds
                );

        Notification notification =
                Notification.builder()
                        .recipientEmail(
                                event.getUserEmail()
                        )
                        .notificationType(
                                NotificationType.SECURITY
                        )
                        .channel(
                                NotificationChannel.IN_APP
                        )
                        .title(title)
                        .message(message)
                        .status(
                                NotificationStatus.SENT
                        )
                        .read(false)
                        .transactionReference(
                                event.getChallengeId()
                        )
                        .build();

        notificationRepository.save(
                notification
        );

        log.info(
                "In-app TAN notification saved. challengeId={}",
                event.getChallengeId()
        );

        emailService.sendTanEmail(
                event
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(
            String email
    ) {

        log.info(
                "Retrieving notifications for {}",
                email
        );

        return notificationRepository
                .findByRecipientEmailAndChannelOrderByCreatedAtDesc(
                        email,
                        NotificationChannel.IN_APP
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(
            String email
    ) {

        log.info(
                "Retrieving unread notifications for {}",
                email
        );

        return notificationRepository
                .findByRecipientEmailAndChannelAndReadFalseOrderByCreatedAtDesc(
                        email,
                        NotificationChannel.IN_APP
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(
            Long notificationId,
            String email
    ) {

        Notification notification =
                notificationRepository
                        .findByIdAndRecipientEmailAndChannel(
                                notificationId,
                                email,
                                NotificationChannel.IN_APP
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Notification not found"
                                        )
                        );

        if (!notification.isRead()) {

            notification.setRead(true);

            notification =
                    notificationRepository.save(
                            notification
                    );

            log.info(
                    "Notification {} marked as read for {}",
                    notificationId,
                    email
            );
        }

        return mapToDto(
                notification
        );
    }

    @Override
    @Transactional
    public int markAllAsRead(
            String email
    ) {

        List<Notification> notifications =
                notificationRepository
                        .findByRecipientEmailAndChannelAndReadFalseOrderByCreatedAtDesc(
                                email,
                                NotificationChannel.IN_APP
                        );

        if (notifications.isEmpty()) {

            log.info(
                    "No unread notifications found for {}",
                    email
            );

            return 0;
        }

        notifications.forEach(
                notification ->
                        notification.setRead(true)
        );

        notificationRepository.saveAll(
                notifications
        );

        log.info(
                "Marked {} notifications as read for {}",
                notifications.size(),
                email
        );

        return notifications.size();
    }

    private NotificationDto mapToDto(
            Notification notification
    ) {

        return NotificationDto.builder()
                .id(
                        notification.getId()
                )
                .notificationType(
                        notification.getNotificationType()
                )
                .channel(
                        notification.getChannel()
                )
                .title(
                        notification.getTitle()
                )
                .message(
                        notification.getMessage()
                )
                .status(
                        notification.getStatus()
                )
                .read(
                        notification.isRead()
                )
                .transactionReference(
                        notification.getTransactionReference()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }

    private String buildTransactionTitle(
            BalanceUpdateEvent event
    ) {

        /*
         * Defensive handling.
         *
         * Some older Kafka events may not contain
         * transactionType. This prevents the NPE
         * you previously encountered.
         */

        if (event.getTransactionDirection()
                == TransactionDirection.CREDIT) {

            if (event.getTransactionType() == null) {
                return "Money added to your account";
            }

            return switch (
                    event.getTransactionType()
                    ) {

                case DEPOSIT -> "Money deposited";

                case TRANSFER -> "Money received";

                case PAYMENT -> "Payment received";

                default -> "Money added to your account";
            };
        }

        if (event.getTransactionType() == null) {
            return "Money withdrawn from your account";
        }

        return switch (
                event.getTransactionType()
                ) {

            case WITHDRAWAL -> "Cash withdrawal";

            case TRANSFER -> "Money transferred";

            case PAYMENT -> "Payment made";

            default -> "Money withdrawn from your account";
        };
    }

    private String buildTransactionMessage(
            BalanceUpdateEvent event
    ) {

        String currency =
                event.getCurrency() != null
                        ? event.getCurrency().name()
                        : "";

        String amount =
                formatAmount(
                        event.getAmount()
                );

        if (event.getTransactionDirection()
                == TransactionDirection.CREDIT) {

            if (event.getTransactionType() == null) {

                return "Your account was credited with "
                        + currency
                        + " "
                        + amount
                        + ".";
            }

            return switch (
                    event.getTransactionType()
                    ) {

                case DEPOSIT -> "Your account was credited with "
                        + currency
                        + " "
                        + amount
                        + ".";

                case TRANSFER -> "You received "
                        + currency
                        + " "
                        + amount
                        + " into your account.";

                default -> "Your account was credited with "
                        + currency
                        + " "
                        + amount
                        + ".";
            };
        }

        if (event.getTransactionType() == null) {

            return currency
                    + " "
                    + amount
                    + " was debited from your account.";
        }

        return switch (
                event.getTransactionType()
                ) {

            case WITHDRAWAL -> currency
                    + " "
                    + amount
                    + " was withdrawn from your account.";

            case TRANSFER -> currency
                    + " "
                    + amount
                    + " was transferred from your account.";

            case PAYMENT -> currency
                    + " "
                    + amount
                    + " was paid from your account.";

            default -> currency
                    + " "
                    + amount
                    + " was debited from your account.";
        };
    }

    private String buildTanMessage(
            TanNotificationEvent event,
            long expiresInSeconds
    ) {

        long minutes =
                Math.max(
                        1,
                        (expiresInSeconds + 59) / 60
                );

        return "Your TAN for "
                + event.getOperation()
                + " is "
                + event.getTan()
                + ". It expires in approximately "
                + minutes
                + " minute(s). "
                + "Never share this TAN with anyone.";
    }

    private long calculateRemainingSeconds(
            LocalDateTime expiresAt
    ) {

        if (expiresAt == null) {
            return 0;
        }

        return Math.max(
                0,
                Duration.between(
                        LocalDateTime.now(),
                        expiresAt
                ).getSeconds()
        );
    }

    private String formatAmount(
            BigDecimal amount
    ) {

        if (amount == null) {
            return "0.00";
        }

        return amount
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    @Override
    public void sendTanNotification(TanNotificationEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "TAN notification event must not be null"
            );
        }

        log.info(
                "Processing TAN notification. challengeId={}, user={}, operation={}",
                event.getChallengeId(),
                event.getUserEmail(),
                event.getOperation()
        );

        emailService.sendTanEmail(event);
    }
}