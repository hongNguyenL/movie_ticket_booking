package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Booking;
import com.nguyen.movieticket.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import com.nguyen.movieticket.entity.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByUuid(UUID uuid);

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("SELECT b FROM Booking b JOIN FETCH b.showtime JOIN FETCH b.user WHERE b.user.id = :userId ORDER BY b.bookedAt DESC")
    List<Booking> findByUserIdOrderByBookedAtDesc(@Param("userId") Long userId);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findAllByOrderByBookedAtDesc(Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.expiresAt < :now")
    List<Booking> findExpiredBookings(@Param("status") BookingStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.showtime.id = :showtimeId AND b.status IN ('CONFIRMED', 'PENDING')")
    List<Booking> findActiveBookingsByShowtime(@Param("showtimeId") Long showtimeId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.showtime.movie.id = :movieId AND b.status = 'CONFIRMED'")
    long countConfirmedBookingsByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT COALESCE(SUM(bs.price), 0) FROM BookingSeat bs WHERE bs.booking.showtime.id = :showtimeId AND bs.booking.status IN ('CONFIRMED', 'PENDING')")
    BigDecimal sumBookedSeatPricesByShowtime(@Param("showtimeId") Long showtimeId);

    @Query("SELECT b FROM Booking b WHERE LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.showtime.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.user.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Booking> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByStatusAndShowtimeStartTimeBetween(BookingStatus status, LocalDateTime start, LocalDateTime end);

    long countByShowtimeStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.user.id = :userId AND b.showtime.movie.id = :movieId AND b.status = 'CONFIRMED'")
    boolean existsConfirmedBookingByUserAndMovie(@Param("userId") Long userId, @Param("movieId") Long movieId);
}
