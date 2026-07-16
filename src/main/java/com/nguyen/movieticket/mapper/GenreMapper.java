package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.GenreResponse;
import com.nguyen.movieticket.entity.Genre;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GenreMapper {

    GenreResponse toResponse(Genre genre);

    List<GenreResponse> toResponseList(List<Genre> genres);
}
