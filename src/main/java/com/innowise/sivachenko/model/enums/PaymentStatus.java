package com.innowise.sivachenko.model.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    CANCELED("canceled"),
    PROCESSING("processing"),
    REQUIRES_ACTION("requires_action"),
    REQUIRES_CAPTURE("requires_capture"),
    REQUIRES_CONFIRMATION("requires_confirmation"),
    REQUIRES_PAYMENT_METHOD("requires_payment_method"),
    SUCCEEDED("succeeded"),
    REFUNDED("refunded"),
    FAILED("failed");

    private String statusName;

    PaymentStatus(String statusName) {
        this.statusName = statusName;
    }

}
