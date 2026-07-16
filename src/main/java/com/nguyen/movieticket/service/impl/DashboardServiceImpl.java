package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.response.DashboardStatsResponse;
import com.nguyen.movieticket.dto.response.MonthlyReportResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.entity.Movie;
import com.nguyen.movieticket.mapper.MovieMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.CinemaRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.repository.PaymentRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CinemaRepository cinemaRepository;
    private final PaymentRepository paymentRepository;
    private final MovieMapper movieMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalMovies = movieRepository.countByIsActiveTrue();
        long totalCustomers = userRepository.countByEnabledTrue();
        long totalCinemas = cinemaRepository.countByIsActiveTrue();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfYear = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal totalRevenue = paymentRepository.sumCompletedPaymentsBetween(startOfYear, now);

        List<Object[]> monthlyRevenueData = paymentRepository.getMonthlyRevenue(startOfYear, now);
        Map<String, BigDecimal> monthlyRevenue = monthlyRevenueData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<Object[]> popularMovies = movieRepository.findMostBookedMovies(PageRequest.of(0, 5));
        List<MovieSummaryResponse> popularMovieList = popularMovies.stream()
                .limit(5)
                .map(row -> movieMapper.toSummary((Movie) row[0]))
                .toList();

        long totalBookings = bookingRepository.count();

        double occupancyRate = calculateOccupancyRate();

        return DashboardStatsResponse.builder()
                .totalMovies(totalMovies)
                .totalBookings(totalBookings)
                .totalCustomers(totalCustomers)
                .totalCinemas(totalCinemas)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .occupancyRate(occupancyRate)
                .monthlyRevenue(monthlyRevenue)
                .popularMovies(popularMovieList)
                .build();
    }

    @Override
    public MonthlyReportResponse getMonthlyReport(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().plusDays(1).atStartOfDay();

        long totalBookings = bookingRepository.countByShowtimeStartTimeBetween(start, end);
        BigDecimal revenue = paymentRepository.sumCompletedPaymentsBetween(start, end);
        long newCustomers = userRepository.countByCreatedAtBetween(start, end);
        double occupancyRate = calculateOccupancyRate();

        if (revenue == null) revenue = BigDecimal.ZERO;

        return new MonthlyReportResponse(yearMonth, totalBookings, revenue, newCustomers, occupancyRate);
    }

    private double calculateOccupancyRate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        long confirmedBookings = bookingRepository.countByStatusAndShowtimeStartTimeBetween(
                com.nguyen.movieticket.entity.BookingStatus.CONFIRMED, startOfMonth, now);
        long totalBookings = bookingRepository.countByShowtimeStartTimeBetween(startOfMonth, now);

        if (totalBookings == 0) return 0.0;
        return (double) confirmedBookings / totalBookings * 100.0;
    }
}
