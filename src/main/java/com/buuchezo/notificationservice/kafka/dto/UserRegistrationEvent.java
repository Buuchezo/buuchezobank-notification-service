package com.buuchezo.notificationservice.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationEvent {

    private String email;
    private String firstName;
    private String lastName;
    private String accountNumber;
    private String bankName = "Buuchezo Bank";

}
