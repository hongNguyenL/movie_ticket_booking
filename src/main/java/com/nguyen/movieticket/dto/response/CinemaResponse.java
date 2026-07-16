package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String phone;
    private String email;
    private String description;
    private boolean isActive;
}
