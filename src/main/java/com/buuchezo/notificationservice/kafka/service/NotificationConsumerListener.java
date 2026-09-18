package com.buuchezo.notificationservice.kafka.service;

import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;
import com.buuchezo.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "user-registered-event",
            groupId = "notification-group"
    )
    public void consumeUserRegisteredEvent(
            UserRegistrationEvent event
    ) {

        log.info(
                "Received user registration event for {}",
                event.getEmail()
        );

        try {

            notificationService.processUserRegistration(event);

        } catch (Exception e) {

            log.error(
                    "Error processing user registration notification",
                    e
            );
        }
    }

    @KafkaListener(
            topics = "balance-update-notification-event",
            groupId = "notification-group"
    )
    public void consumeBalanceUpdateEvent(
            BalanceUpdateEvent event
    ) {

        log.info(
                "Received balance update event. Reference: {}",
                event.getReference()
        );

        try {

            notificationService.processBalanceUpdate(event);

        } catch (Exception e) {

            log.error(
                    "Error processing balance update notification",
                    e
            );
        }
    }
}