package com.cropdeal.paymentservice.dto;

import com.cropdeal.paymentservice.model.PaymentMethod;
import com.cropdeal.paymentservice.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BillingDTO {

    private String title; // INVOICE or RECEIPT

    // Party info
    private Party dealer;
    private Party farmer;

    // Payment info
    private Long paymentId;
    private Long dealId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime createdAt;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Party {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
    }
}