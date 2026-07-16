package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.CreateMovieRequest;
import com.nguyen.movieticket.dto.request.MovieSearchRequest;
import com.nguyen.movieticket.dto.request.UpdateMovieRequest;
import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {
    PageResponse<MovieSummaryResponse> getActiveMovies(Pageable pageable);

    PageResponse<MovieSummaryResponse> searchMovies(MovieSearchRequest request, Pageable pageable);

    MovieResponse getMovieByUuid(String uuid);

    MovieResponse getMovieDetails(Long movieId);

    List<MovieSummaryResponse> getTopRatedMovies(int limit);

    MovieResponse createMovie(CreateMovieRequest request);

    MovieResponse updateMovie(Long movieId, UpdateMovieRequest request);

    void deleteMovie(Long movieId);

    PageResponse<MovieSummaryResponse> getMostBookedMovies(Pageable pageable);
}
