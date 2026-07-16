package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Genre;
import com.nguyen.movieticket.entity.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    private Genre actionGenre;
    private Genre dramaGenre;

    @BeforeEach
    void setUp() {
        actionGenre = genreRepository.save(Genre.builder().name("Action").slug("action").build());
        dramaGenre = genreRepository.save(Genre.builder().name("Drama").slug("drama").build());

        movieRepository.save(Movie.builder()
                .title("The Dark Knight")
                .genre(actionGenre)
                .language("English")
                .rating(new BigDecimal("8.5"))
                .isActive(true)
                .duration(152)
                .build());

        movieRepository.save(Movie.builder()
                .title("Inception")
                .genre(actionGenre)
                .language("English")
                .rating(new BigDecimal("8.8"))
                .isActive(true)
                .duration(148)
                .build());

        movieRepository.save(Movie.builder()
                .title("Parasite")
                .genre(dramaGenre)
                .language("Korean")
                .rating(new BigDecimal("8.6"))
                .isActive(true)
                .duration(132)
                .build());

        movieRepository.save(Movie.builder()
                .title("Inactive Movie")
                .genre(dramaGenre)
                .language("English")
                .isActive(false)
                .duration(120)
                .build());
    }

    @Test
    void searchByTitle_ShouldReturnMatchingMovies() {
        Page<Movie> results = movieRepository.searchByTitle("dark", PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("The Dark Knight");
    }

    @Test
    void filterMovies_ShouldFilterByGenreAndLanguage() {
        Page<Movie> results = movieRepository.filterMovies(
                actionGenre.getId(), "English", null, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("The Dark Knight", "Inception");
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveMovies() {
        Page<Movie> results = movieRepository.findByIsActiveTrue(PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent()).extracting(Movie::getTitle)
                .containsExactlyInAnyOrder("The Dark Knight", "Inception", "Parasite");
    }
}
