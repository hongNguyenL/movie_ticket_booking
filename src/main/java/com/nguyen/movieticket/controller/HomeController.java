package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<MovieSummaryResponse> featuredMovies = movieService.getTopRatedMovies(6);
        List<MovieSummaryResponse> topRated = movieService.getTopRatedMovies(10);
        List<MovieSummaryResponse> nowShowing = movieService.getTopRatedMovies(8);
        model.addAttribute("featuredMovies", featuredMovies);
        model.addAttribute("topRated", topRated);
        model.addAttribute("nowShowing", nowShowing);
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
