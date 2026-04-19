package com.cropdeal.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Deal ID is required")
    private Long dealId;
}
