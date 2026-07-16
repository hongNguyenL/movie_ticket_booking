package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.ActorResponse;
import com.nguyen.movieticket.dto.response.MovieResponse;
import com.nguyen.movieticket.dto.response.MovieSummaryResponse;
import com.nguyen.movieticket.entity.Actor;
import com.nguyen.movieticket.entity.Movie;
import com.nguyen.movieticket.entity.MovieActor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {GenreMapper.class, ActorMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MovieMapper {

    @Mapping(target = "genre", source = "genre.name")
    @Mapping(target = "genreId", source = "genre.id")
    @Mapping(target = "directorName", source = "director.name")
    @Mapping(target = "directorId", source = "director.id")
    @Mapping(target = "actors", source = "movieActors")
    MovieResponse toResponse(Movie movie);

    @Mapping(target = "genre", source = "genre.name")
    MovieSummaryResponse toSummary(Movie movie);

    List<MovieSummaryResponse> toSummaryList(List<Movie> movies);

    default ActorResponse movieActorToActorResponse(MovieActor movieActor) {
        if (movieActor == null) return null;
        ActorResponse response = new ActorResponse();
        Actor actor = movieActor.getActor();
        if (actor != null) {
            response.setId(actor.getId());
            response.setName(actor.getName());
            response.setPhoto(actor.getPhoto());
        }
        response.setRoleName(movieActor.getRoleName());
        return response;
    }
}
