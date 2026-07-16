package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.ReviewRequest;
import com.nguyen.movieticket.security.CustomUserDetails;
import com.nguyen.movieticket.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public String createReview(@AuthenticationPrincipal CustomUserDetails currentUser,
                               @Valid @ModelAttribute("reviewRequest") ReviewRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getFieldError() != null ? bindingResult.getFieldError().getDefaultMessage() : "Invalid review data");
            return "redirect:/movies/" + request.getMovieId();
        }

        try {
            reviewService.createReview(currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted! It will be visible after approval.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/movies/" + request.getMovieId();
    }

    @PostMapping("/{id}/edit")
    public String editReview(@AuthenticationPrincipal CustomUserDetails currentUser,
                             @PathVariable Long id,
                             @Valid @ModelAttribute("reviewRequest") ReviewRequest request,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid review data");
            return "redirect:/movies/" + request.getMovieId();
        }

        try {
            reviewService.updateReview(id, currentUser.getId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Review updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/movies/" + request.getMovieId();
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@AuthenticationPrincipal CustomUserDetails currentUser,
                               @PathVariable Long id,
                               @RequestParam(required = false) Long movieId,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/movies/" + movieId;
    }
}
