package com.cropdeal.notificationservice.dto;

import lombok.Data;

@Data
public class DealNotificationRequest {
    private Long dealId;
    private Long farmerId;
    private Long dealerId;
    private String cropName;
    private Double quantity;
    private String totalAmount;
    private String status;
}