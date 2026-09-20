package com.buuchezo.notificationservice.kafka.service;

import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.TanNotificationEvent;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;
import com.buuchezo.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerListener {

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;


    // ============================================================
    // USER REGISTRATION
    // ============================================================

    @KafkaListener(
            topics = "user-registered-event",
            groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeUserRegistrationEvent(
            String message
    ) {

        try {

            UserRegistrationEvent event =
                    objectMapper.readValue(
                            message,
                            UserRegistrationEvent.class
                    );

            log.info(
                    "Received user registration event. Email: {}",
                    event.getEmail()
            );

            notificationService.processUserRegistration(
                    event
            );

        } catch (Exception e) {

            log.error(
                    "Error processing user registration notification",
                    e
            );

            throw new IllegalStateException(
                    "User registration notification processing failed",
                    e
            );
        }
    }


    // ============================================================
    // BALANCE UPDATE
    // ============================================================

    @KafkaListener(
            topics = "balance-update-notification-event",
            groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeBalanceUpdateEvent(
            String message
    ) {

        try {

            BalanceUpdateEvent event =
                    objectMapper.readValue(
                            message,
                            BalanceUpdateEvent.class
                    );

            log.info(
                    "Received balance update event. Reference: {}",
                    event.getReference()
            );

            notificationService.processBalanceUpdate(
                    event
            );

        } catch (Exception e) {

            log.error(
                    "Error processing balance update notification",
                    e
            );

            throw new IllegalStateException(
                    "Balance update notification processing failed",
                    e
            );
        }
    }


    // ============================================================
    // TAN NOTIFICATION
    // ============================================================

    @KafkaListener(
            topics = "tan-notification-event",
            groupId = "tan-notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void consumeTanNotificationEvent(
            String message
    ) {

        try {

            TanNotificationEvent event =
                    objectMapper.readValue(
                            message,
                            TanNotificationEvent.class
                    );

            /*
             * IMPORTANT:
             *
             * Never log event.getTan().
             *
             * The TAN is sensitive authentication data.
             */

            log.info(
                    "Received TAN notification event. challengeId={}, user={}, operation={}",
                    event.getChallengeId(),
                    event.getUserEmail(),
                    event.getOperation()
            );

            notificationService.processTanNotification(
                    event
            );

        } catch (Exception e) {

            log.error(
                    "Error processing TAN notification",
                    e
            );

            throw new IllegalStateException(
                    "TAN notification processing failed",
                    e
            );
        }
    }
}