package com.innowise.sivachenko.validation.api;

import com.innowise.sivachenko.validation.PaymentMethodIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PaymentMethodIdValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentMethodId {
    String message() default "Invalid payment method ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
