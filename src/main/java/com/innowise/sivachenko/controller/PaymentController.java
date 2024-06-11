package com.innowise.sivachenko.controller;

import com.innowise.sivachenko.model.dto.request.CreatePaymentDto;
import com.innowise.sivachenko.model.dto.response.PaymentDto;
import com.innowise.sivachenko.model.enums.PaymentStatus;
import com.innowise.sivachenko.model.exception.CannotCreatePaymentException;
import com.innowise.sivachenko.model.exception.CannotDeletePaymentException;
import com.innowise.sivachenko.model.exception.RefundPaymentException;
import com.innowise.sivachenko.model.exception.ServiceNotFoundException;
import com.innowise.sivachenko.service.PaymentServiceImpl;
import com.innowise.sivachenko.validation.api.ValidAmount;
import com.innowise.sivachenko.validation.api.ValidCurrency;
import com.innowise.sivachenko.validation.api.ValidPaymentMethodId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Payment Controller", description = "Payment controller manages operations with payments")
@RestController
@RequestMapping("/api/v1/payment-service")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    @Operation(
            summary = "Get page of payments with query params"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @Parameters({
            @Parameter(name = "amount", description = "Amount of the payment"),
            @Parameter(name = "currency", description = "Currency of the payment"),
            @Parameter(name = "paymentMethodId", description = "ID of the payment method used"),
            @Parameter(name = "carId", description = "ID of the car involved in the payment"),
            @Parameter(name = "rentId", description = "ID of the rental associated with the payment"),
            @Parameter(name = "clientId", description = "ID of the client making the payment"),
            @Parameter(name = "createdFrom", description = "Start of the date range for payment creation"),
            @Parameter(name = "createdTo", description = "End of the date range for payment creation"),
            @Parameter(name = "paymentStatus", description = "Status of the payment (ACTIVE, COMPLETED, CANCELED)"),
            @Parameter(name = "page", description = "Page number (starts from 0)", example = "0"),
            @Parameter(name = "size", description = "Number of payments per page (max 100)", example = "10")

    })
    @GetMapping("/")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<Page<PaymentDto>> getPayments(
            @RequestParam(name = "amount", required = false) @ValidAmount Long amount,
            @RequestParam(name = "currency", required = false) @ValidCurrency String currency,
            @RequestParam(name = "paymentMethodId", required = false) @ValidPaymentMethodId String paymentMethodId,
            @RequestParam(name = "carId", required = false) @Min(1) Long carId,
            @RequestParam(name = "rentId", required = false) @Min(1) Long rentId,
            @RequestParam(name = "clientId", required = false) @Min(1) Long clientId,
            @RequestParam(name = "createdFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(name = "createdTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(name = "paymentStatus", required = false) PaymentStatus paymentStatus,
            @RequestParam(name = "page", required = false, defaultValue = "0") @Min(1) Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") @Min(1) @Max(100) Integer size) {
        return ResponseEntity.ok(paymentService.getPayments(amount, currency, paymentMethodId, carId, rentId, clientId, createdFrom, createdTo, paymentStatus, PageRequest.of(page, size)));
    }

    @Operation(
            summary = "Get payment by payment id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable(name = "paymentId") @Min(1) Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @Operation(
            summary = "Get payment by stripe id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @GetMapping("/stripeid/{stripeId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentByStripeId(@PathVariable(name = "stripeId") String stripeId) {
        return ResponseEntity.ok(paymentService.getPaymentByStripePaymentId(stripeId));
    }

    @Operation(
            summary = "Create payment"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @PostMapping("/")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentDto createPaymentDto) throws ServiceNotFoundException, CannotCreatePaymentException {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(createPaymentDto));
    }

    @Operation(
            summary = "Refund payment"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @PostMapping("/refund/{stripePaymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> refundPayment(@PathVariable(name = "stripePaymentId") String stripePaymentId) throws RefundPaymentException {
        return ResponseEntity.ok(paymentService.refundPayment(stripePaymentId));
    }

    @Operation(
            summary = "Delete payment by payment id"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = Page.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error", content = {@Content(mediaType = "application/json")})
    })
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    public ResponseEntity<PaymentDto> deletePayment(@PathVariable(name = "paymentId") @Min(1) Long paymentId) throws ServiceNotFoundException, CannotDeletePaymentException {
        return ResponseEntity.ok(paymentService.deletePaymentById(paymentId));
    }
}