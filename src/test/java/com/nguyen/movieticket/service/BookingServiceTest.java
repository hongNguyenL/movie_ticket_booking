package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.BookingRequest;
import com.nguyen.movieticket.dto.response.BookingResponse;
import com.nguyen.movieticket.dto.response.SeatResponse;
import com.nguyen.movieticket.entity.*;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.BookingMapper;
import com.nguyen.movieticket.mapper.SeatMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.BookingSeatRepository;
import com.nguyen.movieticket.repository.SeatRepository;
import com.nguyen.movieticket.repository.ShowtimeRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSeatRepository bookingSeatRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private SeatMapper seatMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_ShouldCreateBooking_WhenSeatsAvailable() {
        Long userId = 1L;
        User user = User.builder().id(userId).username("testuser").build();

        Hall hall = Hall.builder().id(1L).name("Hall 1").build();
        Showtime showtime = Showtime.builder()
                .id(1L)
                .hall(hall)
                .ticketPrice(new BigDecimal("10.00"))
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        Seat seat1 = Seat.builder().id(1L).rowLabel("A").seatNumber(1).seatType(SeatType.STANDARD).build();
        Seat seat2 = Seat.builder().id(2L).rowLabel("A").seatNumber(2).seatType(SeatType.VIP).build();

        BookingRequest request = BookingRequest.builder()
                .showtimeId(1L)
                .seatIds(Set.of(1L, 2L))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingSeatRepository.findBookedSeatIdsByShowtime(1L)).thenReturn(Set.of());
        when(seatRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(seat1, seat2));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        BookingResponse result = bookingService.createBooking(userId, request);

        assertThat(result).isNotNull();
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowException_WhenSeatsAlreadyBooked() {
        Long userId = 1L;
        User user = User.builder().id(userId).username("testuser").build();

        Showtime showtime = Showtime.builder()
                .id(1L)
                .ticketPrice(new BigDecimal("10.00"))
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        BookingRequest request = BookingRequest.builder()
                .showtimeId(1L)
                .seatIds(Set.of(1L, 2L))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(bookingSeatRepository.findBookedSeatIdsByShowtime(1L)).thenReturn(Set.of(1L));

        assertThatThrownBy(() -> bookingService.createBooking(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already booked");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_ShouldCancel_WhenBeforeShowtime() {
        String bookingRef = "BK-TEST123";
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference(bookingRef)
                .status(BookingStatus.CONFIRMED)
                .showtime(Showtime.builder()
                        .startTime(LocalDateTime.now().plusDays(1))
                        .build())
                .build();

        when(bookingRepository.findByBookingReference(bookingRef)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(new BookingResponse());

        BookingResponse result = bookingService.cancelBooking(bookingRef, "Changed mind");

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelReason()).isEqualTo("Changed mind");
    }

    @Test
    void cancelBooking_ShouldThrowException_WhenAfterShowtime() {
        String bookingRef = "BK-TEST123";
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference(bookingRef)
                .status(BookingStatus.CONFIRMED)
                .showtime(Showtime.builder()
                        .startTime(LocalDateTime.now().minusHours(1))
                        .build())
                .build();

        when(bookingRepository.findByBookingReference(bookingRef)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingRef, "Too late"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("after showtime has started");
    }

    @Test
    void getAvailableSeats_ShouldReturnSeats_ExcludingBooked() {
        Long showtimeId = 1L;

        Hall hall = Hall.builder().id(1L).name("Hall 1").build();
        Showtime showtime = Showtime.builder()
                .id(showtimeId)
                .hall(hall)
                .build();

        Seat seat1 = Seat.builder().id(1L).rowLabel("A").seatNumber(1).build();
        Seat seat2 = Seat.builder().id(2L).rowLabel("A").seatNumber(2).build();
        Seat seat3 = Seat.builder().id(3L).rowLabel("A").seatNumber(3).build();

        when(showtimeRepository.findById(showtimeId)).thenReturn(Optional.of(showtime));
        when(seatRepository.findActiveSeatsByHallId(1L)).thenReturn(List.of(seat1, seat2, seat3));
        when(bookingSeatRepository.findBookedSeatIdsByShowtime(showtimeId)).thenReturn(Set.of(2L));

        SeatResponse seatResponse1 = SeatResponse.builder().id(1L).rowLabel("A").seatNumber(1).isAvailable(true).build();
        SeatResponse seatResponse2 = SeatResponse.builder().id(2L).rowLabel("A").seatNumber(2).isAvailable(false).build();
        SeatResponse seatResponse3 = SeatResponse.builder().id(3L).rowLabel("A").seatNumber(3).isAvailable(true).build();

        when(seatMapper.toResponse(seat1)).thenReturn(seatResponse1);
        when(seatMapper.toResponse(seat2)).thenReturn(seatResponse2);
        when(seatMapper.toResponse(seat3)).thenReturn(seatResponse3);

        List<SeatResponse> result = bookingService.getAvailableSeats(showtimeId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).isAvailable()).isTrue();
        assertThat(result.get(1).isAvailable()).isFalse();
        assertThat(result.get(2).isAvailable()).isTrue();
    }
}
