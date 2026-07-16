package com.nguyen.movieticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMovieRequest {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    private LocalDate releaseDate;

    private String posterUrl;

    private String trailerUrl;

    private String language;

    @NotNull(message = "Genre ID is required")
    private Long genreId;

    @NotNull(message = "Director ID is required")
    private Long directorId;

    private Set<Long> actorIds;
}
