package com.innowise.sivachenko.aspect;

import com.innowise.sivachenko.kafka.avro.AuditActionRequest;
import com.innowise.sivachenko.kafka.avro.AuditActionType;
import com.innowise.sivachenko.kafka.avro.PaymentNotificationType;
import com.innowise.sivachenko.kafka.avro.PaymentServiceNotification;
import com.innowise.sivachenko.kafka.producer.AuditActionProducer;
import com.innowise.sivachenko.kafka.producer.NotificationProducer;
import com.innowise.sivachenko.model.dto.request.CreatePaymentDto;
import com.innowise.sivachenko.model.dto.response.PaymentDto;
import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.model.exception.CannotCreatePaymentException;
import com.innowise.sivachenko.model.exception.CannotDeletePaymentException;
import com.innowise.sivachenko.model.exception.RefundPaymentException;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.time.ZoneId;

import static com.innowise.sivachenko.kafka.avro.AuditActionType.*;
import static com.innowise.sivachenko.kafka.avro.AuditServiceType.PAYMENT;
import static com.innowise.sivachenko.kafka.avro.PaymentStatus.*;
import static com.innowise.sivachenko.security.util.SecurityUtil.getUserEmail;
import static java.lang.String.format;

@Log4j2
@Component
@Aspect
@RequiredArgsConstructor
public class AuditNotificationAspect {

    private final AuditActionProducer auditActionProducer;
    private final NotificationProducer notificationProducer;

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPayments())",
            returning = "result",
            argNames = "result"
    )
    public void getPayments_afterReturning(ResponseEntity<Page<PaymentDto>> result) {
        log.info("Payments received successfully at {}", Instant.now());
        sendAuditActionRequest(
                format("Payments received successfully at {%s}", Instant.now()),
                GET,
                result.getStatusCode().value()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPayments())",
            throwing = "ex",
            argNames = "ex"
    )
    public void getPayments_afterThrowing(Exception ex) {
        log.info("An error occurred while getting payments at {}, reason: {}, message: {}", Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("An error occurred while getting payments at {%s}, reason: {%s}, message: {%s}", Instant.now(), ex.getCause(), ex.getMessage()),
                GET,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPaymentById()) && args(paymentId)",
            returning = "result",
            argNames = "paymentId,result"
    )
    public void getPaymentById_afterReturning(Long paymentId, ResponseEntity<PaymentDto> result) {
        log.info("Payment with id {} received successfully at {}", paymentId, Instant.now());
        sendAuditActionRequest(
                format("Payment with id {%s} received successfully at {%s}", paymentId, Instant.now()),
                GET,
                result.getStatusCode().value()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPaymentById()) && args(paymentId)",
            throwing = "ex",
            argNames = "paymentId,ex"
    )
    public void getPaymentById_afterThrowing(Long paymentId, Exception ex) {
        log.info("An error occurred while getting payment with id {} at {}, reason: {}, message: {}", paymentId, Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("An error occurred while getting payment with id {%s} at {%s}, reason: {%s}, message: {%s}", paymentId, Instant.now(), ex.getCause(), ex.getMessage()),
                GET,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPaymentByStripeId()) && args(stripeId)",
            returning = "result",
            argNames = "stripeId,result"
    )
    public void getPaymentByStripeId_afterReturning(String stripeId, ResponseEntity<PaymentDto> result) {
        log.info("Payment with stripe id {} received successfully at {}", stripeId, Instant.now());
        sendAuditActionRequest(
                format("Payment with stripe id {%s} received successfully at {%s}", stripeId, Instant.now()),
                GET,
                result.getStatusCode().value()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.getPaymentByStripeId()) && args(stripeId)",
            throwing = "ex",
            argNames = "stripeId,ex"
    )
    public void getPaymentByStripeId_afterThrowing(String stripeId, Exception ex) {
        log.info("An error occurred while getting payment with stripe id {} at {}, reason: {}, message: {}", stripeId, Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("An error occurred while getting payment with stripe id {%s} at {%s}, reason: {%s}, message: {%s}", stripeId, Instant.now(), ex.getCause(), ex.getMessage()),
                GET,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.createPayment()) && args(createPaymentDto)",
            returning = "result",
            argNames = "createPaymentDto,result"
    )
    public void createPayment_afterReturning(CreatePaymentDto createPaymentDto, ResponseEntity<PaymentDto> result) {
        log.info("Payment with body {} created successfully at {}", createPaymentDto, Instant.now());
        sendAuditActionRequest(
                format("Payment with body {%s} created successfully at {%s}", createPaymentDto, Instant.now()),
                POST,
                result.getStatusCode().value()
        );
        if (!result.getBody().paymentStatus().equals(PaymentStatus.CANCELED)) {
            notificationProducer.sendMessage(new PaymentServiceNotification(
                    getUserEmail(),
                    result.getBody().paymentStatus().equals(PaymentStatus.SUCCEEDED) ? com.innowise.sivachenko.kafka.avro.PaymentStatus.SUCCEEDED : PROCESSING,
                    result.getBody().amount(),
                    result.getBody().currency(),
                    result.getBody().createdAt().atZone(ZoneId.systemDefault()).toInstant(),
                    PaymentNotificationType.CREATE_PAYMENT
            ));
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.createPayment()) && args(createPaymentDto)",
            throwing = "ex",
            argNames = "createPaymentDto,ex"
    )
    public void createPayment_afterThrowing(CreatePaymentDto createPaymentDto, Exception ex) {
        log.info("Error when creating a payment with body {} at {}, reason: {}, message: {}", createPaymentDto, Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("Error when creating a payment with body {%s} at {%s}, reason: {%s}, message: {%s}", createPaymentDto, Instant.now(), ex.getCause(), ex.getMessage()),
                POST,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.refundPayment()) && args(stripePaymentId)",
            returning = "result",
            argNames = "stripePaymentId,result"
    )
    public void refundPayment_afterReturning(String stripePaymentId, ResponseEntity<PaymentDto> result) {
        log.info("Payment with stripe id {} refunded successfully at {}", stripePaymentId, Instant.now());
        sendAuditActionRequest(
                format("Payment with stripe id {%s} refunded successfully at {%s}", stripePaymentId, Instant.now()),
                POST,
                result.getStatusCode().value()
        );
        notificationProducer.sendMessage(new PaymentServiceNotification(
                getUserEmail(),
                REFUNDED,
                result.getBody().amount(),
                result.getBody().currency(),
                result.getBody().createdAt().atZone(ZoneId.systemDefault()).toInstant(),
                PaymentNotificationType.REFUND_PAYMENT
        ));
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.refundPayment()) && args(stripePaymentId)",
            throwing = "ex",
            argNames = "stripePaymentId,ex"
    )
    public void refundPayment_afterThrowing(String stripePaymentId, Exception ex) {
        log.info("Error when refunding a payment with stripe id {} at {}, reason: {}, message: {}", stripePaymentId, Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("Error when refunding a payment with stripe id {%s} at {%s}, reason: {%s}, message: {%s}", stripePaymentId, Instant.now(), ex.getCause(), ex.getMessage()),
                POST,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.deletePayment()) && args(paymentId)",
            returning = "result",
            argNames = "paymentId,result"
    )
    public void deletePayment_afterReturning(Long paymentId, ResponseEntity<PaymentDto> result) {
        log.info("Payment with id {} deleted successfully at {}", paymentId, Instant.now());
        sendAuditActionRequest(
                format("Payment with id {%s} deleted successfully at {%s}", paymentId, Instant.now()),
                DELETE,
                result.getStatusCode().value()
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.PaymentController.deletePayment()) && args(paymentId)",
            throwing = "ex",
            argNames = "paymentId,ex"
    )
    public void deletePayment_afterThrowing(Long paymentId, Exception ex) {
        log.info("An error occurred while deleting payment with id {} at {}, reason: {}, message: {}", paymentId, Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("An error occurred while deleting payment with id {%s} at {%s}, reason: {%s}, message: {%s}", paymentId, Instant.now(), ex.getCause(), ex.getMessage()),
                DELETE,
                getStatusCode(ex)
        );
    }

    @AfterReturning(
            pointcut = "execution(* com.innowise.sivachenko.controller.stripe.StripePaymentController.handleChangedPaymentStatus()) && args(payload)",
            returning = "result",
            argNames = "payload,result"
    )
    public void handleChangedPaymentStatus_afterReturning(String payload, PaymentEntity result) {
        log.info("Changing payment status with payment id {} was successful at {}", result.getId(), Instant.now());
        sendAuditActionRequest(
                format("Changing payment status with payment id {%s} was successful at {%s}", result.getId(), Instant.now()),
                POST,
                200
        );
    }

    @AfterThrowing(
            pointcut = "execution(* com.innowise.sivachenko.controller.stripe.StripePaymentController.handleChangedPaymentStatus()) && args(payload)",
            throwing = "ex",
            argNames = "payload,ex"
    )
    public void handleChangedPaymentStatus_afterThrowing(String payload, Exception ex) {
        log.info("Error when changing payment status at {}; reason: {}, message: {}", Instant.now(), ex.getCause(), ex.getMessage());
        sendAuditActionRequest(
                format("Error when changing payment status at {%s}; reason: {%s}, message: {%s}", Instant.now(), ex.getCause(), ex.getMessage()),
                POST,
                getStatusCode(ex)
        );
    }

    private void sendAuditActionRequest(String info, AuditActionType actionType, int statusCode) {
        auditActionProducer.sendMessage(new AuditActionRequest(
                getUserEmail(),
                info,
                actionType,
                PAYMENT,
                statusCode,
                Instant.now()
        ));
    }

    private int getStatusCode(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException
                || ex instanceof CannotCreatePaymentException
                || ex instanceof CannotDeletePaymentException
                || ex instanceof RefundPaymentException) {
            return 400;
        } else if (ex instanceof AccessDeniedException) {
            return 403;
        } else if (ex instanceof EntityNotFoundException) {
            return 404;
        } else if (ex instanceof ServiceNotFoundException) {
            return 503;
        } else {
            return 500;
        }
    }

}
