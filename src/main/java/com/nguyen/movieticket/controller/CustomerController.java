package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.ChangePasswordRequest;
import com.nguyen.movieticket.dto.request.ProfileUpdateRequest;
import com.nguyen.movieticket.dto.response.BookingResponse;
import com.nguyen.movieticket.dto.response.BookingSummaryResponse;
import com.nguyen.movieticket.dto.response.UserResponse;
import com.nguyen.movieticket.security.CustomUserDetails;
import com.nguyen.movieticket.service.AuthService;
import com.nguyen.movieticket.service.BookingService;
import com.nguyen.movieticket.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final UserService userService;
    private final AuthService authService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser,
                            Model model) {
        UserResponse profile = userService.getProfile(currentUser.getId());
        List<BookingSummaryResponse> recentBookings = bookingService.getUserBookingHistory(currentUser.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("recentBookings", recentBookings.stream().limit(5).toList());
        return "customer/dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails currentUser,
                          Model model) {
        UserResponse profile = userService.getProfile(currentUser.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("profileUpdateRequest", ProfileUpdateRequest.builder()
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .avatar(profile.getAvatar())
                .build());
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/profile";
        }

        try {
            userService.updateProfile(currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "customer/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/change-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
            return "customer/change-password";
        }

        try {
            authService.changePassword(currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/profile";
    }

    @GetMapping("/bookings")
    public String bookings(@AuthenticationPrincipal CustomUserDetails currentUser,
                           Model model) {
        List<BookingSummaryResponse> bookings = bookingService.getUserBookingHistory(currentUser.getId());
        model.addAttribute("bookings", bookings);
        return "customer/bookings";
    }

    @GetMapping("/bookings/{reference}")
    public String bookingDetail(@PathVariable String reference,
                                Model model) {
        BookingResponse booking = bookingService.getBookingByReference(reference);
        model.addAttribute("booking", booking);
        return "customer/booking-detail";
    }

    @PostMapping("/bookings/{reference}/cancel")
    public String cancelBooking(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @PathVariable String reference,
                                @RequestParam(required = false, defaultValue = "Cancelled by customer") String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(reference, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer/bookings";
    }

    @GetMapping("/bookings/{reference}/ticket")
    public String viewTicket(@PathVariable String reference,
                             Model model) {
        BookingResponse booking = bookingService.getBookingByReference(reference);
        model.addAttribute("booking", booking);
        return "customer/ticket";
    }
}
