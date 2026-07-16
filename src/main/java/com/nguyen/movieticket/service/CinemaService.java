package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.CreateCinemaRequest;
import com.nguyen.movieticket.dto.request.UpdateCinemaRequest;
import com.nguyen.movieticket.dto.response.CinemaResponse;
import com.nguyen.movieticket.dto.response.HallResponse;

import java.util.List;

public interface CinemaService {
    List<CinemaResponse> getAllCinemas();

    CinemaResponse getCinemaById(Long id);

    CinemaResponse createCinema(CreateCinemaRequest request);

    CinemaResponse updateCinema(Long id, UpdateCinemaRequest request);

    void deleteCinema(Long id);

    List<HallResponse> getHallsByCinema(Long cinemaId);
}
