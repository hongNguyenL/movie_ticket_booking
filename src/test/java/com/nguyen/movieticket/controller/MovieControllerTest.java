package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.service.MovieService;
import com.nguyen.movieticket.service.ReviewService;
import com.nguyen.movieticket.service.ShowtimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private ShowtimeService showtimeService;

    @Test
    void browseMovies_ShouldReturnMoviesView() throws Exception {
        PageResponse<MovieSummaryResponse> pageResponse = new PageResponse<>(
                List.of(), 0, 12, 0, 0, true);

        when(movieService.getActiveMovies(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/list"))
                .andExpect(model().attributeExists("movies"));
    }

    @Test
    void getMovieDetail_ShouldReturnDetailView_WhenMovieExists() throws Exception {
        String uuid = UUID.randomUUID().toString();
        MovieResponse movieResponse = MovieResponse.builder()
                .id(1L)
                .uuid(uuid)
                .title("Test Movie")
                .build();

        when(movieService.getMovieByUuid(uuid)).thenReturn(movieResponse);
        when(showtimeService.getShowtimesByMovie(1L)).thenReturn(List.of());
        when(reviewService.getMovieReviews(1L)).thenReturn(List.of());

        mockMvc.perform(get("/movies/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/detail"))
                .andExpect(model().attributeExists("movie"))
                .andExpect(model().attributeExists("showtimes"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    void getMovieDetail_ShouldThrowException_WhenMovieNotExists() throws Exception {
        String uuid = UUID.randomUUID().toString();

        when(movieService.getMovieByUuid(uuid))
                .thenThrow(new ResourceNotFoundException("Movie", "uuid", uuid));

        mockMvc.perform(get("/movies/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(view().name("movies/detail"))
                .andExpect(model().attributeDoesNotExist("movie"));
    }
}
