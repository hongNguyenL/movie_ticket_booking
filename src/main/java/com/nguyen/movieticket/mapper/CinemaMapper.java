package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.CinemaResponse;
import com.nguyen.movieticket.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CinemaMapper {

    CinemaResponse toResponse(Cinema cinema);

    List<CinemaResponse> toResponseList(List<Cinema> cinemas);
}
