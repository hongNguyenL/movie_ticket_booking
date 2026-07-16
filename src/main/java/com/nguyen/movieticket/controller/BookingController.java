package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.BookingRequest;
import com.nguyen.movieticket.dto.request.PaymentRequest;
import com.nguyen.movieticket.dto.response.*;
import com.nguyen.movieticket.security.CustomUserDetails;
import com.nguyen.movieticket.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/select-movie")
    public String selectMovie(Model model) {
        List<MovieSummaryResponse> movies = movieService.getTopRatedMovies(20);
        model.addAttribute("movies", movies);
        return "booking/select-movie";
    }

    @GetMapping("/select-cinema")
    public String selectCinema(@RequestParam Long movieId, Model model) {
        MovieResponse movie = movieService.getMovieDetails(movieId);
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByMovie(movieId);
        Set<CinemaResponse> cinemas = showtimes.stream()
                .map(s -> s.getHall().getCinema())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        model.addAttribute("movie", movie);
        model.addAttribute("cinemas", cinemas);
        return "booking/select-cinema";
    }

    @GetMapping("/select-date")
    public String selectDate(@RequestParam Long movieId,
                             @RequestParam Long cinemaId,
                             Model model) {
        MovieResponse movie = movieService.getMovieDetails(movieId);
        CinemaResponse cinema = cinemaService.getCinemaById(cinemaId);
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByMovie(movieId);
        Set<LocalDate> availableDates = showtimes.stream()
                .filter(s -> s.getHall().getCinema().getId().equals(cinemaId))
                .map(s -> s.getStartTime().toLocalDate())
                .filter(d -> !d.isBefore(LocalDate.now()))
                .collect(Collectors.toCollection(TreeSet::new));
        model.addAttribute("movie", movie);
        model.addAttribute("cinema", cinema);
        model.addAttribute("availableDates", availableDates);
        return "booking/select-date";
    }

    @GetMapping("/select-seats")
    public String selectSeats(@RequestParam Long movieId,
                              @RequestParam Long cinemaId,
                              @RequestParam Long showtimeId) {
        return "redirect:/booking/seats/" + showtimeId;
    }

    @GetMapping("/seats/{showtimeId}")
    public String selectSeats(@PathVariable Long showtimeId, Model model) {
        ShowtimeResponse showtime = showtimeService.getShowtimeById(showtimeId);
        List<SeatResponse> seats = bookingService.getAvailableSeats(showtimeId);

        int maxRow = seats.stream()
                .filter(s -> s.getRowLabel() != null)
                .mapToInt(s -> s.getRowLabel().charAt(0) - 'A' + 1)
                .max().orElse(0);
        int maxCol = seats.stream()
                .mapToInt(SeatResponse::getSeatNumber)
                .max().orElse(0);

        List<String> vipSeats = seats.stream()
                .filter(s -> "VIP".equals(s.getSeatType()) || "COUPLE".equals(s.getSeatType()))
                .filter(s -> s.getRowLabel() != null)
                .map(s -> s.getRowLabel() + s.getSeatNumber())
                .toList();

        List<String> bookedSeats = seats.stream()
                .filter(s -> !s.isAvailable())
                .filter(s -> s.getRowLabel() != null)
                .map(s -> s.getRowLabel() + s.getSeatNumber())
                .toList();

        Map<String, List<SeatResponse>> seatsByRow = seats.stream()
                .filter(s -> s.getRowLabel() != null)
                .collect(Collectors.groupingBy(SeatResponse::getRowLabel,
                        LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("showtime", showtime);
        model.addAttribute("movie", showtime.getMovie());
        model.addAttribute("cinema", showtime.getHall() != null ? showtime.getHall().getCinema() : null);
        model.addAttribute("seats", seats);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("seatRows", maxRow);
        model.addAttribute("seatCols", maxCol);
        model.addAttribute("seatPrice", showtime.getTicketPrice() != null ? showtime.getTicketPrice() : BigDecimal.ZERO);
        model.addAttribute("vipSeatsJson", vipSeats.toString());
        model.addAttribute("bookedSeatsJson", bookedSeats.toString());
        model.addAttribute("tempBookedSeatsJson", "[]");
        return "booking/seat-selection";
    }

    @PostMapping("/create")
    public String createBooking(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @Valid @ModelAttribute("bookingRequest") BookingRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one seat.");
            return "redirect:/booking/seats/" + request.getShowtimeId();
        }

        try {
            BookingResponse booking = bookingService.createBooking(currentUser.getId(), request);
            redirectAttributes.addAttribute("reference", booking.getBookingReference());
            return "redirect:/booking/checkout/{reference}";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/booking/seats/" + request.getShowtimeId();
        }
    }

    @GetMapping("/checkout/{reference}")
    public String checkout(@PathVariable String reference, Model model) {
        BookingResponse booking = bookingService.getBookingByReference(reference);
        model.addAttribute("booking", booking);
        model.addAttribute("paymentRequest", PaymentRequest.builder()
                .bookingId(booking.getId())
                .build());
        return "booking/checkout";
    }

    @PostMapping("/pay")
    public String processPayment(@AuthenticationPrincipal CustomUserDetails currentUser,
                                 @RequestParam String reference,
                                 @Valid @ModelAttribute("paymentRequest") PaymentRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment method is required.");
            return "redirect:/booking/checkout/" + reference;
        }

        try {
            paymentService.processPayment(request);
            bookingService.confirmBooking(reference);
            redirectAttributes.addFlashAttribute("successMessage", "Payment successful! Enjoy your movie.");
            return "redirect:/customer/bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/booking/checkout/" + reference;
        }
    }
}
