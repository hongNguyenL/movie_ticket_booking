package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponse {
    private Long id;
    private String uuid;
    private String title;
    private String description;
    private int duration;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String language;
    private BigDecimal rating;
    private String genre;
    private Long genreId;
    private String directorName;
    private Long directorId;
    private boolean isActive;
    private List<ActorResponse> actors;
    private LocalDateTime createdAt;
}
