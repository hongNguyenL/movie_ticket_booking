package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.ShowtimeResponse;
import com.nguyen.movieticket.entity.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MovieMapper.class, HallMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShowtimeMapper {

    ShowtimeResponse toResponse(Showtime showtime);

    List<ShowtimeResponse> toResponseList(List<Showtime> showtimes);
}
