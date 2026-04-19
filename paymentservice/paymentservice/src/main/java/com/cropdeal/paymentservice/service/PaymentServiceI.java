package com.cropdeal.paymentservice.service;

import com.cropdeal.paymentservice.dto.BillingDTO;
import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;

import java.util.List;

public interface PaymentServiceI {
    PaymentResponse initiatePayment(Long dealerId, PaymentRequest request);
    PaymentResponse getPaymentByDeal(Long dealId);
    List<PaymentResponse> getPaymentsByDealer(Long dealerId);
    List<PaymentResponse> getPaymentsByFarmer(Long farmerId);
    BillingDTO generateInvoice(Long paymentId);
    BillingDTO generateReceipt(Long paymentId);
	List<PaymentResponse> getAllPayments();
}