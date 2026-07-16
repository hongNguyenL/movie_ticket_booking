package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Review;
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
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUuid(UUID uuid);

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    Page<Review> findByMovieId(Long movieId, Pageable pageable);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.movie.id = :movieId AND r.isApproved = true")
    BigDecimal getAverageRatingByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId AND r.isApproved = true")
    long countApprovedReviewsByMovieId(@Param("movieId") Long movieId);

    Page<Review> findByIsApprovedFalse(Pageable pageable);
}
