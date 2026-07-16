package com.nguyen.movieticket.dto.response;

import java.math.BigDecimal;

public record MonthlyReportResponse(
        String month,
        long totalBookings,
        BigDecimal revenue,
        long newCustomers,
        double occupancyRate
) {
}
