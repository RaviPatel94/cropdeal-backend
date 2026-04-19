package com.cropdeal.inventoryservice.controller;

import com.cropdeal.inventoryservice.dto.CropListingRequest;
import com.cropdeal.inventoryservice.dto.CropListingResponse;
import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.service.CropListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class CropListingController {

    private final CropListingService cropListingService;

    @PostMapping("/listings")
    public ResponseEntity<CropListingResponse> createListing(
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CropListingRequest request) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.createListing(farmerId, request));
    }

    @GetMapping("/listings/my")
    public ResponseEntity<List<CropListingResponse>> getMyListings(
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getListingsByFarmer(farmerId));
    }

    @PutMapping("/listings/{id}")
    public ResponseEntity<CropListingResponse> updateListing(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CropListingRequest request) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.updateListing(id, farmerId, request));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<String> cancelListing(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        cropListingService.cancelListing(id, farmerId);
        return ResponseEntity.ok("Listing cancelled");
    }

    @GetMapping("/listings")
    public ResponseEntity<List<CropListingResponse>> getAllAvailable(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getAllAvailableListings());
    }

    @GetMapping("/listings/type/{cropType}")
    public ResponseEntity<List<CropListingResponse>> getByType(
            @PathVariable CropType cropType,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getListingsByCropType(cropType));
    }

    @GetMapping("/listings/search")
    public ResponseEntity<List<CropListingResponse>> searchByCropName(
            @RequestParam String name,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getListingsByCropName(name));
    }

    @GetMapping("/listings/{id}")
    public ResponseEntity<CropListingResponse> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getListingById(id));
    }

    @PutMapping("/listings/{id}/sold")
    public ResponseEntity<Void> markAsSold(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        cropListingService.markAsSold(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/getAll")
    public ResponseEntity<List<CropListingResponse>> getAllListings(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(cropListingService.getAllListings());
    }
}