package com.nguyen.movieticket.dto.request;

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
public class UpdateMovieRequest {
    private String title;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private String posterUrl;
    private String trailerUrl;
    private String language;
    private Long genreId;
    private Long directorId;
    private Set<Long> actorIds;
}
