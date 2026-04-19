package com.cropdeal.paymentservice.service;

public interface StripePaymentResult {
	String getStatus();
	String getId();
	String getPaymentUrl();
}
