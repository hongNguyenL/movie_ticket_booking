package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.request.*;
import com.nguyen.movieticket.dto.response.*;
import com.nguyen.movieticket.entity.Hall;
import com.nguyen.movieticket.entity.Seat;
import com.nguyen.movieticket.entity.SeatType;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.ActorMapper;
import com.nguyen.movieticket.mapper.HallMapper;
import com.nguyen.movieticket.mapper.SeatMapper;
import com.nguyen.movieticket.repository.*;
import com.nguyen.movieticket.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final PaymentService paymentService;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final CinemaRepository cinemaRepository;
    private final ReviewRepository reviewRepository;
    private final HallMapper hallMapper;
    private final SeatMapper seatMapper;
    private final ActorMapper actorMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String movies(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MovieSummaryResponse> moviesPage = movieService.getMostBookedMovies(pageable);
        model.addAttribute("movies", moviesPage.content());
        model.addAttribute("totalPages", moviesPage.totalPages());
        model.addAttribute("currentPage", moviesPage.page());
        model.addAttribute("number", moviesPage.page());
        model.addAttribute("first", moviesPage.page() == 0);
        model.addAttribute("last", moviesPage.last());
        model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
        return "admin/movies";
    }

    @GetMapping("/movies/create")
    public String createMovieForm(Model model) {
        model.addAttribute("movie", new CreateMovieRequest());
        model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
        model.addAttribute("directors", directorRepository.findAll());
        model.addAttribute("actors", actorRepository.findAll());
        return "admin/movie-form";
    }

    @PostMapping("/movies/create")
    public String createMovie(@Valid @ModelAttribute("movie") CreateMovieRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
            model.addAttribute("directors", directorRepository.findAll());
            model.addAttribute("actors", actorRepository.findAll());
            return "admin/movie-form";
        }

        try {
            movieService.createMovie(request);
            redirectAttributes.addFlashAttribute("successMessage", "Movie created successfully!");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
            model.addAttribute("directors", directorRepository.findAll());
            model.addAttribute("actors", actorRepository.findAll());
            return "admin/movie-form";
        }
    }

    @GetMapping("/movies/{id}/edit")
    public String editMovieForm(@PathVariable Long id, Model model) {
        MovieResponse movie = movieService.getMovieDetails(id);
        model.addAttribute("movie", movie);
        model.addAttribute("updateMovieRequest", UpdateMovieRequest.builder()
                .title(movie.getTitle())
                .description(movie.getDescription())
                .duration(movie.getDuration())
                .releaseDate(movie.getReleaseDate())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .language(movie.getLanguage())
                .genreId(movie.getGenreId())
                .directorId(movie.getDirectorId())
                .actorIds(movie.getActors().stream().map(ActorResponse::getId).collect(Collectors.toSet()))
                .build());
        model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
        model.addAttribute("directors", directorRepository.findAll());
        model.addAttribute("actors", actorRepository.findAll());
        return "admin/movie-form";
    }

    @PostMapping("/movies/{id}/edit")
    public String updateMovie(@PathVariable Long id,
                              @Valid @ModelAttribute("updateMovieRequest") UpdateMovieRequest request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
            model.addAttribute("directors", directorRepository.findAll());
            model.addAttribute("actors", actorRepository.findAll());
            return "admin/movie-form";
        }

        try {
            movieService.updateMovie(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Movie updated successfully!");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
            model.addAttribute("directors", directorRepository.findAll());
            model.addAttribute("actors", actorRepository.findAll());
            return "admin/movie-form";
        }
    }

    @PostMapping("/movies/delete")
    public String deleteMovie(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMessage", "Movie deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/genres")
    public String genres(Model model) {
        model.addAttribute("genres", genreRepository.findAllByOrderByNameAsc());
        return "admin/genres";
    }

    @GetMapping("/actors")
    public String actors(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         Model model) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("actors", actorRepository.findAll(pageable));
        return "admin/actors";
    }

    @GetMapping("/directors")
    public String directors(Model model) {
        model.addAttribute("directors", directorRepository.findAll());
        return "admin/directors";
    }

    @GetMapping("/cinemas")
    public String cinemas(Model model) {
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "admin/cinemas";
    }

    @GetMapping("/cinemas/create")
    public String createCinemaForm(Model model) {
        model.addAttribute("cinema", new CreateCinemaRequest());
        return "admin/cinema-form";
    }

    @PostMapping("/cinemas/create")
    public String createCinema(@Valid @ModelAttribute("cinema") CreateCinemaRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/cinema-form";
        }

        try {
            cinemaService.createCinema(request);
            redirectAttributes.addFlashAttribute("successMessage", "Cinema created successfully!");
            return "redirect:/admin/cinemas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "admin/cinema-form";
        }
    }

    @GetMapping("/cinemas/{id}/edit")
    public String editCinemaForm(@PathVariable Long id, Model model) {
        CinemaResponse cinema = cinemaService.getCinemaById(id);
        model.addAttribute("cinema", cinema);
        return "admin/cinema-form";
    }

    @PostMapping("/cinemas/{id}/edit")
    public String updateCinema(@PathVariable Long id,
                               @Valid @ModelAttribute("cinema") UpdateCinemaRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/cinema-form";
        }

        try {
            cinemaService.updateCinema(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cinema updated successfully!");
            return "redirect:/admin/cinemas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "admin/cinema-form";
        }
    }

    @PostMapping("/cinemas/delete")
    public String deleteCinema(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            cinemaService.deleteCinema(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cinema deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/cinemas";
    }

    @GetMapping("/cinemas/{id}/halls")
    public String halls(@PathVariable Long id, Model model) {
        CinemaResponse cinema = cinemaService.getCinemaById(id);
        List<HallResponse> halls = cinemaService.getHallsByCinema(id);
        model.addAttribute("cinema", cinema);
        model.addAttribute("halls", halls);
        return "admin/halls";
    }

    @GetMapping("/halls/create/{cinemaId}")
    public String createHallForm(@PathVariable Long cinemaId, Model model) {
        CinemaResponse cinema = cinemaService.getCinemaById(cinemaId);
        model.addAttribute("cinema", cinema);
        model.addAttribute("hall", CreateHallRequest.builder()
                .cinemaId(cinemaId)
                .build());
        return "admin/hall-form";
    }

    @PostMapping("/halls/create")
    public String createHall(@Valid @ModelAttribute("hall") CreateHallRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cinema", cinemaService.getCinemaById(request.getCinemaId()));
            return "admin/hall-form";
        }

        try {
            var cinema = cinemaRepository.findById(request.getCinemaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", request.getCinemaId()));
            Hall hall = Hall.builder()
                    .name(request.getName())
                    .capacity(request.getCapacity())
                    .description(request.getDescription())
                    .cinema(cinema)
                    .isActive(true)
                    .build();
            hallRepository.save(hall);
            redirectAttributes.addFlashAttribute("successMessage", "Hall created successfully!");
            return "redirect:/admin/cinemas/" + request.getCinemaId() + "/halls";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/halls/create/" + request.getCinemaId();
        }
    }

    @GetMapping("/halls/{id}/edit")
    public String editHallForm(@PathVariable Long id, Model model) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
        model.addAttribute("hall", hallMapper.toResponse(hall));
        model.addAttribute("updateHallRequest", UpdateHallRequest.builder()
                .name(hall.getName())
                .capacity(hall.getCapacity())
                .description(hall.getDescription())
                .isActive(hall.getIsActive())
                .build());
        return "admin/hall-form";
    }

    @PostMapping("/halls/{id}/edit")
    public String updateHall(@PathVariable Long id,
                             @Valid @ModelAttribute("updateHallRequest") UpdateHallRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Hall hall = hallRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
            model.addAttribute("hall", hallMapper.toResponse(hall));
            return "admin/hall-form";
        }

        try {
            Hall hall = hallRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
            if (request.getName() != null) hall.setName(request.getName());
            if (request.getCapacity() != null) hall.setCapacity(request.getCapacity());
            if (request.getDescription() != null) hall.setDescription(request.getDescription());
            if (request.getIsActive() != null) hall.setIsActive(request.getIsActive());
            hallRepository.save(hall);
            redirectAttributes.addFlashAttribute("successMessage", "Hall updated successfully!");
            return "redirect:/admin/cinemas/" + hall.getCinema().getId() + "/halls";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/halls/" + id + "/edit";
        }
    }

    @PostMapping("/halls/delete")
    public String deleteHall(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            Hall hall = hallRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
            Long cinemaId = hall.getCinema().getId();
            hall.setIsActive(false);
            hallRepository.save(hall);
            redirectAttributes.addFlashAttribute("successMessage", "Hall deleted successfully!");
            return "redirect:/admin/cinemas/" + cinemaId + "/halls";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/cinemas";
        }
    }

    @GetMapping("/halls/{id}/seats")
    public String seatLayoutForm(@PathVariable Long id, Model model) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
        List<SeatResponse> seats = seatMapper.toResponseList(seatRepository.findByHallIdOrderByRowLabelAscSeatNumberAsc(id));
        model.addAttribute("hall", hallMapper.toResponse(hall));
        model.addAttribute("seats", seats);
        model.addAttribute("seatLayoutRequest", new SeatLayoutRequest());
        return "admin/seat-layout";
    }

    @PostMapping("/halls/{id}/seats")
    public String saveSeatLayout(@PathVariable Long id,
                                 @Valid @ModelAttribute("seatLayoutRequest") SeatLayoutRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "redirect:/admin/halls/" + id + "/seats";
        }

        try {
            Hall hall = hallRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Hall", "id", id));
            List<Seat> existingSeats = seatRepository.findByHallIdOrderByRowLabelAscSeatNumberAsc(id);
            seatRepository.deleteAll(existingSeats);
            for (SeatLayoutRequest.SeatEntry entry : request.getSeats()) {
                Seat seat = Seat.builder()
                        .hall(hall)
                        .rowLabel(entry.getRowLabel())
                        .seatNumber(entry.getSeatNumber())
                        .seatType(entry.getSeatType() != null ? SeatType.valueOf(entry.getSeatType()) : SeatType.STANDARD)
                        .isActive(true)
                        .build();
                seatRepository.save(seat);
            }
            hall.setCapacity(request.getSeats().size());
            hallRepository.save(hall);
            redirectAttributes.addFlashAttribute("successMessage", "Seat layout saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/halls/" + id + "/seats";
    }

    @GetMapping("/showtimes")
    public String showtimes(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ShowtimeResponse> showtimesPage = showtimeService.getShowtimes(pageable);
        model.addAttribute("showtimes", showtimesPage.content());
        model.addAttribute("totalPages", showtimesPage.totalPages());
        model.addAttribute("currentPage", showtimesPage.page());
        model.addAttribute("number", showtimesPage.page());
        model.addAttribute("first", showtimesPage.page() == 0);
        model.addAttribute("last", showtimesPage.last());
        model.addAttribute("movies", movieService.getTopRatedMovies(100));
        model.addAttribute("cinemas", cinemaService.getAllCinemas());
        return "admin/showtimes";
    }

    @GetMapping("/showtimes/create")
    public String createShowtimeForm(Model model) {
        model.addAttribute("showtime", new ShowtimeRequest());
        model.addAttribute("movies", movieService.getTopRatedMovies(50));
        model.addAttribute("halls", hallRepository.findAll());
        return "admin/showtime-form";
    }

    @PostMapping("/showtimes/create")
    public String createShowtime(@Valid @ModelAttribute("showtime") ShowtimeRequest request,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("movies", movieService.getTopRatedMovies(50));
            model.addAttribute("halls", hallRepository.findAll());
            return "admin/showtime-form";
        }

        try {
            showtimeService.createShowtime(request);
            redirectAttributes.addFlashAttribute("successMessage", "Showtime created successfully!");
            return "redirect:/admin/showtimes";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("movies", movieService.getTopRatedMovies(50));
            model.addAttribute("halls", hallRepository.findAll());
            return "admin/showtime-form";
        }
    }

    @PostMapping("/showtimes/delete")
    public String deleteShowtime(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            showtimeService.deleteShowtime(id);
            redirectAttributes.addFlashAttribute("successMessage", "Showtime deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/showtimes";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String keyword,
                           Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BookingResponse> bookingsPage;
        if (keyword != null && !keyword.isBlank()) {
            bookingsPage = bookingService.searchBookings(keyword, pageable);
        } else {
            bookingsPage = bookingService.getAllBookings(pageable);
        }
        model.addAttribute("bookings", bookingsPage.content());
        model.addAttribute("totalPages", bookingsPage.totalPages());
        model.addAttribute("currentPage", bookingsPage.page());
        model.addAttribute("number", bookingsPage.page());
        model.addAttribute("first", bookingsPage.page() == 0);
        model.addAttribute("last", bookingsPage.last());
        model.addAttribute("keyword", keyword);
        return "admin/bookings";
    }

    @PostMapping("/bookings/{reference}/cancel")
    public String cancelBooking(@PathVariable String reference,
                                @RequestParam(required = false, defaultValue = "Cancelled by admin") String reason,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(reference, reason);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<UserResponse> customersPage = userService.getCustomers(pageable);
        model.addAttribute("customers", customersPage.content());
        model.addAttribute("totalPages", customersPage.totalPages());
        model.addAttribute("currentPage", customersPage.page());
        model.addAttribute("number", customersPage.page());
        model.addAttribute("first", customersPage.page() == 0);
        model.addAttribute("last", customersPage.last());
        return "admin/customers";
    }

    @PostMapping("/customers/{id}/lock")
    public String lockCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.lockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account locked successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @PostMapping("/customers/{id}/unlock")
    public String unlockCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unlockAccount(id);
            redirectAttributes.addFlashAttribute("successMessage", "Account unlocked successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/reviews")
    public String reviews(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ReviewResponse> reviewsPage = reviewService.getPendingReviews(pageable);
        model.addAttribute("reviews", reviewsPage.content());
        model.addAttribute("totalPages", reviewsPage.totalPages());
        model.addAttribute("currentPage", reviewsPage.page());
        model.addAttribute("number", reviewsPage.page());
        model.addAttribute("first", reviewsPage.page() == 0);
        model.addAttribute("last", reviewsPage.last());
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.approveReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/reviews/delete")
    public String deleteReview(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            var review = reviewRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
            reviewRepository.delete(review);
            redirectAttributes.addFlashAttribute("successMessage", "Review deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "admin/reports";
    }
}
