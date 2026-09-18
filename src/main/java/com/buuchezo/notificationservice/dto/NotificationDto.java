package com.buuchezo.notificationservice.dto;

import com.buuchezo.notificationservice.enums.NotificationChannel;
import com.buuchezo.notificationservice.enums.NotificationStatus;
import com.buuchezo.notificationservice.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;

    private NotificationType notificationType;

    private NotificationChannel channel;

    private String title;

    private String message;

    private NotificationStatus status;

    private boolean read;

    private String transactionReference;

    private LocalDateTime createdAt;
}