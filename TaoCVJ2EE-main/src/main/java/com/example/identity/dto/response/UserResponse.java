package com.example.identity.dto.response;

import com.example.identity.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String email;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<String> roles;
    String avatar;
    UserStatus status;

}