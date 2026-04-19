package com.cropdeal.inventoryservice.repository;

import com.cropdeal.inventoryservice.model.CropListing;
import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.model.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropListingRepository extends JpaRepository<CropListing, Long> {
    List<CropListing> findAllByFarmerId(Long farmerId);
    List<CropListing> findAllByStatus(ListingStatus status);
    List<CropListing> findAllByCropType(CropType cropType);
    List<CropListing> findAllByCropTypeAndStatus(CropType cropType, ListingStatus status);
    List<CropListing> findAllByCropNameIgnoreCaseAndStatus(String cropName, ListingStatus status);
}