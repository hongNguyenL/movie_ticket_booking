package com.nguyen.movieticket.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeRequest {
    private Long id;

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Hall ID is required")
    private Long hallId;

    @NotNull(message = "Start date/time is required")
    @Future(message = "Start date/time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "Ticket price is required")
    @Positive(message = "Ticket price must be positive")
    private BigDecimal ticketPrice;
}
