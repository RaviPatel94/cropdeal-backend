package com.cropdeal.dealservice.dto;

import com.cropdeal.dealservice.model.DealStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DealResponse {
    private Long id;
    private Long dealerId;
    private String dealerName;
    private Long farmerId;
    private String farmerName;
    private Long listingId;
    private String cropName;
    private Double quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
    private DealStatus status;
    private String remarks;
    private LocalDateTime createdAt;
}