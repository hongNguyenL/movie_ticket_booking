package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.HallResponse;
import com.nguyen.movieticket.entity.Hall;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CinemaMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HallMapper {

    HallResponse toResponse(Hall hall);

    List<HallResponse> toResponseList(List<Hall> halls);
}
