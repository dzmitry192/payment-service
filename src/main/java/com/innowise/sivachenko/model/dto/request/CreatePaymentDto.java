package com.innowise.sivachenko.model.dto.request;

public record CreatePaymentDto(
        Long amount,
        String currency,
        String paymentMethodId,
        Long carId,
        Long rentId,
        Long clientId
) {
}