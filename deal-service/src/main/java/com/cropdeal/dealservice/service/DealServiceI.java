package com.cropdeal.dealservice.service;

import com.cropdeal.dealservice.dto.DealRequest;
import com.cropdeal.dealservice.dto.DealResponse;
import java.util.List;

public interface DealServiceI {
    DealResponse createDeal(Long dealerId, DealRequest request);
    DealResponse acceptDeal(Long dealId, Long farmerId);
    DealResponse rejectDeal(Long dealId, Long farmerId);
    DealResponse completeDeal(Long dealId);
    DealResponse cancelDeal(Long dealId, Long dealerId);
    List<DealResponse> getDealsByDealer(Long dealerId);
    List<DealResponse> getDealsByFarmer(Long farmerId);
    DealResponse getDealById(Long dealId);
    DealResponse getDealByIdInternal(Long dealId);
}