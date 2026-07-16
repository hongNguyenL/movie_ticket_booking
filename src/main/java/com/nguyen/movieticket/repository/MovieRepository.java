package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByUuid(UUID uuid);

    Page<Movie> findByIsActiveTrue(Pageable pageable);

    List<Movie> findByIsActiveTrue();

    @Query("SELECT m FROM Movie m WHERE m.isActive = true AND LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.isActive = true AND (:genreId IS NULL OR m.genre.id = :genreId) AND (:language IS NULL OR m.language = :language) AND (:minRating IS NULL OR m.rating >= :minRating)")
    Page<Movie> filterMovies(@Param("genreId") Long genreId, @Param("language") String language, @Param("minRating") BigDecimal minRating, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.isActive = true ORDER BY m.rating DESC")
    List<Movie> findTopRated(Pageable pageable);

    @Query("SELECT m, COUNT(b.id) as bookingCount FROM Movie m JOIN Showtime s ON s.movie = m JOIN Booking b ON b.showtime = s WHERE b.status = 'CONFIRMED' GROUP BY m ORDER BY bookingCount DESC")
    List<Object[]> findMostBookedMovies(Pageable pageable);

    long countByIsActiveTrue();
}
