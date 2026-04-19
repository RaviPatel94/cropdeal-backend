package com.cropdeal.dealservice.feign;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/deal")
    void sendDealNotification(@RequestBody DealNotificationRequest request);

    @Data
    class DealNotificationRequest {
        private Long dealId;
        private Long farmerId;
        private Long dealerId;
        private String cropName;
        private Double quantity;
        private String totalAmount;
        private String status;
    }
}