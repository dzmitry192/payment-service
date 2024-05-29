package com.innowise.sivachenko.service.api;

import com.innowise.sivachenko.model.dto.request.CreatePaymentDto;
import com.innowise.sivachenko.model.dto.response.PaymentDto;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.model.exception.CannotCreatePaymentException;
import com.innowise.sivachenko.model.exception.CannotDeletePaymentException;
import com.innowise.sivachenko.model.exception.RefundPaymentException;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PaymentService {

    Page<PaymentDto> getPayments(Long amount, String currency, String paymentMethodId, Long carId, Long rentId,
                                 Long clientId, LocalDateTime createdFrom, LocalDateTime createdAt,
                                 PaymentStatus paymentStatus, Pageable pageable);

    PaymentDto getPaymentById(Long paymentId);

    PaymentDto getPaymentByStripePaymentId(String stripePaymentId);

    PaymentDto createPayment(CreatePaymentDto createPaymentDto) throws ServiceNotFoundException, CannotCreatePaymentException;

    PaymentDto refundPayment(String stripePaymentId) throws RefundPaymentException;

    PaymentDto deletePaymentById(Long paymentId) throws ServiceNotFoundException, CannotDeletePaymentException;
}
