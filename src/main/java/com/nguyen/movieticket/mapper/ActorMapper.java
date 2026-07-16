package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.ActorResponse;
import com.nguyen.movieticket.entity.Actor;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {

    ActorResponse toResponse(Actor actor);

    List<ActorResponse> toResponseList(List<Actor> actors);
}
