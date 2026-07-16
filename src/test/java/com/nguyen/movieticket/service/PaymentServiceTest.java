package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.PaymentRequest;
import com.nguyen.movieticket.dto.response.PaymentResponse;
import com.nguyen.movieticket.entity.*;
import com.nguyen.movieticket.exception.BadRequestException;
import com.nguyen.movieticket.exception.ResourceNotFoundException;
import com.nguyen.movieticket.mapper.PaymentMapper;
import com.nguyen.movieticket.repository.BookingRepository;
import com.nguyen.movieticket.repository.PaymentRepository;
import com.nguyen.movieticket.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPayment_ShouldCompletePayment_WhenValid() {
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingReference("BK-TEST123")
                .totalPrice(new BigDecimal("20.00"))
                .status(BookingStatus.PENDING)
                .build();

        PaymentRequest request = PaymentRequest.builder()
                .bookingId(bookingId)
                .paymentMethod("CREDIT_CARD")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(
                PaymentResponse.builder().id(1L).amount(new BigDecimal("20.00")).status("COMPLETED").build());

        PaymentResponse result = paymentService.processPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldFail_WhenBookingNotFound() {
        Long bookingId = 999L;
        PaymentRequest request = PaymentRequest.builder()
                .bookingId(bookingId)
                .paymentMethod("CREDIT_CARD")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void refundPayment_ShouldRefund_WhenCompleted() {
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .bookingReference("BK-TEST123")
                .totalPrice(new BigDecimal("20.00"))
                .status(BookingStatus.CONFIRMED)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .booking(booking)
                .amount(new BigDecimal("20.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

        paymentService.refundPayment(bookingId);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelReason()).isEqualTo("Payment refunded");
        verify(paymentRepository).save(payment);
        verify(bookingRepository).save(booking);
    }
}
