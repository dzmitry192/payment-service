package com.innowise.sivachenko.model.dto.response;

import com.innowise.sivachenko.model.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentDto(
        Long id,
        String email,
        String stripePaymentId,
        Long amount,
        String currency,
        String paymentMethodId,
        PaymentStatus paymentStatus,
        Long carId,
        Long rentId,
        Long clientId,
        LocalDateTime createdAt
) {
}
