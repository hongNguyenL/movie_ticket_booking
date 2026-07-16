package com.nguyen.movieticket.dto.response;

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
public class BookingSummaryResponse {
    private Long id;
    private String bookingReference;
    private String movieTitle;
    private String cinemaName;
    private LocalDateTime showtimeStart;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime bookedAt;
}
