package com.cropdeal.notificationservice.dto;

import lombok.Data;

@Data
public class CropListingNotificationRequest {
    private Long listingId;
    private String cropName;
    private String cropType;
    private Double quantity;
    private String location;
    private String farmerName;
}