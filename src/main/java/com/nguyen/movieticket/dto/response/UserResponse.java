package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String uuid;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private boolean enabled;
    private boolean accountLocked;
    private LocalDateTime createdAt;
    private Set<String> roles;
}
