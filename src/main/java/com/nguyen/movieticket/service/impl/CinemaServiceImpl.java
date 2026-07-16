package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.CreateCinemaRequest;
import com.nguyen.movieticket.dto.request.UpdateCinemaRequest;
import com.nguyen.movieticket.dto.response.CinemaResponse;
import com.nguyen.movieticket.dto.response.HallResponse;
import com.nguyen.movieticket.entity.Cinema;
import com.nguyen.movieticket.entity.Hall;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.CinemaMapper;
import com.nguyen.movieticket.mapper.HallMapper;
import com.nguyen.movieticket.repository.CinemaRepository;
import com.nguyen.movieticket.repository.HallRepository;
import com.nguyen.movieticket.service.CinemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CinemaServiceImpl implements CinemaService {

    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;
    private final CinemaMapper cinemaMapper;
    private final HallMapper hallMapper;

    @Override
    public List<CinemaResponse> getAllCinemas() {
        List<Cinema> cinemas = cinemaRepository.findByIsActiveTrue();
        return cinemaMapper.toResponseList(cinemas);
    }

    @Override
    public CinemaResponse getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        return cinemaMapper.toResponse(cinema);
    }

    @Override
    @Transactional
    public CinemaResponse createCinema(CreateCinemaRequest request) {
        Cinema cinema = Cinema.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .phone(request.getPhone())
                .email(request.getEmail())
                .description(request.getDescription())
                .isActive(true)
                .build();

        cinema = cinemaRepository.save(cinema);
        log.info("Cinema created: {}", cinema.getName());
        return cinemaMapper.toResponse(cinema);
    }

    @Override
    @Transactional
    public CinemaResponse updateCinema(Long id, UpdateCinemaRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));

        if (request.getName() != null) cinema.setName(request.getName());
        if (request.getAddress() != null) cinema.setAddress(request.getAddress());
        if (request.getCity() != null) cinema.setCity(request.getCity());
        if (request.getState() != null) cinema.setState(request.getState());
        if (request.getPhone() != null) cinema.setPhone(request.getPhone());
        if (request.getEmail() != null) cinema.setEmail(request.getEmail());
        if (request.getDescription() != null) cinema.setDescription(request.getDescription());

        cinema = cinemaRepository.save(cinema);
        log.info("Cinema updated: {}", cinema.getName());
        return cinemaMapper.toResponse(cinema);
    }

    @Override
    @Transactional
    public void deleteCinema(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema", "id", id));
        cinema.setIsActive(false);
        cinemaRepository.save(cinema);
        log.warn("Cinema soft-deleted: {}", cinema.getName());
    }

    @Override
    public List<HallResponse> getHallsByCinema(Long cinemaId) {
        List<Hall> halls = hallRepository.findByCinemaIdAndIsActiveTrue(cinemaId);
        return hallMapper.toResponseList(halls);
    }
}
