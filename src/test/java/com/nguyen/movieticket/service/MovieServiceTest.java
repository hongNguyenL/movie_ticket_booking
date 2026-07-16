package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.MovieSearchRequest;
import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.entity.*;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.MovieMapper;
import com.nguyen.movieticket.repository.DirectorRepository;
import com.nguyen.movieticket.repository.GenreRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private DirectorRepository directorRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    void getActiveMovies_ShouldReturnPaginatedMovies() {
        Pageable pageable = PageRequest.of(0, 10);
        Movie movie1 = Movie.builder().id(1L).title("Movie 1").build();
        Movie movie2 = Movie.builder().id(2L).title("Movie 2").build();
        Page<Movie> moviePage = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

        MovieSummaryResponse summary1 = MovieSummaryResponse.builder().id(1L).title("Movie 1").build();
        MovieSummaryResponse summary2 = MovieSummaryResponse.builder().id(2L).title("Movie 2").build();

        when(movieRepository.findByIsActiveTrue(pageable)).thenReturn(moviePage);
        when(movieMapper.toSummary(movie1)).thenReturn(summary1);
        when(movieMapper.toSummary(movie2)).thenReturn(summary2);

        PageResponse<MovieSummaryResponse> result = movieService.getActiveMovies(pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).getTitle()).isEqualTo("Movie 1");
        assertThat(result.content().get(1).getTitle()).isEqualTo("Movie 2");
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Test
    void getMovieByUuid_ShouldReturnMovie_WhenExists() {
        String uuid = UUID.randomUUID().toString();
        Movie movie = Movie.builder().id(1L).title("Test Movie").build();
        MovieResponse response = MovieResponse.builder().id(1L).title("Test Movie").build();

        when(movieRepository.findByUuid(java.util.UUID.fromString(uuid))).thenReturn(Optional.of(movie));
        when(movieMapper.toResponse(movie)).thenReturn(response);

        MovieResponse result = movieService.getMovieByUuid(uuid);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void getMovieByUuid_ShouldThrowException_WhenNotExists() {
        String uuid = UUID.randomUUID().toString();

        when(movieRepository.findByUuid(java.util.UUID.fromString(uuid))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieByUuid(uuid))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie");
    }

    @Test
    void searchMovies_ShouldReturnFilteredResults() {
        Pageable pageable = PageRequest.of(0, 10);
        MovieSearchRequest request = MovieSearchRequest.builder()
                .keyword("dark")
                .build();

        Movie movie = Movie.builder().id(1L).title("The Dark Knight").build();
        Page<Movie> moviePage = new PageImpl<>(List.of(movie), pageable, 1);
        MovieSummaryResponse summary = MovieSummaryResponse.builder().id(1L).title("The Dark Knight").build();

        when(movieRepository.searchByTitle("dark", pageable)).thenReturn(moviePage);
        when(movieMapper.toSummary(movie)).thenReturn(summary);

        PageResponse<MovieSummaryResponse> result = movieService.searchMovies(request, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getTitle()).isEqualTo("The Dark Knight");
    }
}
