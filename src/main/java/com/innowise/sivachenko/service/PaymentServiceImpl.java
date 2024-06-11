package com.innowise.sivachenko.service;

import com.innowise.sivachenko.feign.RentServiceFeignClient;
import com.innowise.sivachenko.mapstruct.PaymentMapperImpl;
import com.innowise.sivachenko.model.dto.request.CreatePaymentDto;
import com.innowise.sivachenko.model.dto.response.PaymentDto;
import com.innowise.sivachenko.model.entity.PaymentEntity;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.model.enums.RentStatus;
import com.innowise.sivachenko.model.exception.CannotCreatePaymentException;
import com.innowise.sivachenko.model.exception.CannotDeletePaymentException;
import com.innowise.sivachenko.model.exception.RefundPaymentException;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import com.innowise.sivachenko.repository.PaymentRepository;
import com.innowise.sivachenko.service.api.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.innowise.sivachenko.model.enums.PaymentStatus.*;
import static com.innowise.sivachenko.service.specification.PaymentSpecification.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapperImpl paymentMapper;
    private final RentServiceFeignClient rentServiceFeignClient;

    @Override
    public Page<PaymentDto> getPayments(Long amount, String currency, String paymentMethodId, Long carId, Long rentId,
                                        Long clientId, LocalDateTime createdFrom, LocalDateTime createdTo,
                                        PaymentStatus paymentStatus, Pageable pageable) {
        log.info("Getting payments with filter params");
        return paymentRepository.findAll(Specification.where(
                        withAmount(amount)
                                .and(withCurrency(currency))
                                .and(withPaymentMethodId(paymentMethodId))
                                .and(withCarId(carId))
                                .and(withRentId(rentId))
                                .and(withClientId(clientId))
                                .and(withCreatedAt(createdFrom, createdTo))
                                .and(withPaymentStatus(paymentStatus))
                ), pageable)
                .map(paymentMapper::toPaymentDto);
    }

    @Override
    public PaymentDto getPaymentById(Long paymentId) {
        log.info("Getting payment with id {}", paymentId);
        Optional<PaymentEntity> optionalPaymentEntity = paymentRepository.findById(paymentId);
        if (optionalPaymentEntity.isEmpty()) {
            throw new EntityNotFoundException("Payment with id " + paymentId + " not found");
        }
        return paymentMapper.toPaymentDto(optionalPaymentEntity.get());
    }

    @Override
    public PaymentDto getPaymentByStripePaymentId(String stripePaymentId) {
        log.info("Getting payment with stripe payment id {}", stripePaymentId);
        Optional<PaymentEntity> optionalPaymentEntity = paymentRepository.findByStripePaymentId(stripePaymentId);
        if (optionalPaymentEntity.isEmpty()) {
            throw new EntityNotFoundException("Payment with stripe method id " + stripePaymentId + " not found");
        }
        return paymentMapper.toPaymentDto(optionalPaymentEntity.get());
    }

    @Override
    @Transactional
    public PaymentDto createPayment(CreatePaymentDto createPaymentDto) throws ServiceNotFoundException, CannotCreatePaymentException {
        log.info("Starting creating a payment with body: {}", createPaymentDto);
        if (!rentServiceFeignClient.canPayRent(createPaymentDto.rentId())) {
            throw new CannotCreatePaymentException(String.format("Cannot create payment with rent id {%s}, because rent is not active", createPaymentDto.rentId()));
        }
        PaymentEntity payment = paymentMapper.toPaymentEntity(createPaymentDto);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(createPaymentDto.amount())
                .setCurrency(createPaymentDto.currency())
                .setPaymentMethod(createPaymentDto.paymentMethodId())
                .setConfirm(true)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                .build())
                .build();
        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return paymentMapper.toPaymentDto(processPaymentByStatus(paymentIntent, payment));
        } catch (StripeException e) {
            throw new CannotCreatePaymentException(String.format("Couldn't create payment due to stripe exception, status code: {%s}; message: {%s}", e.getStatusCode(), e.getMessage()));
        }
    }

    @Override
    @Transactional
    public PaymentDto refundPayment(String stripePaymentId) throws RefundPaymentException {
        log.info("Starting refunding payment with stripe payment id: {}", stripePaymentId);
        Optional<PaymentEntity> optionalPayment = paymentRepository.findByStripePaymentId(stripePaymentId);
        if (optionalPayment.isEmpty()) {
            throw new EntityNotFoundException("Payment with stripe payment id " + stripePaymentId + " not found");
        }
        PaymentEntity payment = optionalPayment.get();
        try {
            Refund refund = Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentId())
                    .build());
            if (PaymentStatus.valueOf(refund.getStatus().toUpperCase()).equals(SUCCEEDED)) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                return paymentMapper.toPaymentDto(paymentRepository.save(payment));
            } else {
                throw new RefundPaymentException(String.format("Can't refund payment with id {%s}, status code: {%s}", payment.getId(), refund.getStatus()));
            }
        } catch (StripeException e) {
            throw new RefundPaymentException(String.format("Can't refund payment with id {%s}, status code: {%s}; message: {%s}", payment.getId(), e.getStatusCode(), e.getMessage()));
        }
    }

    @Override
    public PaymentDto deletePaymentById(Long paymentId) throws ServiceNotFoundException, CannotDeletePaymentException {
        Optional<PaymentEntity> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            throw new EntityNotFoundException("Payment with id " + paymentId + " not found");
        }
        PaymentEntity payment = optionalPayment.get();
        if (rentServiceFeignClient.existsActiveRent(payment.getRentId(), null, null)) {
            throw new CannotDeletePaymentException(String.format("Couldn't delete payment exception, because rent with id {%s} is active", payment.getRentId()));
        }
        paymentRepository.delete(payment);
        return paymentMapper.toPaymentDto(payment);
    }

    private PaymentEntity processPaymentByStatus(PaymentIntent paymentIntent, PaymentEntity payment) throws CannotCreatePaymentException, StripeException, ServiceNotFoundException {
        log.info("Starting checking payment by status. Payment status: {}", paymentIntent.getStatus());
        payment.setStripePaymentId(paymentIntent.getId());

        PaymentStatus status;
        try {
            status = PaymentStatus.valueOf(paymentIntent.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CannotCreatePaymentException("Unknown payment status received: " + paymentIntent.getStatus());
        }

        switch (status) {
            case REQUIRES_PAYMENT_METHOD:
                throw new CannotCreatePaymentException("Payment requires a valid payment method");
            case REQUIRES_CONFIRMATION:
                throw new CannotCreatePaymentException("Payment requires confirmation");
            case REQUIRES_ACTION:
                payment.setPaymentStatus(REQUIRES_ACTION);
                break;
            case REQUIRES_CAPTURE:
                log.info("Starting capture...");
                PaymentIntent intent = PaymentIntent.retrieve(paymentIntent.getId());
                intent.capture();
                payment.setPaymentStatus(SUCCEEDED);
                break;
            case PROCESSING:
                payment.setPaymentStatus(PROCESSING);
                break;
            case CANCELED:
                payment.setPaymentStatus(CANCELED);
                break;
            case SUCCEEDED:
                payment.setPaymentStatus(SUCCEEDED);
                rentServiceFeignClient.updateRentStatus(payment.getRentId(), RentStatus.COMPLETED);
                break;
            default:
                throw new CannotCreatePaymentException("Unknown payment status received");
        }
        return paymentRepository.save(payment);
    }
}