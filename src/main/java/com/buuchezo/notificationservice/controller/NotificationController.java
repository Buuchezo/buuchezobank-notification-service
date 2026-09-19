package com.buuchezo.notificationservice.controller;

import com.buuchezo.notificationservice.dto.ApiResponse;
import com.buuchezo.notificationservice.dto.NotificationDto;
import com.buuchezo.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final String USER_EMAIL_HEADER =
            "X-User-Email";

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>>
    getNotifications(
            @RequestHeader(USER_EMAIL_HEADER) String email
    ) {

        List<NotificationDto> notifications =
                notificationService.getNotifications(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        "Notifications retrieved successfully",
                        notifications
                )
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>>
    getUnreadNotifications(
            @RequestHeader(USER_EMAIL_HEADER) String email
    ) {

        List<NotificationDto> notifications =
                notificationService.getUnreadNotifications(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        "Unread notifications retrieved successfully",
                        notifications
                )
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>>
    markAsRead(
            @PathVariable Long id,
            @RequestHeader(USER_EMAIL_HEADER) String email
    ) {

        NotificationDto notification =
                notificationService.markAsRead(
                        id,
                        email
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        "Notification marked as read",
                        notification
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>>
    markAllAsRead(
            @RequestHeader(USER_EMAIL_HEADER) String email
    ) {

        int updatedCount =
                notificationService.markAllAsRead(email);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        200,
                        "All notifications marked as read",
                        updatedCount
                )
        );
    }
}