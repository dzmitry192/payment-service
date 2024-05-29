package com.innowise.sivachenko.service;

import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.repository.PaymentRepository;
import com.innowise.sivachenko.service.api.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class StripeServiceImpl implements StripeService {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final PaymentRepository paymentRepository;

    @Override
    public void handleChangedPaymentStatus(String payload, String sigHeader) throws SignatureVerificationException {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Ошибка проверки подписи вебхука", e);
            throw new SignatureVerificationException("Ошибка проверки подписи вебхука, сообщение: " + e.getMessage(), sigHeader);
        }

        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
        if (paymentIntent != null) {
            Optional<PaymentEntity> optionalPayment = paymentRepository.findByStripePaymentId(paymentIntent.getId());
            if (optionalPayment.isPresent()) {
                PaymentEntity payment = optionalPayment.get();
                PaymentStatus newStatus = getPaymentStatusFromEvent(event);

                if (newStatus != null && payment.getPaymentStatus() != newStatus) {
                    payment.setPaymentStatus(newStatus);
                    paymentRepository.save(payment);
                    sendStatusChangeMessage(payment, newStatus);
                }
            } else {
                log.warn("Платеж с ID Stripe {} не найден в базе данных", paymentIntent.getId());
            }
        } else {
            log.warn("Не удалось десериализовать PaymentIntent из события: {}", event.getId());
        }
    }

    private PaymentStatus getPaymentStatusFromEvent(Event event) {
        return switch (event.getType()) {
            case "payment_intent.succeeded" -> PaymentStatus.SUCCEEDED;
            case "payment_intent.canceled" -> PaymentStatus.CANCELED;
            default -> {
                log.info("Необработанный тип события: {}", event.getType());
                yield null;
            }
        };
    }

    private void sendStatusChangeMessage(PaymentEntity payment, PaymentStatus newStatus) {
        //реализовать логику отправки уведомления пользователю на почту
        CompletableFuture.runAsync(() -> {
            log.info("Уведомление пользователя {} о смене статуса платежа на {}", payment.getClientId(), newStatus);
        });
    }
}
