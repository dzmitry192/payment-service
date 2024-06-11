package com.innowise.sivachenko.service.specification;

import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentSpecification {

    public static Specification<PaymentEntity> withAmount(Long amount) {
        return (payment, query, builder) -> builder.equal(payment.get("amount"), amount);
    }

    public static Specification<PaymentEntity> withCurrency(String currency) {
        return (payment, query, builder) -> builder.equal(payment.get("currency"), currency);
    }

    public static Specification<PaymentEntity> withPaymentMethodId(String paymentMethodId) {
        return (payment, query, builder) -> builder.equal(payment.get("paymentMethodId"), paymentMethodId);
    }

    public static Specification<PaymentEntity> withCarId(Long carId) {
        return (payment, query, builder) -> builder.equal(payment.get("carId"), carId);
    }

    public static Specification<PaymentEntity> withRentId(Long rentId) {
        return (payment, query, builder) -> builder.equal(payment.get("rentId"), rentId);
    }

    public static Specification<PaymentEntity> withClientId(Long clientId) {
        return (payment, query, builder) -> builder.equal(payment.get("clientId"), clientId);
    }

    public static Specification<PaymentEntity> withCreatedAt(LocalDateTime createdFrom, LocalDateTime createdTo) {
        return (payment, query, builder) -> {
            if (createdFrom == null && createdTo == null) {
                return null;
            } else if (createdFrom != null && createdTo != null) {
                return builder.between(payment.get("createdAt"), createdFrom, createdTo);
            } else if (createdFrom != null) {
                return builder.greaterThanOrEqualTo(payment.get("createdAt"), createdFrom);
            } else {
                return builder.lessThanOrEqualTo(payment.get("createdAt"), createdTo);
            }
        };
    }

    public static Specification<PaymentEntity> withPaymentStatus(PaymentStatus paymentStatus) {
        return (payment, query, builder) -> builder.equal(payment.get("paymentStatus"), paymentStatus);
    }
}
