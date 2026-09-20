package com.buuchezo.notificationservice.kafka.service;

import com.buuchezo.notificationservice.kafka.dto.TanNotificationEvent;
import com.buuchezo.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TanNotificationListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "tan-notification-event",
            groupId = "tan-notification-group"
    )
    public void consume(String message) {

        try {

            TanNotificationEvent event =
                    objectMapper.readValue(
                            message,
                            TanNotificationEvent.class
                    );

            log.info(
                    "Received TAN notification event. challengeId={}, user={}, operation={}",
                    event.getChallengeId(),
                    event.getUserEmail(),
                    event.getOperation()
            );

            notificationService.sendTanNotification(
                    event
            );

        } catch (Exception e) {

            log.error(
                    "Failed to process TAN notification event",
                    e
            );

            throw new IllegalStateException(
                    "TAN notification processing failed",
                    e
            );
        }
    }
}