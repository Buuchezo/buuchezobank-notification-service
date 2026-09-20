package com.buuchezo.notificationservice.service;

import com.buuchezo.notificationservice.dto.NotificationDto;
import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.TanNotificationEvent;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;

import java.util.List;

public interface NotificationService {

    void processUserRegistration(
            UserRegistrationEvent event
    );

    void processBalanceUpdate(
            BalanceUpdateEvent event
    );

    void processTanNotification(
            TanNotificationEvent event
    );

    List<NotificationDto> getNotifications(
            String email
    );

    List<NotificationDto> getUnreadNotifications(
            String email
    );

    NotificationDto markAsRead(
            Long notificationId,
            String email
    );

    int markAllAsRead(
            String email
    );

    void sendTanNotification(TanNotificationEvent event);
}