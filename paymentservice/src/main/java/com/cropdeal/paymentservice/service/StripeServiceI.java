package com.cropdeal.paymentservice.service;

import java.math.BigDecimal;

import com.stripe.exception.StripeException;
 
public interface StripeServiceI {
    StripePaymentResult createCheckoutSession(Long dealId, BigDecimal amount, String currency) throws StripeException;
}
