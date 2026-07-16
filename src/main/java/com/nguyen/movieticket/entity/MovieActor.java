package com.nguyen.movieticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movie_actors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "actor_id"}))
public class MovieActor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Actor actor;

    @Column(name = "role_name", length = 200)
    private String roleName;
}
