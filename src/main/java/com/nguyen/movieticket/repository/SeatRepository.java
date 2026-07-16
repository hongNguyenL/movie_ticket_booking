package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByHallIdOrderByRowLabelAscSeatNumberAsc(Long hallId);

    @Query("SELECT s FROM Seat s WHERE s.hall.id = :hallId AND s.isActive = true ORDER BY s.rowLabel, s.seatNumber")
    List<Seat> findActiveSeatsByHallId(@Param("hallId") Long hallId);

    @Query("SELECT s FROM Seat s LEFT JOIN FETCH s.hall WHERE s.id IN :seatIds")
    List<Seat> findByIdsWithHall(@Param("seatIds") Set<Long> seatIds);
}
