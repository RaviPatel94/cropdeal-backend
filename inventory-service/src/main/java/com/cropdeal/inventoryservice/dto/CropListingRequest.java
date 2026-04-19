package com.cropdeal.inventoryservice.dto;

import com.cropdeal.inventoryservice.model.CropType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CropListingRequest {

    @NotNull
    private CropType cropType;

    @NotBlank
    private String cropName;

    @NotNull @Positive
    private Double quantity;

    @NotBlank
    private String quantityUnit;

    @NotNull @Positive
    private BigDecimal pricePerUnit;

    @NotBlank
    private String location;

    private String description;
}