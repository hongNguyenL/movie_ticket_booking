package com.nguyen.movieticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHallRequest {
    private String name;
    private Integer capacity;
    private String description;
    private Boolean isActive;
}
