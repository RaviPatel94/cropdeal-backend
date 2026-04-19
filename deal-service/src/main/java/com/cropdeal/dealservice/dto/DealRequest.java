package com.cropdeal.dealservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DealRequest {
    @NotNull 
    private Long listingId;
    
    @NotNull 
    private Long farmerId;
    
    @NotNull 
    @Positive 
    private Double quantity;
    
    @NotNull 
    @Positive 
    private BigDecimal pricePerUnit;
    
    private String remarks;
}