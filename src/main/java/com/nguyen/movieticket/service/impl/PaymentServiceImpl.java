package com.nguyen.movieticket.service.impl;

import com.nguyen.movieticket.dto.request.PaymentRequest;
import com.nguyen.movieticket.dto.response.PaymentResponse;
import com.nguyen.movieticket.entity.Booking;
import com.nguyen.movieticket.entity.BookingStatus;
import com.nguyen.movieticket.entity.Payment;
import com.nguyen.movieticket.entity.PaymentStatus;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.PaymentMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.PaymentRepository;
import com.nguyen.movieticket.repository.UserRepository;
import com.nguyen.movieticket.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING status");
        }

        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BadRequestException("Payment already exists for this booking");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .transactionId(transactionId)
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setExpiresAt(null);
        bookingRepository.save(booking);

        log.info("Payment processed for booking: {}, transaction: {}", booking.getBookingReference(), transactionId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByBooking(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getUserPaymentHistory(Long userId) {
        List<Payment> payments = paymentRepository.findByBookingUserIdOrderByCreatedAtDesc(userId);
        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void refundPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment is not in COMPLETED status");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason("Payment refunded");
        bookingRepository.save(booking);

        log.info("Payment refunded for booking: {}", booking.getBookingReference());
    }
}
