package com.innowise.sivachenko.service;

import com.innowise.sivachenko.feign.RentServiceFeignClient;
import com.innowise.sivachenko.kafka.avro.PaymentNotificationType;
import com.innowise.sivachenko.kafka.avro.PaymentServiceNotification;
import com.innowise.sivachenko.kafka.producer.NotificationProducer;
import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.model.enums.RentStatus;
import com.innowise.sivachenko.repository.PaymentRepository;
import com.innowise.sivachenko.service.api.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.innowise.sivachenko.kafka.avro.PaymentStatus.*;
import static com.innowise.sivachenko.security.util.SecurityUtil.getUserEmail;
import static java.lang.String.format;

@Log4j2
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    private final RentServiceFeignClient rentServiceFeignClient;
    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final PaymentRepository paymentRepository;
    private final NotificationProducer notificationProducer;

    @Override
    public PaymentEntity handleChangedPaymentStatus(String payload, String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification error; reason: {}, message: {}", e.getCause(), e.getMessage());
            throw new SignatureVerificationException(format("Webhook signature verification error; reason: {%s}, message: {%s})", e.getCause(), e.getMessage()), sigHeader);
        }

        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        PaymentEntity payment;
        if (paymentIntent != null) {
            Optional<PaymentEntity> optionalPayment = paymentRepository.findByStripePaymentId(paymentIntent.getId());
            if (optionalPayment.isPresent()) {
                payment = optionalPayment.get();
                PaymentStatus newStatus = getPaymentStatusFromEvent(event);


                if (newStatus != null && payment.getPaymentStatus() != newStatus) {
                    payment.setPaymentStatus(newStatus);
                    if (newStatus.equals(PaymentStatus.SUCCEEDED)) {
                        rentServiceFeignClient.updateRentStatus(payment.getRentId(), RentStatus.COMPLETED);
                    }
                    paymentRepository.save(payment);
                    sendStatusChangeMessage(payment);
                }
            } else {
                log.warn("Payment with stripe id {} not found in database", paymentIntent.getId());
                throw new EntityNotFoundException(format("Payment with stripe id {%s} not found in database", paymentIntent.getId()));
            }
        } else {
            log.warn("Failed to deserialize PaymentIntent from event: {}", event.getId());
            throw new Exception(format("Failed to deserialize PaymentIntent from event: {%s}", event.getId()));
        }
        return payment;
    }

    private PaymentStatus getPaymentStatusFromEvent(Event event) {
        return switch (event.getType()) {
            case "payment_intent.requires_action" -> PaymentStatus.REQUIRES_ACTION;
            case "payment_intent.succeeded" -> PaymentStatus.SUCCEEDED;
            case "payment_intent.canceled" -> PaymentStatus.CANCELED;
            case "payment_intent.payment_failed" -> PaymentStatus.FAILED;
            default -> {
                log.info("Raw event type: {}", event.getType());
                yield null;
            }
        };
    }

    private void sendStatusChangeMessage(PaymentEntity payment) {
        CompletableFuture.runAsync(() -> {
            notificationProducer.sendMessage(new PaymentServiceNotification(
                    getUserEmail(),
                    getPaymentStatus(payment.getPaymentStatus()),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                    PaymentNotificationType.CHANGE_PAYMENT_STATUS
            ));
        });
    }
    
    private com.innowise.sivachenko.kafka.avro.PaymentStatus getPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus.equals(PaymentStatus.SUCCEEDED)) {
            return SUCCEEDED;
        } else if (paymentStatus.equals(PaymentStatus.CANCELED)) {
            return CANCELED;
        } else if (paymentStatus.equals(PaymentStatus.REQUIRES_ACTION)) {
            return REQUIRES_ACTION;
        } else if (paymentStatus.equals(PaymentStatus.FAILED)) {
            return FAILED;
        }
        return null;
    }
}
