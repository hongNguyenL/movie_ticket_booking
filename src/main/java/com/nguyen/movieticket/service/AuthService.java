package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ChangePasswordRequest;
import com.nguyen.movieticket.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
