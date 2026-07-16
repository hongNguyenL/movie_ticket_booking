package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.CreateMovieRequest;
import com.nguyen.movieticket.dto.request.MovieSearchRequest;
import com.nguyen.movieticket.dto.request.UpdateMovieRequest;
import com.nguyen.movieticket.dto.response.ActorResponse;
import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.entity.Actor;
import com.nguyen.movieticket.entity.Director;
import com.nguyen.movieticket.entity.Genre;
import com.nguyen.movieticket.entity.Movie;
import com.nguyen.movieticket.entity.MovieActor;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.MovieMapper;
import com.nguyen.movieticket.repository.ActorRepository;
import com.nguyen.movieticket.repository.DirectorRepository;
import com.nguyen.movieticket.repository.GenreRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final MovieMapper movieMapper;

    @Override
    public PageResponse<MovieSummaryResponse> getActiveMovies(Pageable pageable) {
        Page<Movie> movies = movieRepository.findByIsActiveTrue(pageable);
        List<MovieSummaryResponse> content = movies.getContent().stream()
                .map(movieMapper::toSummary)
                .toList();
        return new PageResponse<>(
                content, movies.getNumber(), movies.getSize(),
                movies.getTotalElements(), movies.getTotalPages(), movies.isLast()
        );
    }

    @Override
    public PageResponse<MovieSummaryResponse> searchMovies(MovieSearchRequest request, Pageable pageable) {
        Page<Movie> movies;
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            movies = movieRepository.searchByTitle(request.getKeyword(), pageable);
        } else {
            BigDecimal minRating = request.getMinRating() != null
                    ? BigDecimal.valueOf(request.getMinRating())
                    : null;
            movies = movieRepository.filterMovies(request.getGenreId(), request.getLanguage(), minRating, pageable);
        }
        List<MovieSummaryResponse> content = movies.getContent().stream()
                .map(movieMapper::toSummary)
                .toList();
        return new PageResponse<>(
                content, movies.getNumber(), movies.getSize(),
                movies.getTotalElements(), movies.getTotalPages(), movies.isLast()
        );
    }

    @Override
    public MovieResponse getMovieByUuid(String uuid) {
        Movie movie = movieRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "uuid", uuid));
        return buildMovieResponseWithActors(movie);
    }

    @Override
    public MovieResponse getMovieDetails(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));
        return buildMovieResponseWithActors(movie);
    }

    @Override
    public List<MovieSummaryResponse> getTopRatedMovies(int limit) {
        List<Movie> movies = movieRepository.findTopRated(PageRequest.of(0, limit));
        return movieMapper.toSummaryList(movies);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", request.getGenreId()));
        Director director = directorRepository.findById(request.getDirectorId())
                .orElseThrow(() -> new ResourceNotFoundException("Director", "id", request.getDirectorId()));

        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .releaseDate(request.getReleaseDate())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .language(request.getLanguage())
                .genre(genre)
                .director(director)
                .isActive(true)
                .build();

        if (request.getActorIds() != null) {
            for (Long actorId : request.getActorIds()) {
                Actor actor = actorRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Actor", "id", actorId));
                MovieActor movieActor = MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .build();
                movie.getMovieActors().add(movieActor);
            }
        }

        movie = movieRepository.save(movie);
        log.info("Movie created: {}", movie.getTitle());
        return buildMovieResponseWithActors(movie);
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(Long movieId, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDuration() != null) movie.setDuration(request.getDuration());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getGenreId() != null) {
            Genre genre = genreRepository.findById(request.getGenreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", request.getGenreId()));
            movie.setGenre(genre);
        }
        if (request.getDirectorId() != null) {
            Director director = directorRepository.findById(request.getDirectorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Director", "id", request.getDirectorId()));
            movie.setDirector(director);
        }
        if (request.getActorIds() != null) {
            movie.getMovieActors().clear();
            for (Long actorId : request.getActorIds()) {
                Actor actor = actorRepository.findById(actorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Actor", "id", actorId));
                MovieActor movieActor = MovieActor.builder()
                        .movie(movie)
                        .actor(actor)
                        .build();
                movie.getMovieActors().add(movieActor);
            }
        }

        movie = movieRepository.save(movie);
        log.info("Movie updated: {}", movie.getTitle());
        return buildMovieResponseWithActors(movie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", movieId));
        movie.setIsActive(false);
        movieRepository.save(movie);
        log.warn("Movie soft-deleted: {}", movie.getTitle());
    }

    @Override
    public PageResponse<MovieSummaryResponse> getMostBookedMovies(Pageable pageable) {
        List<Object[]> results = movieRepository.findMostBookedMovies(pageable);
        List<MovieSummaryResponse> content = results.stream()
                .map(row -> movieMapper.toSummary((Movie) row[0]))
                .toList();

        long total = movieRepository.count();
        int totalPages = (int) Math.ceil((double) total / pageable.getPageSize());

        return new PageResponse<>(
                content, pageable.getPageNumber(), pageable.getPageSize(),
                total, totalPages, pageable.getPageNumber() >= totalPages - 1
        );
    }

    private MovieResponse buildMovieResponseWithActors(Movie movie) {
        MovieResponse response = movieMapper.toResponse(movie);

        if (movie.getMovieActors() != null) {
            List<ActorResponse> actorResponses = movie.getMovieActors().stream()
                    .map(ma -> ActorResponse.builder()
                            .id(ma.getActor().getId())
                            .name(ma.getActor().getName())
                            .photo(ma.getActor().getPhoto())
                            .roleName(ma.getRoleName())
                            .build())
                    .toList();
            response.setActors(actorResponses);
        }

        return response;
    }
}
