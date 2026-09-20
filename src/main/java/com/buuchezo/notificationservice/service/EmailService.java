package com.buuchezo.notificationservice.service;

import com.buuchezo.notificationservice.kafka.dto.BalanceUpdateEvent;
import com.buuchezo.notificationservice.kafka.dto.TanNotificationEvent;
import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;

public interface EmailService {

    void sendWelcomeEmail(
            UserRegistrationEvent event
    );

    void sendTransactionAlertEmail(
            BalanceUpdateEvent event
    );

    void sendTanEmail(
            TanNotificationEvent event
    );
}