package com.innowise.sivachenko.validation;

import com.innowise.sivachenko.validation.api.ValidAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AmountValidator implements ConstraintValidator<ValidAmount, Long> {

    @Override
    public boolean isValid(Long amount, ConstraintValidatorContext context) {
        long minAmount = 50;
        return amount != null && amount >= minAmount;
    }
}
