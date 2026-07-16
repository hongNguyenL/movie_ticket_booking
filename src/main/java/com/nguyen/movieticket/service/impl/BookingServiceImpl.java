package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.BookingRequest;
import com.nguyen.movieticket.dto.response.BookingResponse;
import com.nguyen.movieticket.dto.response.BookingSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.SeatResponse;
import com.nguyen.movieticket.entity.Booking;
import com.nguyen.movieticket.entity.BookingSeat;
import com.nguyen.movieticket.entity.BookingStatus;
import com.nguyen.movieticket.entity.Seat;
import com.nguyen.movieticket.entity.SeatType;
import com.nguyen.movieticket.entity.Showtime;
import com.nguyen.movieticket.entity.User;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.BookingMapper;
import com.nguyen.movieticket.mapper.SeatMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.BookingSeatRepository;
import com.nguyen.movieticket.repository.SeatRepository;
import com.nguyen.movieticket.repository.ShowtimeRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final SeatMapper seatMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, BookingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", request.getShowtimeId()));

        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowtime(request.getShowtimeId());
        for (Long seatId : request.getSeatIds()) {
            if (bookedSeatIds.contains(seatId)) {
                throw new BadRequestException("Seat ID " + seatId + " is already booked");
            }
        }

        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new BadRequestException("One or more seats not found");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal multiplier = getSeatTypeMultiplier(seat.getSeatType());
            totalPrice = totalPrice.add(showtime.getTicketPrice().multiply(multiplier));
        }

        String bookingReference = generateBookingReference();

        Booking booking = Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference(bookingReference)
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .bookedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        for (Seat seat : seats) {
            BigDecimal multiplier = getSeatTypeMultiplier(seat.getSeatType());
            BigDecimal seatPrice = showtime.getTicketPrice().multiply(multiplier);
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .price(seatPrice)
                    .build();
            booking.getBookingSeats().add(bookingSeat);
        }

        booking = bookingRepository.save(booking);
        log.info("Booking created: {} for user {}", bookingReference, userId);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING status");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        booking = bookingRepository.save(booking);
        log.info("Booking confirmed: {}", bookingReference);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(String bookingReference, String reason) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot cancel booking after showtime has started");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason(reason);
        booking = bookingRepository.save(booking);
        log.info("Booking cancelled: {}", bookingReference);
        return bookingMapper.toResponse(booking);
    }

    @Override
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", bookingReference));
        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingSummaryResponse> getUserBookingHistory(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookedAtDesc(userId);
        return bookingMapper.toSummaryList(bookings);
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findAllByOrderByBookedAtDesc(pageable);
        List<BookingResponse> content = bookings.getContent().stream()
                .map(bookingMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content, bookings.getNumber(), bookings.getSize(),
                bookings.getTotalElements(), bookings.getTotalPages(), bookings.isLast()
        );
    }

    @Override
    public PageResponse<BookingResponse> searchBookings(String keyword, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.searchByKeyword(keyword, pageable);
        List<BookingResponse> content = bookings.getContent().stream()
                .map(bookingMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content, bookings.getNumber(), bookings.getSize(),
                bookings.getTotalElements(), bookings.getTotalPages(), bookings.isLast()
        );
    }

    @Override
    public List<SeatResponse> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId));

        List<Seat> allSeats = seatRepository.findActiveSeatsByHallId(showtime.getHall().getId());
        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowtime(showtimeId);

        return allSeats.stream()
                .map(seat -> {
                    SeatResponse response = seatMapper.toResponse(seat);
                    response.setAvailable(!bookedSeatIds.contains(seat.getId()));
                    return response;
                })
                .toList();
    }

    @Override
    public BigDecimal calculateTotalPrice(Set<Long> seatIds) {
        List<Seat> seats = seatRepository.findAllById(seatIds);
        BigDecimal basePrice = new BigDecimal("10.00");
        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal multiplier = getSeatTypeMultiplier(seat.getSeatType());
            total = total.add(basePrice.multiply(multiplier));
        }
        return total;
    }

    private BigDecimal getSeatTypeMultiplier(SeatType seatType) {
        return switch (seatType) {
            case VIP -> new BigDecimal("1.5");
            case COUPLE -> new BigDecimal("2.0");
            default -> BigDecimal.ONE;
        };
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
