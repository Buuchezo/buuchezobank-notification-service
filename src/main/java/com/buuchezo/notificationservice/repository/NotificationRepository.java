package com.buuchezo.notificationservice.repository;

import com.buuchezo.notificationservice.entity.Notification;
import com.buuchezo.notificationservice.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientEmailAndChannelOrderByCreatedAtDesc(
            String recipientEmail,
            NotificationChannel channel
    );

    List<Notification> findByRecipientEmailAndChannelAndReadFalseOrderByCreatedAtDesc(
            String recipientEmail,
            NotificationChannel channel
    );

    Optional<Notification> findByIdAndRecipientEmailAndChannel(
            Long id,
            String recipientEmail,
            NotificationChannel channel
    );
}