package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ReviewRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(Long userId, ReviewRequest request);

    ReviewResponse updateReview(Long reviewId, Long userId, ReviewRequest request);

    void deleteReview(Long reviewId, Long userId);

    List<ReviewResponse> getMovieReviews(Long movieId);

    void approveReview(Long reviewId);

    PageResponse<ReviewResponse> getPendingReviews(Pageable pageable);
}
