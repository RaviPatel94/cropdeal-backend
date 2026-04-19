package com.cropdeal.paymentservice.model;

import com.cropdeal.paymentservice.model.PaymentMethod;
import com.cropdeal.paymentservice.model.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Deal ID is required")
    @Column(nullable = false)
    private Long dealId;

    @NotNull(message = "Dealer ID is required")
    @Column(nullable = false)
    private Long dealerId;

    @NotNull(message = "Farmer ID is required")
    @Column(nullable = false)
    private Long farmerId;

    @NotNull
    @Positive(message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Size(max = 100)
    private String transactionReference;

    @Size(max = 255)
    private String failureReason;

    @CreationTimestamp
    private LocalDateTime createdAt;
}