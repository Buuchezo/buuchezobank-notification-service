package com.buuchezo.notificationservice.kafka.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TanNotificationEvent {

    private String eventId;

    private String userEmail;

    private String operation;

    private String challengeId;

    private String tan;

    private LocalDateTime expiresAt;
}