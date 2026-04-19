package com.cropdeal.dealservice.model;

import com.cropdeal.dealservice.model.DealStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deals")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Dealer ID is required")
    @Column(nullable = false)
    private Long dealerId;

    @NotNull(message = "Farmer ID is required")
    @Column(nullable = false)
    private Long farmerId;

    @NotNull(message = "Listing ID is required")
    @Column(nullable = false)
    private Long listingId;

    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotNull
    @Positive(message = "Price must be greater than 0")
    private BigDecimal pricePerUnit;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DealStatus status = DealStatus.PENDING;

    @Size(max = 500, message = "Remarks too long")
    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}