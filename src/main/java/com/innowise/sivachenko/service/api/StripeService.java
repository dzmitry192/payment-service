package com.innowise.sivachenko.service.api;

import com.stripe.exception.SignatureVerificationException;

public interface StripeService {
    void handleChangedPaymentStatus(String payload, String sigHeader) throws SignatureVerificationException;
}
