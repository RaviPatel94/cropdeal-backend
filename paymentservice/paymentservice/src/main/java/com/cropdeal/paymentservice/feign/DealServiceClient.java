package com.cropdeal.paymentservice.feign;

import com.cropdeal.paymentservice.config.FeignConfig;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.math.BigDecimal;

@FeignClient(name = "deal-service", configuration = FeignConfig.class)
public interface DealServiceClient {

    @GetMapping("/api/deals/internal/{dealId}")
    DealDto getDealById(@PathVariable Long dealId);

    @PutMapping("/api/deals/internal/{dealId}/complete")
    void completeDeal(@PathVariable Long dealId);

    @Data
    class DealDto {
        private Long id;
        private Long dealerId;
        private Long farmerId;
        private Long listingId;
        private String cropName;
        private Double quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal totalAmount;
        private String status;
        private String remarks;
    }
}
