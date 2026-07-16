package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.BookingResponse;
import com.nguyen.movieticket.dto.response.BookingSeatResponse;
import com.nguyen.movieticket.dto.response.BookingSummaryResponse;
import com.nguyen.movieticket.entity.Booking;
import com.nguyen.movieticket.entity.BookingSeat;
import com.nguyen.movieticket.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MovieMapper.class, ShowtimeMapper.class, CinemaMapper.class, HallMapper.class, PaymentMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "movie", source = "showtime.movie")
    @Mapping(target = "cinema", source = "showtime.hall.cinema")
    @Mapping(target = "hall", source = "showtime.hall")
    @Mapping(target = "showtimeStart", source = "showtime.startTime")
    @Mapping(target = "showtimeEnd", source = "showtime.endTime")
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "movieTitle", source = "showtime.movie.title")
    @Mapping(target = "cinemaName", source = "showtime.hall.cinema.name")
    @Mapping(target = "showtimeStart", source = "showtime.startTime")
    BookingSummaryResponse toSummary(Booking booking);

    List<BookingSummaryResponse> toSummaryList(List<Booking> bookings);

    default BookingSeatResponse toSeatResponse(BookingSeat bookingSeat) {
        if (bookingSeat == null) return null;
        Seat seat = bookingSeat.getSeat();
        return BookingSeatResponse.builder()
                .id(bookingSeat.getId())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType().name())
                .price(bookingSeat.getPrice())
                .build();
    }
}
