package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    List<Actor> findByNameContainingIgnoreCase(String name);

    Page<Actor> findAll(Pageable pageable);
}
