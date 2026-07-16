package com.nguyen.movieticket.controller;

import com.nguyen.movieticket.dto.response.SeatResponse;
import com.nguyen.movieticket.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final BookingService bookingService;

    @GetMapping("/{showtimeId}")
    @ResponseBody
    public List<SeatResponse> getSeatsForShowtime(@PathVariable Long showtimeId) {
        return bookingService.getAvailableSeats(showtimeId);
    }
}
