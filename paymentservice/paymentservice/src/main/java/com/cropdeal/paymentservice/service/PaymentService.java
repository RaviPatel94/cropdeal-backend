package com.cropdeal.paymentservice.service;

import com.cropdeal.paymentservice.dto.BillingDTO;
import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.feign.DealServiceClient;
import com.cropdeal.paymentservice.feign.UserServiceClient;
import com.cropdeal.paymentservice.model.Payment;
import com.cropdeal.paymentservice.model.PaymentMethod;
import com.cropdeal.paymentservice.model.PaymentStatus;
import com.cropdeal.paymentservice.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements PaymentServiceI {

    private final PaymentRepository paymentRepository;
    private final DealServiceClient dealServiceClient;
    private final StripeServiceI stripeService;
    private final UserServiceClient userClient;

    @Value("${stripe.currency:inr}")
    private String currency;
    
    public PaymentResponse verifyPayment(String sessionId) throws StripeException {
        // fetch session directly from stripe to check status
        com.stripe.model.checkout.Session session =
                com.stripe.model.checkout.Session.retrieve(sessionId);
     
        Payment payment = paymentRepository.findByTransactionReference(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
     
        if ("paid".equals(session.getPaymentStatus())
                && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
     
            try {
                dealServiceClient.completeDeal(payment.getDealId());
            } catch (Exception e) {
                log.error("Failed to complete deal: {}", e.getMessage());
            }
        }
     
        return mapToResponse(payment);
    }

@Override
public PaymentResponse initiatePayment(Long dealerId, PaymentRequest request) {
 
    DealServiceClient.DealDto deal = dealServiceClient.getDealById(request.getDealId());
 
    if (!deal.getStatus().equals("ACCEPTED")) {
        throw new RuntimeException("Deal must be ACCEPTED before payment");
    }
 
    if (paymentRepository.findByDealId(request.getDealId()).isPresent()) {
        throw new RuntimeException("Payment already done for this deal");
    }
 
    Payment payment = Payment.builder()
            .dealId(request.getDealId())
            .dealerId(dealerId)
            .farmerId(deal.getFarmerId())
            .amount(deal.getTotalAmount())
            .paymentMethod(PaymentMethod.DEBIT_CARD)
            .status(PaymentStatus.PENDING)
            .build();
 
    String paymentUrl = null;
 
    try {
        StripePaymentResult result = stripeService.createCheckoutSession(
                request.getDealId(), deal.getTotalAmount(), currency);
 
        payment.setTransactionReference(result.getId());
        paymentUrl = result.getPaymentUrl();
        // status stays PENDING until webhook confirms payment
 
    } catch (Exception e) {
        log.error("Stripe error: {}", e.getMessage());
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(e.getMessage());
    }
 
    Payment saved = paymentRepository.save(payment);
 
    PaymentResponse response = mapToResponse(saved);
    response.setPaymentUrl(paymentUrl);  // send link to frontend
    return response;
}

    @Override
    public PaymentResponse getPaymentByDeal(Long dealId) {
        return paymentRepository.findByDealId(dealId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Payment not found for deal: " + dealId));
    }
    
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByDealer(Long dealerId) {
        return paymentRepository.findAllByDealerId(dealerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getPaymentsByFarmer(Long farmerId) {
        return paymentRepository.findAllByFarmerId(farmerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setDealId(payment.getDealId());
        response.setDealerId(payment.getDealerId());
        response.setFarmerId(payment.getFarmerId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionReference(payment.getTransactionReference());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
    

        public BillingDTO generateInvoice(Long paymentId) {
            Payment p = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            BillingDTO.Party dealer = map(userClient.getUserById(p.getDealerId()));
            BillingDTO.Party farmer = map(userClient.getUserById(p.getFarmerId()));

            return BillingDTO.builder()
                    .title(p.getStatus() == PaymentStatus.SUCCESS ? "INVOICE" : "PROFORMA INVOICE")
                    .dealer(dealer)
                    .farmer(farmer)
                    .amount(p.getAmount())
                    .method(p.getPaymentMethod())
                    .status(p.getStatus())
                    .paymentId(p.getId())
                    .dealId(p.getDealId())
                    .transactionReference(p.getTransactionReference())
                    .createdAt(p.getCreatedAt())
                    .build();
        }

        public BillingDTO generateReceipt(Long paymentId) {
            Payment p = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (p.getStatus() != PaymentStatus.SUCCESS) {
                throw new RuntimeException("Receipt only available after successful payment");
            }
            
            BillingDTO.Party dealer = map(userClient.getUserById(p.getDealerId()));
            BillingDTO.Party farmer = map(userClient.getUserById(p.getFarmerId()));

            return BillingDTO.builder()
                    .title("PAYMENT RECEIPT")
                    .dealer(dealer)
                    .farmer(farmer)
                    .amount(p.getAmount())
                    .method(p.getPaymentMethod())
                    .status(p.getStatus())
                    .paymentId(p.getId())
                    .dealId(p.getDealId())
                    .transactionReference(p.getTransactionReference())
                    .createdAt(p.getCreatedAt())
                    .build();
        }

        private BillingDTO.Party map(UserServiceClient.UserDto u) {
            return BillingDTO.Party.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .phone(u.getPhone())
                    .address(u.getAddress())
                    .build();
        }
}