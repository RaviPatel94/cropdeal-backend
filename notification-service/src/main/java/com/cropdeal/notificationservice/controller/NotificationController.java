package com.cropdeal.notificationservice.controller;

import com.cropdeal.notificationservice.dto.*;
import com.cropdeal.notificationservice.model.Subscription;
import com.cropdeal.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody SubscriptionRequest request) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.subscribe(dealerId, request));
    }

    @DeleteMapping("/subscribe/{subscriptionId}")
    public ResponseEntity<String> unsubscribe(
            @PathVariable Long subscriptionId,
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.unsubscribe(subscriptionId, dealerId);
        return ResponseEntity.ok("Unsubscribed");
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getMySubscriptions(
            @RequestHeader("X-User-Id") Long dealerId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("DEALER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(notificationService.getMySubscriptions(dealerId));
    }

    @PostMapping("/crop-listing")
    public ResponseEntity<Void> notifyNewListing(
            @RequestBody CropListingNotificationRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (role == null || !role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.notifySubscribedDealers(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deal")
    public ResponseEntity<Void> sendDealNotification(
            @RequestBody DealNotificationRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        if (role == null || !role.equals("INTERNAL")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.sendDealNotification(request);
        return ResponseEntity.ok().build();
    }
}