package com.cropdeal.inventoryservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.Data;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/crop-listing")
    void notifyNewListing(@RequestBody CropListingNotificationRequest request);

    @Data
    class CropListingNotificationRequest {
        private Long listingId;
        private String cropName;
        private String cropType;
        private Double quantity;
        private String location;
        private String farmerName;
    }
}