package com.cropdeal.inventoryservice.dto;

import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.model.ListingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CropListingResponse {
    private Long id;
    private Long farmerId;
    private String farmerName;        // fetched from user-service
    private CropType cropType;
    private String cropName;
    private Double quantity;
    private String quantityUnit;
    private BigDecimal pricePerUnit;
    private String location;
    private String description;
    private ListingStatus status;
    private LocalDateTime createdAt;
}