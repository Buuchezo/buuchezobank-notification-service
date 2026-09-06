package com.buuchezo.notificationservice.service;

import com.buuchezo.notificationservice.kafka.dto.UserRegistrationEvent;

public interface EmailService {
    void sendWelcomeEmail(UserRegistrationEvent event);
    void sendCreditAlert();
    void sendDebitAlert();
}
