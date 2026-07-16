package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    Optional<Showtime> findByUuid(UUID uuid);

    List<Showtime> findByMovieIdAndIsActiveTrue(Long movieId);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.hall.cinema.id = :cinemaId AND DATE(s.startTime) = :date AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findByMovieAndCinemaAndDate(@Param("movieId") Long movieId, @Param("cinemaId") Long cinemaId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.hall WHERE s.startTime >= :start AND s.startTime < :end AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findShowtimesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(s) > 0 FROM Showtime s WHERE s.hall.id = :hallId AND s.isActive = true AND s.endTime > :startTime AND s.startTime < :endTime")
    boolean existsOverlappingShowtime(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Showtime s WHERE DATE(s.startTime) = :date AND s.isActive = true ORDER BY s.startTime")
    List<Showtime> findByDate(@Param("date") LocalDate date);
}
