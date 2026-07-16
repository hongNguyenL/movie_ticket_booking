package com.nguyen.movieticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSearchRequest {
    private String keyword;
    private Long genreId;
    private String language;
    private Double minRating;
    private Integer page;
    private Integer size;
}
