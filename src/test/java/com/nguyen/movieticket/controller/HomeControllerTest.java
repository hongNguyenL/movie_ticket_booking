package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.service.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    void home_ShouldReturnHomeView() throws Exception {
        when(movieService.getTopRatedMovies(6)).thenReturn(List.of());
        when(movieService.getTopRatedMovies(10)).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void home_ShouldHaveMoviesAttribute() throws Exception {
        MovieSummaryResponse movie = MovieSummaryResponse.builder().id(1L).title("Test Movie").build();
        when(movieService.getTopRatedMovies(6)).thenReturn(List.of(movie));
        when(movieService.getTopRatedMovies(10)).thenReturn(List.of(movie, movie));

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("featuredMovies"))
                .andExpect(model().attributeExists("topRatedMovies"));
    }

    @Test
    void about_ShouldReturnAboutView() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }
}
