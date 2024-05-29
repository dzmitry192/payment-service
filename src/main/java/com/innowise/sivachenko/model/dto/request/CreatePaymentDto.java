package com.innowise.sivachenko.model.dto.request;

import com.innowise.sivachenko.validation.api.ValidAmount;
import com.innowise.sivachenko.validation.api.ValidCurrency;
import com.innowise.sivachenko.validation.api.ValidPaymentMethodId;
import jakarta.validation.constraints.Min;

public record CreatePaymentDto(
        @ValidAmount Long amount,
        @ValidCurrency String currency,
        @ValidPaymentMethodId String paymentMethodId,
        @Min(1) Long carId,
        @Min(1) Long rentId,
        @Min(1) Long clientId
) {
}