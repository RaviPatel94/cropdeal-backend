package com.cropdeal.dealservice.feign;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.math.BigDecimal;

@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/listings/{id}")
    ListingDto getListingById(@PathVariable Long id);

    @PutMapping("/api/inventory/listings/{id}/sold")
    void markAsSold(@PathVariable Long id);

    @Data
    class ListingDto {
        private Long id;
        private Long farmerId;
        private String cropName;
        private Double quantity;
        private BigDecimal pricePerUnit;
        private String status;
    }
}