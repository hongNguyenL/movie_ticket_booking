package com.nguyen.movieticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatLayoutRequest {
    @NotNull(message = "Hall ID is required")
    private Long hallId;

    @NotEmpty(message = "At least one seat must be defined")
    private List<SeatEntry> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatEntry {
        @NotBlank(message = "Row label is required")
        private String rowLabel;

        @NotNull(message = "Seat number is required")
        @Positive(message = "Seat number must be positive")
        private Integer seatNumber;

        private String seatType;
    }
}
