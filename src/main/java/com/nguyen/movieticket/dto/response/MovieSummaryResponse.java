package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSummaryResponse {
    private Long id;
    private String uuid;
    private String title;
    private int duration;
    private String posterUrl;
    private String language;
    private BigDecimal rating;
    private String description;
    private String genre;
}
