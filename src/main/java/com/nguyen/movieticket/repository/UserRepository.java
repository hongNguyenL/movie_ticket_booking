package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findByEnabledTrue(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountLocked = false AND u.enabled = true")
    Page<User> findActiveCustomers(Pageable pageable);

    long countByEnabledTrue();

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
