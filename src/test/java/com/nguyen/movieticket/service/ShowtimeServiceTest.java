package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ShowtimeRequest;
import com.nguyen.movieticket.dto.response.ShowtimeResponse;
import com.nguyen.movieticket.entity.*;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.ShowtimeMapper;
import com.nguyen.movieticket.repository.*;
import com.nguyen.movieticket.service.impl.ShowtimeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private ShowtimeMapper showtimeMapper;

    @InjectMocks
    private ShowtimeServiceImpl showtimeService;

    @Test
    void createShowtime_ShouldCreate_WhenNoOverlap() {
        Long movieId = 1L;
        Long hallId = 1L;

        Movie movie = Movie.builder().id(movieId).title("Test Movie").duration(120).build();
        Hall hall = Hall.builder().id(hallId).name("Hall 1").build();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusMinutes(120);

        ShowtimeRequest request = ShowtimeRequest.builder()
                .movieId(movieId)
                .hallId(hallId)
                .startDateTime(startTime)
                .ticketPrice(new BigDecimal("12.00"))
                .build();

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(hallId)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsOverlappingShowtime(hallId, startTime, endTime)).thenReturn(false);
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(invocation -> {
            Showtime s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(showtimeMapper.toResponse(any(Showtime.class))).thenReturn(
                ShowtimeResponse.builder().id(1L).build());

        ShowtimeResponse result = showtimeService.createShowtime(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(showtimeRepository).save(any(Showtime.class));
    }

    @Test
    void createShowtime_ShouldThrowException_WhenOverlap() {
        Long movieId = 1L;
        Long hallId = 1L;

        Movie movie = Movie.builder().id(movieId).title("Test Movie").duration(120).build();
        Hall hall = Hall.builder().id(hallId).name("Hall 1").build();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusMinutes(120);

        ShowtimeRequest request = ShowtimeRequest.builder()
                .movieId(movieId)
                .hallId(hallId)
                .startDateTime(startTime)
                .ticketPrice(new BigDecimal("12.00"))
                .build();

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(hallRepository.findById(hallId)).thenReturn(Optional.of(hall));
        when(showtimeRepository.existsOverlappingShowtime(hallId, startTime, endTime)).thenReturn(true);

        assertThatThrownBy(() -> showtimeService.createShowtime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlap");

        verify(showtimeRepository, never()).save(any(Showtime.class));
    }
}
