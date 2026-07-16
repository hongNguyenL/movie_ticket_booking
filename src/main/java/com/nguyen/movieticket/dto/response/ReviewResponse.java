package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private String uuid;
    private String userName;
    private Long userId;
    private Long movieId;
    private String movieTitle;
    private int rating;
    private String comment;
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
