package com.nguyen.movieticket.service;

import com.nguyen.movieticket.dto.request.PaymentRequest;
import com.nguyen.movieticket.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentByBooking(Long bookingId);

    List<PaymentResponse> getUserPaymentHistory(Long userId);

    void refundPayment(Long bookingId);
}
