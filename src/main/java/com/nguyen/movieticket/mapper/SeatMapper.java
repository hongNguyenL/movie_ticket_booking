package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.SeatResponse;
import com.nguyen.movieticket.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeatMapper {

    SeatResponse toResponse(Seat seat);

    List<SeatResponse> toResponseList(List<Seat> seats);
}
