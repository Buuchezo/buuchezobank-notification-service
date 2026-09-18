package com.buuchezo.notificationservice.entity;

import com.buuchezo.notificationservice.enums.NotificationChannel;
import com.buuchezo.notificationservice.enums.NotificationStatus;
import com.buuchezo.notificationservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email address of the user receiving the notification.
     */
    @Column(nullable = false)
    private String recipientEmail;

    /**
     * Optional phone number.
     * <p>
     * This will be useful later if SMS notifications are implemented.
     */
    private String recipientPhone;

    /**
     * What kind of event caused this notification.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    /**
     * How the notification is delivered.
     * <p>
     * Examples:
     * IN_APP
     * EMAIL
     * SMS
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    /**
     * Short title displayed to the user.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Main notification message.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /**
     * Delivery status.
     * <p>
     * For an in-app notification this will normally be SENT
     * once it has successfully been persisted.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    /**
     * Whether the user has opened/read the notification.
     */
    @Column(nullable = false)
    private boolean read;

    /**
     * Reference to the transaction that caused this notification.
     */
    private String transactionReference;

    /**
     * Time at which the notification was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
