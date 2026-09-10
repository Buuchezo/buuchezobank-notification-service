package com.buuchezo.notificationservice.kafka.service;


import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;
import com.buuchezo.notificationservice.service.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerListener {
    private final EmailServiceImpl emailService;

    @KafkaListener(topics = "user-registered-event", groupId = "notification-group")
    public void consumeUserRegisteredEvent(UserRegistrationEvent event) {
        log.info("Received user registration event: {}", event);
        try {
            emailService.sendWelcomeEmail(event);
        } catch (Exception e) {
            log.error("Error sending email out: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "balance-update-notification-event", groupId = "notification-group")
    public void consumeBalanceUpdateEvent(BalanceUpdateEvent event) {
        log.info("Received balance update  event: {}", event);
        try {
            emailService.sendTransactionAlertEmail(event);
        } catch (Exception e) {
            log.error("Error sending Balance update email out: {}", e.getMessage());
        }
    }



}
