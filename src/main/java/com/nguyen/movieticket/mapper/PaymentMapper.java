package com.nguyen.movieticket.mapper;

import com.nguyen.movieticket.dto.response.PaymentResponse;
import com.nguyen.movieticket.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);
}
