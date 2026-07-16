package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.MovieSearchRequest;
import com.nguyen.movieticket.dto.response.CinemaResponse;
import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.dto.response.ShowtimeResponse;
import com.nguyen.movieticket.service.MovieService;
import com.nguyen.movieticket.service.ReviewService;
import com.nguyen.movieticket.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public String browseMovies(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long genreId,
                               @RequestParam(required = false) String language,
                               @RequestParam(required = false) Double minRating,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               Model model) {
        MovieSearchRequest searchRequest = MovieSearchRequest.builder()
                .keyword(keyword)
                .genreId(genreId)
                .language(language)
                .minRating(minRating)
                .page(page)
                .size(size)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MovieSummaryResponse> movies;

        if (keyword != null || genreId != null || language != null || minRating != null) {
            movies = movieService.searchMovies(searchRequest, pageable);
        } else {
            movies = movieService.getActiveMovies(pageable);
        }

        model.addAttribute("movies", movies);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedGenreId", genreId);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedMinRating", minRating);
        return "movies/list";
    }

    @GetMapping("/{id:\\d+}")
    public String movieDetailById(@PathVariable Long id,
                                  @RequestParam(required = false) Long cinemaId,
                                  @RequestParam(required = false) String date,
                                  @RequestParam(required = false) Long showtimeId,
                                  Model model) {
        if (showtimeId != null) {
            return "redirect:/booking/seats/" + showtimeId;
        }
        MovieResponse movie = movieService.getMovieDetails(id);
        List<ShowtimeResponse> allShowtimes = showtimeService.getShowtimesByMovie(movie.getId());

        Set<CinemaResponse> cinemas = allShowtimes.stream()
                .filter(s -> s.getHall() != null)
                .map(s -> s.getHall().getCinema())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        model.addAttribute("movie", movie);
        model.addAttribute("cinemas", cinemas);

        if (cinemaId != null) {
            Set<LocalDate> availableDates = allShowtimes.stream()
                    .filter(s -> s.getHall() != null && s.getHall().getCinema() != null
                            && s.getHall().getCinema().getId().equals(cinemaId))
                    .filter(s -> s.getStartTime() != null)
                    .map(s -> s.getStartTime().toLocalDate())
                    .collect(Collectors.toCollection(TreeSet::new));
            model.addAttribute("dates", availableDates);
        }

        if (cinemaId != null && date != null) {
            LocalDate selectedDate = LocalDate.parse(date);
            List<ShowtimeResponse> filteredShowtimes = allShowtimes.stream()
                    .filter(s -> s.getHall() != null && s.getHall().getCinema() != null
                            && s.getHall().getCinema().getId().equals(cinemaId))
                    .filter(s -> s.getStartTime() != null
                            && s.getStartTime().toLocalDate().equals(selectedDate))
                    .toList();
            model.addAttribute("showtimes", filteredShowtimes);
        }

        model.addAttribute("reviews", reviewService.getMovieReviews(movie.getId()));
        return "movies/detail";
    }

    @GetMapping("/{uuid:[0-9a-fA-F-]{36}}")
    public String movieDetailByUuid(@PathVariable String uuid, Model model) {
        MovieResponse movie = movieService.getMovieByUuid(uuid);
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimeService.getShowtimesByMovie(movie.getId()));
        model.addAttribute("reviews", reviewService.getMovieReviews(movie.getId()));
        return "movies/detail";
    }
}
