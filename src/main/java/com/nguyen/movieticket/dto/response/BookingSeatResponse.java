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
public class BookingSeatResponse {
    private Long id;
    private String rowLabel;
    private int seatNumber;
    private String seatType;
    private BigDecimal price;
}
