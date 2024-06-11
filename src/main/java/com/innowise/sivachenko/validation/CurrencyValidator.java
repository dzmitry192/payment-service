package com.innowise.sivachenko.validation;

import com.innowise.sivachenko.validation.api.ValidCurrency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        return currency != null && currency.equals("USD");
    }
}