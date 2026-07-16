package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.RegisterRequest;
import com.nguyen.movieticket.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void showLoginForm_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void showRegisterForm_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void register_ShouldRedirect_WhenValid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("password", "Password1@")
                        .param("confirmPassword", "Password1@")
                        .param("fullName", "New User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void register_ShouldReturnForm_WhenInvalid() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("username", "")
                        .param("email", "invalid")
                        .param("password", "short")
                        .param("confirmPassword", "different")
                        .param("fullName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_ShouldReturnForm_WhenServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Registration error occurred"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/auth/register")
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("password", "Password1@")
                        .param("confirmPassword", "Password1@")
                        .param("fullName", "New User"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }
}
