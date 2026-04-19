package com.cropdeal.dealservice.service;

import com.cropdeal.dealservice.dto.DealRequest;
import com.cropdeal.dealservice.dto.DealResponse;
import com.cropdeal.dealservice.feign.InventoryServiceClient;
import com.cropdeal.dealservice.feign.NotificationServiceClient;
import com.cropdeal.dealservice.feign.UserServiceClient;
import com.cropdeal.dealservice.model.Deal;
import com.cropdeal.dealservice.model.DealStatus;
import com.cropdeal.dealservice.repository.DealRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DealService implements DealServiceI {

    private final DealRepository dealRepository;
    private final UserServiceClient userServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // Dealer initiates a deal
    public DealResponse createDeal(Long dealerId, DealRequest request) {
        InventoryServiceClient.ListingDto listing =
                inventoryServiceClient.getListingById(request.getListingId());

        if (!listing.getStatus().equals("AVAILABLE")) {
            throw new RuntimeException("Listing is not available");
        }
        if(request.getQuantity()>listing.getQuantity()) {
        	throw new RuntimeException("Quantity cant be bigger than inventory");
        }

        BigDecimal total = request.getPricePerUnit()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Deal deal = Deal.builder()
                .dealerId(dealerId)
                .farmerId(request.getFarmerId())
                .listingId(request.getListingId())
                .quantity(request.getQuantity())
                .pricePerUnit(request.getPricePerUnit())
                .totalAmount(total)
                .remarks(request.getRemarks())
                .status(DealStatus.PENDING)
                .build();

        return mapToResponse(dealRepository.save(deal));
    }

    // Farmer accepts deal
    public DealResponse acceptDeal(Long dealId, Long farmerId) {
        Deal deal = findById(dealId);
        if (!deal.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }
        deal.setStatus(DealStatus.ACCEPTED);
        Deal saved = dealRepository.save(deal);

        // mark listing as sold
        inventoryServiceClient.markAsSold(deal.getListingId());

        // notify both parties
        sendNotification(saved, "ACCEPTED");

        return mapToResponse(saved);
    }

    // Farmer rejects deal
    public DealResponse rejectDeal(Long dealId, Long farmerId) {
        Deal deal = findById(dealId);
        if (!deal.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("Not authorized");
        }
        deal.setStatus(DealStatus.REJECTED);
        Deal saved = dealRepository.save(deal);
        sendNotification(saved, "REJECTED");
        return mapToResponse(saved);
    }

    // Mark deal completed after payment
    public DealResponse completeDeal(Long dealId) {
        Deal deal = findById(dealId);
        deal.setStatus(DealStatus.COMPLETED);
        Deal saved = dealRepository.save(deal);
        sendNotification(saved, "COMPLETED");
        return mapToResponse(saved);
    }

    public DealResponse cancelDeal(Long dealId, Long dealerId) {
        Deal deal = findById(dealId);
        if (!deal.getDealerId().equals(dealerId)) {
            throw new RuntimeException("Not authorized");
        }
        if (deal.getStatus() != DealStatus.PENDING) {
            throw new RuntimeException("Only pending deals can be cancelled");
        }
        deal.setStatus(DealStatus.CANCELLED);
        return mapToResponse(dealRepository.save(deal));
    }

    public List<DealResponse> getDealsByDealer(Long dealerId) {
        return dealRepository.findAllByDealerId(dealerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    public List<DealResponse> getDeals() {
        return dealRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<DealResponse> getDealsByFarmer(Long farmerId) {
        return dealRepository.findAllByFarmerId(farmerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public DealResponse getDealById(Long dealId) {
        return mapToResponse(findById(dealId));
    }

    // Internal - called by payment-service
    public DealResponse getDealByIdInternal(Long dealId) {
        return mapToResponse(findById(dealId));
    }

    private void sendNotification(Deal deal, String status) {
        try {
            InventoryServiceClient.ListingDto listing =
                    inventoryServiceClient.getListingById(deal.getListingId());
            NotificationServiceClient.DealNotificationRequest notif =
                    new NotificationServiceClient.DealNotificationRequest();
            notif.setDealId(deal.getId());
            notif.setFarmerId(deal.getFarmerId());
            notif.setDealerId(deal.getDealerId());
            notif.setCropName(listing.getCropName());
            notif.setQuantity(deal.getQuantity());
            notif.setTotalAmount(deal.getTotalAmount().toString());
            notif.setStatus(status);
            notificationServiceClient.sendDealNotification(notif);
        } catch (Exception e) {
            // don't fail the deal if notification fails
        }
    }

    private Deal findById(Long id) {
        return dealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deal not found: " + id));
    }

    private DealResponse mapToResponse(Deal deal) {
        DealResponse response = new DealResponse();
        response.setId(deal.getId());
        response.setDealerId(deal.getDealerId());
        response.setFarmerId(deal.getFarmerId());
        response.setListingId(deal.getListingId());
        response.setQuantity(deal.getQuantity());
        response.setPricePerUnit(deal.getPricePerUnit());
        response.setTotalAmount(deal.getTotalAmount());
        response.setStatus(deal.getStatus());
        response.setRemarks(deal.getRemarks());
        response.setCreatedAt(deal.getCreatedAt());

        try {
            response.setDealerName(userServiceClient.getUserById(deal.getDealerId()).getName());
            response.setFarmerName(userServiceClient.getUserById(deal.getFarmerId()).getName());
            response.setCropName(inventoryServiceClient.getListingById(deal.getListingId()).getCropName());
        } catch (Exception e) {
            // fallback silently
        }
        return response;
    }
}