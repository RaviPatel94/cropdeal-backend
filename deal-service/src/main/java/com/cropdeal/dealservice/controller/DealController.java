package com.cropdeal.dealservice.controller;

import com.cropdeal.dealservice.dto.DealRequest;
import com.cropdeal.dealservice.dto.DealResponse;
import com.cropdeal.dealservice.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping
    public ResponseEntity<DealResponse> createDeal(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody DealRequest request) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.createDeal(dealerId, request));
    }

    @PutMapping("/{dealId}/accept")
    public ResponseEntity<DealResponse> acceptDeal(
            @PathVariable Long dealId,
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.acceptDeal(dealId, farmerId));
    }

    @PutMapping("/{dealId}/reject")
    public ResponseEntity<DealResponse> rejectDeal(
            @PathVariable Long dealId,
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.rejectDeal(dealId, farmerId));
    }

    @PutMapping("/{dealId}/cancel")
    public ResponseEntity<DealResponse> cancelDeal(
            @PathVariable Long dealId,
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.cancelDeal(dealId, dealerId));
    }

    
    @GetMapping("/getDeals")
    public ResponseEntity<List<DealResponse>> getAllDeals(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.getDeals());
    }
    
    @GetMapping("/my/dealer")
    public ResponseEntity<List<DealResponse>> getMyDealsAsDealer(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.getDealsByDealer(dealerId));
    }

    @GetMapping("/my/farmer")
    public ResponseEntity<List<DealResponse>> getMyDealsAsFarmer(
            @RequestHeader("X-User-Id") Long farmerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("FARMER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.getDealsByFarmer(farmerId));
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<DealResponse> getDeal(
            @PathVariable Long dealId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER") && !role.equals("FARMER") && !role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.getDealById(dealId));
    }

    @GetMapping("/internal/{dealId}")
    public ResponseEntity<DealResponse> getDealInternal(
            @PathVariable Long dealId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.getDealByIdInternal(dealId));
    }

    @PutMapping("/internal/{dealId}/complete")
    public ResponseEntity<DealResponse> completeDeal(
            @PathVariable Long dealId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (!role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealService.completeDeal(dealId));
    }
}