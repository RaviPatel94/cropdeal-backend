package com.cropdeal.paymentservice.controller;

import com.cropdeal.paymentservice.dto.BillingDTO;
import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.service.PaymentService;
import com.cropdeal.paymentservice.service.PaymentServiceI;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentServiceI paymentService;
    private final PaymentService paymentServiceImpl;

    @Operation(summary = "Initiate payment for an accepted deal (Dealer only)")
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody PaymentRequest request) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.initiatePayment(dealerId, request));
    }

    @Operation(summary = "Get payment details by deal ID")
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<PaymentResponse> getPaymentByDeal(
            @PathVariable Long dealId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("FARMER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getPaymentByDeal(dealId));
    }
    
@GetMapping("/verify")
public ResponseEntity<PaymentResponse> verifyPayment(
        @RequestParam String sessionId,
        @RequestHeader("X-User-Role") String role) {
 
    if (!role.equals("DEALER") && !role.equals("FARMER") && !role.equals("ADMIN")) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
    try {
        return ResponseEntity.ok(paymentServiceImpl.verifyPayment(sessionId));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}

    @Operation(summary = "Get all payments made by dealer")
    @GetMapping("/my/dealer")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentsAsDealer(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getPaymentsByDealer(dealerId));
    }
    
    @Operation(summary = "Get all payments")
    @GetMapping("allPays")
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @Operation(summary = "Get all payments received by farmer")
    @GetMapping("/my/farmer")
    public ResponseEntity<List<PaymentResponse>> getMyPaymentsAsFarmer(
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(paymentService.getPaymentsByFarmer(farmerId));
    }
    

    @GetMapping("/invoice/{paymentId}")
    public ResponseEntity<BillingDTO> getInvoice(
            @PathVariable Long paymentId,
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("ADMIN")) {
        	 return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BillingDTO invoice = paymentService.generateInvoice(paymentId);
        return ResponseEntity.ok(invoice);
    }
    

    @GetMapping("/recipt/{paymentId}")
    public ResponseEntity<?> getReceipt(
            @PathVariable Long paymentId,
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER") && !role.equals("ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        BillingDTO receipt = paymentService.generateReceipt(paymentId);
        return ResponseEntity.ok(receipt);
    }


}