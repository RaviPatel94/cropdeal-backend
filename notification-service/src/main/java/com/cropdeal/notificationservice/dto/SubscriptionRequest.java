package com.cropdeal.notificationservice.dto;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private String cropName;
    private String cropType;
    private String dealerEmail;
}