package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.ChangePasswordRequest;
import com.nguyen.movieticket.dto.request.RegisterRequest;
import com.nguyen.movieticket.entity.Role;
import com.nguyen.movieticket.entity.User;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.DuplicateResourceException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.repository.RoleRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldCreateUser_WhenValidData() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .fullName("New User")
                .phone("1234567890")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER"))
                .thenReturn(Optional.of(Role.builder().id(1L).name("ROLE_CUSTOMER").build()));
        when(passwordEncoder.encode("Password1@")).thenReturn("encodedPassword");

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getAccountLocked()).isFalse();
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("new@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@example.com")
                .password("Password1@")
                .confirmPassword("Password1@")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordCorrect() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .password("oldEncodedPassword")
                .build();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("oldPassword")
                .newPassword("NewPassword1@")
                .confirmPassword("NewPassword1@")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1@")).thenReturn("newEncodedPassword");

        authService.changePassword(userId, request);

        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordWrong() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .password("oldEncodedPassword")
                .build();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrongPassword")
                .newPassword("NewPassword1@")
                .confirmPassword("NewPassword1@")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "oldEncodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }
}
