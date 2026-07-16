package com.nguyen.movieticket.repository;

import com.nguyen.movieticket.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUuid(UUID uuid);

    Optional<Payment> findByBookingId(Long bookingId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt >= :start AND p.paidAt < :end")
    BigDecimal sumCompletedPaymentsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('TO_CHAR', p.paidAt, 'YYYY-MM') as month, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.paidAt >= :start AND p.paidAt < :end GROUP BY month ORDER BY month")
    List<Object[]> getMonthlyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Payment> findByBookingUserIdOrderByCreatedAtDesc(Long userId);
}
