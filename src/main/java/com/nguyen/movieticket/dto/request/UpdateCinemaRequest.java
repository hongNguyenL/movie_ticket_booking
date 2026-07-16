package com.nguyen.movieticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCinemaRequest {
    private String name;
    private String address;
    private String city;
    private String state;
    private String phone;
    private String email;
    private String description;
}
