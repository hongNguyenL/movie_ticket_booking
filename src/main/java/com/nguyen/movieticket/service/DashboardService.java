package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.response.DashboardStatsResponse;
import com.nguyen.movieticket.dto.response.MonthlyReportResponse;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();

    MonthlyReportResponse getMonthlyReport(String yearMonth);
}
