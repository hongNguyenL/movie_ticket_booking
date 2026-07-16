package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ReviewRequest;
import com.nguyen.movieticket.dto.response.ReviewResponse;
import com.nguyen.movieticket.entity.Movie;
import com.nguyen.movieticket.entity.Review;
import com.nguyen.movieticket.entity.User;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.UnauthorizedException;
import com.nguyen.movieticket.mapper.ReviewMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.MovieRepository;
import com.nguyen.movieticket.repository.ReviewRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void createReview_ShouldCreate_WhenUserAttended() {
        Long userId = 1L;
        Long movieId = 1L;

        User user = User.builder().id(userId).username("testuser").build();
        Movie movie = Movie.builder().id(movieId).title("Test Movie").build();

        ReviewRequest request = ReviewRequest.builder()
                .movieId(movieId)
                .rating(4)
                .comment("Great movie!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(false);
        when(bookingRepository.existsConfirmedBookingByUserAndMovie(userId, movieId)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(
                ReviewResponse.builder().id(1L).rating(4).comment("Great movie!").build());

        ReviewResponse result = reviewService.createReview(userId, request);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(4);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenUserNotAttended() {
        Long userId = 1L;
        Long movieId = 1L;

        User user = User.builder().id(userId).username("testuser").build();
        Movie movie = Movie.builder().id(movieId).title("Test Movie").build();

        ReviewRequest request = ReviewRequest.builder()
                .movieId(movieId)
                .rating(4)
                .comment("Great movie!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(false);
        when(bookingRepository.existsConfirmedBookingByUserAndMovie(userId, movieId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createReview(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not attended");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenAlreadyReviewed() {
        Long userId = 1L;
        Long movieId = 1L;

        User user = User.builder().id(userId).username("testuser").build();
        Movie movie = Movie.builder().id(movieId).title("Test Movie").build();

        ReviewRequest request = ReviewRequest.builder()
                .movieId(movieId)
                .rating(4)
                .comment("Great movie!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void updateReview_ShouldUpdate_WhenOwnReview() {
        Long reviewId = 1L;
        Long userId = 1L;

        User user = User.builder().id(userId).username("testuser").build();
        Review review = Review.builder()
                .id(reviewId)
                .user(user)
                .rating(3)
                .comment("Old comment")
                .build();

        ReviewRequest request = ReviewRequest.builder()
                .movieId(1L)
                .rating(5)
                .comment("Updated comment")
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toResponse(any(Review.class))).thenReturn(
                ReviewResponse.builder().id(reviewId).rating(5).comment("Updated comment").build());

        ReviewResponse result = reviewService.updateReview(reviewId, userId, request);

        assertThat(result).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Updated comment");
    }

    @Test
    void updateReview_ShouldThrowException_WhenNotOwnReview() {
        Long reviewId = 1L;
        Long userId = 1L;
        Long otherUserId = 2L;

        User otherUser = User.builder().id(otherUserId).username("otheruser").build();
        Review review = Review.builder()
                .id(reviewId)
                .user(otherUser)
                .rating(3)
                .comment("Old comment")
                .build();

        ReviewRequest request = ReviewRequest.builder()
                .movieId(1L)
                .rating(5)
                .comment("Updated comment")
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.updateReview(reviewId, userId, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("own reviews");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_ShouldDelete_WhenOwnReview() {
        Long reviewId = 1L;
        Long userId = 1L;

        User user = User.builder().id(userId).username("testuser").build();
        Review review = Review.builder()
                .id(reviewId)
                .user(user)
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, userId);

        verify(reviewRepository).delete(review);
    }
}
