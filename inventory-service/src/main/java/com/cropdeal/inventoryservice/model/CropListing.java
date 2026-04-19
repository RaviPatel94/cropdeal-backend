package com.cropdeal.inventoryservice.model;

import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.model.ListingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_listings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CropListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Farmer ID is required")
    @Column(nullable = false)
    private Long farmerId;

    @NotNull(message = "Crop type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropType cropType;

    @NotBlank(message = "Crop name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false)
    private String cropName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    @Column(nullable = false)
    private Double quantity;

    @NotBlank(message = "Quantity unit is required")
    @Column(nullable = false)
    private String quantityUnit;

    @NotNull(message = "Price per unit is required")
    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @NotBlank(message = "Location is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String location;

    @Size(max = 500, message = "Description too long")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ListingStatus status = ListingStatus.AVAILABLE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}