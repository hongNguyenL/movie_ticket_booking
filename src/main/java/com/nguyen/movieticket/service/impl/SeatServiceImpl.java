package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.response.SeatResponse;
import com.nguyen.movieticket.entity.Seat;
import com.nguyen.movieticket.mapper.SeatMapper;
import com.nguyen.movieticket.repository.SeatRepository;
import com.nguyen.movieticket.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @Override
    public List<SeatResponse> getSeatsByHall(Long hallId) {
        List<Seat> seats = seatRepository.findByHallIdOrderByRowLabelAscSeatNumberAsc(hallId);
        return seatMapper.toResponseList(seats);
    }
}
