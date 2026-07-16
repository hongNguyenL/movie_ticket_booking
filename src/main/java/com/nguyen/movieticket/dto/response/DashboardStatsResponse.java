package com.nguyen.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {
    private long totalMovies;
    private long totalBookings;
    private long totalCustomers;
    private long totalCinemas;
    private BigDecimal totalRevenue;
    private double occupancyRate;
    private Map<String, BigDecimal> monthlyRevenue;
    private List<MovieSummaryResponse> popularMovies;
}
