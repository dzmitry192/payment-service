package com.innowise.sivachenko.validation.api;

import com.innowise.sivachenko.validation.AmountValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {
    String message() default "Invalid amount for Stripe payment";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}