package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.BookingRequest;
import com.nguyen.movieticket.dto.response.BookingResponse;
import com.nguyen.movieticket.dto.response.BookingSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.SeatResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingRequest request);

    BookingResponse confirmBooking(String bookingReference);

    BookingResponse cancelBooking(String bookingReference, String reason);

    BookingResponse getBookingByReference(String bookingReference);

    List<BookingSummaryResponse> getUserBookingHistory(Long userId);

    PageResponse<BookingResponse> getAllBookings(Pageable pageable);

    PageResponse<BookingResponse> searchBookings(String keyword, Pageable pageable);

    List<SeatResponse> getAvailableSeats(Long showtimeId);

    BigDecimal calculateTotalPrice(Set<Long> seatIds);
}
