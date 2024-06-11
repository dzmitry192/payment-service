package com.innowise.sivachenko.controller.stripe;

import com.innowise.sivachenko.service.StripeServiceImpl;
import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-service/stripe/webhooks")
@RequiredArgsConstructor
public class StripePaymentController {

    private final StripeServiceImpl stripeService;

    @PostMapping("/payment/status")
    public void handleChangedPaymentStatus(@RequestBody String payload,
                                           @RequestHeader("Stripe-Signature") String sigHeader) throws SignatureVerificationException {
        stripeService.handleChangedPaymentStatus(payload, sigHeader);
    }
}
