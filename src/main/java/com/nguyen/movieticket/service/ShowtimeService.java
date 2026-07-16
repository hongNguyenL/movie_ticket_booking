package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ShowtimeRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.ShowtimeResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ShowtimeService {
    List<ShowtimeResponse> getShowtimesByMovieAndCinemaAndDate(Long movieId, Long cinemaId, LocalDate date);

    List<ShowtimeResponse> getShowtimesByMovie(Long movieId);

    ShowtimeResponse getShowtimeById(Long showtimeId);

    ShowtimeResponse createShowtime(ShowtimeRequest request);

    void deleteShowtime(Long showtimeId);

    PageResponse<ShowtimeResponse> getShowtimes(Pageable pageable);

    void cancelExpiredBookings();
}
