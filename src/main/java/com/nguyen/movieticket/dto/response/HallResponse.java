package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HallResponse {
    private Long id;
    private String name;
    private int capacity;
    private String description;
    private CinemaResponse cinema;
}
