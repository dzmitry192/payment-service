package com.innowise.sivachenko.validation;

import com.innowise.sivachenko.validation.api.ValidPaymentMethodId;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentMethodIdValidator implements ConstraintValidator<ValidPaymentMethodId, String> {

    @Override
    public boolean isValid(String paymentMethodId, ConstraintValidatorContext context) {
        if (paymentMethodId == null || paymentMethodId.isEmpty()) {
            return false;
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            return paymentMethod != null;
        } catch (StripeException e) {
            return false;
        }
    }
}