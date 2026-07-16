package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.ReviewRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.ReviewResponse;
import com.nguyen.movieticket.entity.BookingStatus;
import com.nguyen.movieticket.entity.Review;
import com.nguyen.movieticket.entity.User;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.exception.UnauthorizedException;
import com.nguyen.movieticket.mapper.ReviewMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.repository.ReviewRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        var movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        if (reviewRepository.existsByUserIdAndMovieId(userId, request.getMovieId())) {
            throw new BadRequestException("User has already reviewed this movie");
        }

        boolean hasAttended = bookingRepository.existsConfirmedBookingByUserAndMovie(userId, request.getMovieId());
        if (!hasAttended) {
            throw new BadRequestException("Cannot review a movie you have not attended");
        }

        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .rating(request.getRating())
                .comment(request.getComment())
                .isApproved(false)
                .build();

        review = reviewRepository.save(review);
        log.info("Review created by user {} for movie {}", userId, request.getMovieId());
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);
        log.info("Review updated: {}", reviewId);
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        log.info("Review deleted: {}", reviewId);
    }

    @Override
    public List<ReviewResponse> getMovieReviews(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
        return reviewMapper.toResponseList(reviews);
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setIsApproved(true);
        reviewRepository.save(review);
        log.info("Review approved: {}", reviewId);
    }

    @Override
    public PageResponse<ReviewResponse> getPendingReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByIsApprovedFalse(pageable);
        List<ReviewResponse> content = reviews.getContent().stream()
                .map(reviewMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content, reviews.getNumber(), reviews.getSize(),
                reviews.getTotalElements(), reviews.getTotalPages(), reviews.isLast()
        );
    }
}
