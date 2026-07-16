package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.ProfileUpdateRequest;
import com.nguyen.movieticket.dto.response.PageResponse;
import com.nguyen.movieticket.dto.response.UserResponse;
import com.nguyen.movieticket.entity.User;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.UserMapper;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user ID: {}", userId);
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getCustomers(Pageable pageable) {
        Page<User> users = userRepository.findActiveCustomers(pageable);
        List<UserResponse> content = users.getContent().stream()
                .map(userMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content, users.getNumber(), users.getSize(),
                users.getTotalElements(), users.getTotalPages(), users.isLast()
        );
    }

    @Override
    @Transactional
    public void lockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setAccountLocked(true);
        userRepository.save(user);
        log.warn("Account locked for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setAccountLocked(false);
        userRepository.save(user);
        log.info("Account unlocked for user ID: {}", userId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
