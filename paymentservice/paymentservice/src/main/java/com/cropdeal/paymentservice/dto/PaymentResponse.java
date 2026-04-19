package com.cropdeal.paymentservice.dto;
 
import com.cropdeal.paymentservice.model.PaymentMethod;
import com.cropdeal.paymentservice.model.PaymentStatus;
import lombok.Data;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Data
public class PaymentResponse {
    private Long id;
    private Long dealId;
    private Long dealerId;
    private Long farmerId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String failureReason;
    private String paymentUrl;       // stripe checkout link - frontend redirects here
    private LocalDateTime createdAt;
}