package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ProfileUpdateRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.UserResponse;
import com.nguyen.movieticket.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    UserResponse getProfile(Long userId);

    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);

    PageResponse<UserResponse> getCustomers(Pageable pageable);

    void lockAccount(Long userId);

    void unlockAccount(Long userId);

    Optional<User> findByUsername(String username);
}
