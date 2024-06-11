package com.innowise.sivachenko.service.api;

import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.stripe.exception.SignatureVerificationException;

public interface StripeService {
    PaymentEntity handleChangedPaymentStatus(String payload, String sigHeader) throws Exception;
}
