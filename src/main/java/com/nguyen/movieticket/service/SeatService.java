package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.response.SeatResponse;

import java.util.List;

public interface SeatService {
    List<SeatResponse> getSeatsByHall(Long hallId);
}
