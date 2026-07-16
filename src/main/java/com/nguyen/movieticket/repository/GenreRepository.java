package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByName(String name);

    Optional<Genre> findBySlug(String slug);

    List<Genre> findAllByOrderByNameAsc();
}
