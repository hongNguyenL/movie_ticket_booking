package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private DirectorRepository directorRepository;

    private User user;
    private Showtime showtime;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build());

        Genre genre = genreRepository.save(Genre.builder().name("Action").slug("action").build());
        Director director = directorRepository.save(Director.builder().name("Director").build());
        Movie movie = movieRepository.save(Movie.builder()
                .title("Test Movie")
                .genre(genre)
                .director(director)
                .duration(120)
                .isActive(true)
                .build());

        Cinema cinema = cinemaRepository.save(Cinema.builder().name("Test Cinema").build());
        Hall hall = hallRepository.save(Hall.builder().name("Hall 1").cinema(cinema).build());

        showtime = showtimeRepository.save(Showtime.builder()
                .movie(movie)
                .hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusMinutes(120))
                .ticketPrice(new BigDecimal("10.00"))
                .isActive(true)
                .build());
    }

    @Test
    void findByBookingReference_ShouldReturnBooking() {
        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference("BK-TEST123")
                .totalPrice(new BigDecimal("10.00"))
                .status(BookingStatus.PENDING)
                .bookedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build());

        Optional<Booking> found = bookingRepository.findByBookingReference("BK-TEST123");

        assertThat(found).isPresent();
        assertThat(found.get().getBookingReference()).isEqualTo("BK-TEST123");
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findExpiredBookings_ShouldReturnExpiredOnes() {
        LocalDateTime past = LocalDateTime.now().minusMinutes(5);
        bookingRepository.save(Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference("BK-EXPIRED1")
                .totalPrice(new BigDecimal("10.00"))
                .status(BookingStatus.PENDING)
                .bookedAt(past)
                .expiresAt(past)
                .build());

        bookingRepository.save(Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference("BK-ACTIVE1")
                .totalPrice(new BigDecimal("10.00"))
                .status(BookingStatus.PENDING)
                .bookedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build());

        List<Booking> expired = bookingRepository.findExpiredBookings(
                "PENDING", LocalDateTime.now());

        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getBookingReference()).isEqualTo("BK-EXPIRED1");
    }

    @Test
    void findActiveBookingsByShowtime_ShouldReturnActiveBookings() {
        bookingRepository.save(Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference("BK-ACTIVE2")
                .totalPrice(new BigDecimal("10.00"))
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build());

        bookingRepository.save(Booking.builder()
                .user(user)
                .showtime(showtime)
                .bookingReference("BK-CANCELLED")
                .totalPrice(new BigDecimal("10.00"))
                .status(BookingStatus.CANCELLED)
                .bookedAt(LocalDateTime.now())
                .build());

        List<Booking> activeBookings = bookingRepository.findActiveBookingsByShowtime(showtime.getId());

        assertThat(activeBookings).hasSize(1);
        assertThat(activeBookings.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
