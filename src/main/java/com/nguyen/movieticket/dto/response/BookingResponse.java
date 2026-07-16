package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String uuid;
    private String bookingReference;
    private MovieSummaryResponse movie;
    private CinemaResponse cinema;
    private HallResponse hall;
    private LocalDateTime showtimeStart;
    private LocalDateTime showtimeEnd;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime bookedAt;
    private LocalDateTime expiresAt;
    private List<BookingSeatResponse> seats;
    private PaymentResponse payment;
}
