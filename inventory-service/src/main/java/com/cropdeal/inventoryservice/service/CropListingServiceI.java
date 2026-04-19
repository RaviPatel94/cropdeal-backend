package com.cropdeal.inventoryservice.service;

import com.cropdeal.inventoryservice.dto.CropListingRequest;
import com.cropdeal.inventoryservice.dto.CropListingResponse;
import com.cropdeal.inventoryservice.model.CropType;
import java.util.List;

public interface CropListingServiceI {
    CropListingResponse createListing(Long farmerId, CropListingRequest request);
    List<CropListingResponse> getListingsByFarmer(Long farmerId);
    List<CropListingResponse> getAllAvailableListings();
    List<CropListingResponse> getListingsByCropType(CropType cropType);
    List<CropListingResponse> getListingsByCropName(String cropName);
    CropListingResponse getListingById(Long id);
    CropListingResponse updateListing(Long id, Long farmerId, CropListingRequest request);
    void cancelListing(Long id, Long farmerId);
    void markAsSold(Long listingId);
}