package com.innowise.sivachenko.model.entity;

import com.innowise.sivachenko.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_payment_id", nullable = false)
    private String stripePaymentId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_method_id", nullable = false)
    private String paymentMethodId;

    @Enumerated(value = EnumType.ORDINAL)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "rent_id", nullable = false)
    private Long rentId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
