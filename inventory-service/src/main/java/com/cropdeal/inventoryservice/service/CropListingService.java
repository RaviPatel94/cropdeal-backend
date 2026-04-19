package com.cropdeal.inventoryservice.service;

import com.cropdeal.inventoryservice.dto.CropListingRequest;
import com.cropdeal.inventoryservice.dto.CropListingResponse;
import com.cropdeal.inventoryservice.feign.NotificationServiceClient;
import com.cropdeal.inventoryservice.feign.UserServiceClient;
import com.cropdeal.inventoryservice.model.CropListing;
import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.model.ListingStatus;
import com.cropdeal.inventoryservice.repository.CropListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CropListingService implements CropListingServiceI {

    private final CropListingRepository cropListingRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // Farmer posts a new crop listing
    public CropListingResponse createListing(Long farmerId, CropListingRequest request) {
        CropListing listing = CropListing.builder()
                .farmerId(farmerId)
                .cropType(request.getCropType())
                .cropName(request.getCropName())
                .quantity(request.getQuantity())
                .quantityUnit(request.getQuantityUnit())
                .pricePerUnit(request.getPricePerUnit())
                .location(request.getLocation())
                .description(request.getDescription())
                .status(ListingStatus.AVAILABLE)
                .build();
        try {
            NotificationServiceClient.CropListingNotificationRequest notif =
                    new NotificationServiceClient.CropListingNotificationRequest();
            notif.setListingId(listing.getId());
            notif.setCropName(listing.getCropName());
            notif.setCropType(listing.getCropType().name());
            notif.setQuantity(listing.getQuantity());
            notif.setLocation(listing.getLocation());
            notif.setFarmerName(userServiceClient.getUserById(farmerId).getName());
			notificationServiceClient.notifyNewListing(notif);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }
        return mapToResponse(cropListingRepository.save(listing));
    }

    // Get all listings by a specific farmer
    public List<CropListingResponse> getListingsByFarmer(Long farmerId) {
        return cropListingRepository.findAllByFarmerId(farmerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    public List<CropListingResponse> getAllListings() {
        return cropListingRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Get all available listings (for dealers to browse)
    public List<CropListingResponse> getAllAvailableListings() {
        return cropListingRepository.findAllByStatus(ListingStatus.AVAILABLE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Filter by crop type (VEGETABLE or FRUIT)
    public List<CropListingResponse> getListingsByCropType(CropType cropType) {
        return cropListingRepository.findAllByCropTypeAndStatus(cropType, ListingStatus.AVAILABLE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Filter by crop name (e.g. "Tomato")
    public List<CropListingResponse> getListingsByCropName(String cropName) {
        return cropListingRepository.findAllByCropNameIgnoreCaseAndStatus(cropName, ListingStatus.AVAILABLE)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // Get single listing
    public CropListingResponse getListingById(Long id) {
        return mapToResponse(findById(id));
    }

    // Farmer edits their listing
    public CropListingResponse updateListing(Long id, Long farmerId, CropListingRequest request) {
        CropListing listing = findById(id);
        if (!listing.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("You are not authorized to edit this listing");
        }
        listing.setCropType(request.getCropType());
        listing.setCropName(request.getCropName());
        listing.setQuantity(request.getQuantity());
        listing.setQuantityUnit(request.getQuantityUnit());
        listing.setPricePerUnit(request.getPricePerUnit());
        listing.setLocation(request.getLocation());
        listing.setDescription(request.getDescription());
        return mapToResponse(cropListingRepository.save(listing));
    }

    // Farmer deletes/cancels listing
    public void cancelListing(Long id, Long farmerId) {
        CropListing listing = findById(id);
        if (!listing.getFarmerId().equals(farmerId)) {
            throw new RuntimeException("You are not authorized to cancel this listing");
        }
        listing.setStatus(ListingStatus.CANCELLED);
        cropListingRepository.save(listing);
    }

    // Called internally by deal-service when deal is confirmed
    public void markAsSold(Long listingId) {
        CropListing listing = findById(listingId);
        listing.setStatus(ListingStatus.SOLD);
        cropListingRepository.save(listing);
    }

    private CropListing findById(Long id) {
        return cropListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found: " + id));
    }

    private CropListingResponse mapToResponse(CropListing listing) {
        CropListingResponse response = new CropListingResponse();
        response.setId(listing.getId());
        response.setFarmerId(listing.getFarmerId());
        response.setCropType(listing.getCropType());
        response.setCropName(listing.getCropName());
        response.setQuantity(listing.getQuantity());
        response.setQuantityUnit(listing.getQuantityUnit());
        response.setPricePerUnit(listing.getPricePerUnit());
        response.setLocation(listing.getLocation());
        response.setDescription(listing.getDescription());
        response.setStatus(listing.getStatus());
        response.setCreatedAt(listing.getCreatedAt());

        // fetch farmer name from user-service
        try {
            UserServiceClient.UserDto farmer = userServiceClient.getUserById(listing.getFarmerId());
            response.setFarmerName(farmer.getName());
        } catch (Exception e) {
            response.setFarmerName("Unknown");
        }

        return response;
    }
}