package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.ShowtimeRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.ShowtimeResponse;
import com.nguyen.movieticket.entity.BookingStatus;
import com.nguyen.movieticket.entity.Hall;
import com.nguyen.movieticket.entity.Movie;
import com.nguyen.movieticket.entity.Showtime;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.ShowtimeMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.HallRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.repository.ShowtimeRepository;
import com.nguyen.movieticket.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;
    private final BookingRepository bookingRepository;
    private final ShowtimeMapper showtimeMapper;

    @Override
    public List<ShowtimeResponse> getShowtimesByMovieAndCinemaAndDate(Long movieId, Long cinemaId, LocalDate date) {
        List<Showtime> showtimes = showtimeRepository.findByMovieAndCinemaAndDate(movieId, cinemaId, date);
        return showtimeMapper.toResponseList(showtimes);
    }

    @Override
    public List<ShowtimeResponse> getShowtimesByMovie(Long movieId) {
        List<Showtime> showtimes = showtimeRepository.findByMovieIdAndIsActiveTrue(movieId);
        return showtimeMapper.toResponseList(showtimes);
    }

    @Override
    public ShowtimeResponse getShowtimeById(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId));
        return showtimeMapper.toResponse(showtime);
    }

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Hall hall = hallRepository.findById(request.getHallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", request.getHallId()));

        LocalDateTime startTime = request.getStartDateTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

        boolean overlapping = showtimeRepository.existsOverlappingShowtime(
                request.getHallId(), startTime, endTime);
        if (overlapping) {
            throw new BadRequestException("Showtime overlaps with an existing showtime in the same hall");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .hall(hall)
                .startTime(startTime)
                .endTime(endTime)
                .ticketPrice(request.getTicketPrice())
                .isActive(true)
                .build();

        showtime = showtimeRepository.save(showtime);
        log.info("Showtime created for movie '{}' in hall '{}'", movie.getTitle(), hall.getName());
        return showtimeMapper.toResponse(showtime);
    }

    @Override
    @Transactional
    public void deleteShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", showtimeId));
        showtime.setIsActive(false);
        showtimeRepository.save(showtime);
        log.warn("Showtime soft-deleted: {}", showtimeId);
    }

    @Override
    public PageResponse<ShowtimeResponse> getShowtimes(Pageable pageable) {
        Page<Showtime> showtimes = showtimeRepository.findAll(pageable);
        List<ShowtimeResponse> content = showtimes.getContent().stream()
                .map(showtimeMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content, showtimes.getNumber(), showtimes.getSize(),
                showtimes.getTotalElements(), showtimes.getTotalPages(), showtimes.isLast()
        );
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<com.nguyen.movieticket.entity.Booking> expiredBookings =
                bookingRepository.findExpiredBookings(BookingStatus.PENDING, now);

        for (var booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.setCancelReason("Payment expired");
            bookingRepository.save(booking);
            log.info("Expired booking cancelled: {}", booking.getBookingReference());
        }

        if (!expiredBookings.isEmpty()) {
            log.info("Cancelled {} expired bookings", expiredBookings.size());
        }
    }
}
