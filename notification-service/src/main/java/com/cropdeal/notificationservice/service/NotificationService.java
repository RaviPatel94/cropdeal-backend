package com.cropdeal.notificationservice.service;

import com.cropdeal.notificationservice.dto.*;
import com.cropdeal.notificationservice.feign.UserServiceClient;
import com.cropdeal.notificationservice.model.Subscription;
import com.cropdeal.notificationservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationServiceI{

    private final SubscriptionRepository subscriptionRepository;
    private final EmailServiceI emailService;
    private final UserServiceClient userServiceClient;

    public Subscription subscribe(Long dealerId, SubscriptionRequest request) {
        if (subscriptionRepository.existsByDealerIdAndCropNameIgnoreCase(dealerId, request.getCropName())) {
            throw new RuntimeException("Already subscribed to " + request.getCropName());
        }
        Subscription subscription = Subscription.builder()
                .dealerId(dealerId)
                .dealerEmail(request.getDealerEmail())
                .cropName(request.getCropName())
                .cropType(request.getCropType())
                .build();
        return subscriptionRepository.save(subscription);
    }

    public void unsubscribe(Long subscriptionId, Long dealerId) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        if (!sub.getDealerId().equals(dealerId)) {
            throw new RuntimeException("Not authorized");
        }
        subscriptionRepository.delete(sub);
    }

    public List<Subscription> getMySubscriptions(Long dealerId) {
        return subscriptionRepository.findAllByDealerId(dealerId);
    }

    public void notifySubscribedDealers(CropListingNotificationRequest request) {
        List<Subscription> subscribers =
                subscriptionRepository.findAllByCropNameIgnoreCase(request.getCropName());

        for (Subscription sub : subscribers) {
            String subject = "New Crop Available: " + request.getCropName();
            String body = String.format(
                "Hello,\n\nA new crop listing is available:\n\n" +
                "Crop: %s\nType: %s\nQuantity: %.2f kg\nLocation: %s\nFarmer: %s\n\n" +
                "Login to CropDeal to view details.\n\nRegards,\nCropDeal Team",
                request.getCropName(), request.getCropType(),
                request.getQuantity(), request.getLocation(), request.getFarmerName()
            );
            sendEmail(sub.getDealerEmail(), subject, body);
        }
    }

    public void sendDealNotification(DealNotificationRequest request) {
        try {
            UserServiceClient.UserDto farmer = userServiceClient.getUserById(request.getFarmerId());
            UserServiceClient.UserDto dealer = userServiceClient.getUserById(request.getDealerId());

            String subject = "Deal " + request.getStatus() + " - " + request.getCropName();
            String body = String.format(
                "Deal Update\n\nDeal ID: %d\nCrop: %s\nQuantity: %.2f\nTotal Amount: ₹%s\nStatus: %s\n\nRegards,\nCropDeal Team",
                request.getDealId(), request.getCropName(),
                request.getQuantity(), request.getTotalAmount(), request.getStatus()
            );

            sendEmail(farmer.getEmail(), subject, body);
            sendEmail(dealer.getEmail(), subject, body);

        } catch (Exception e) {
            // log silently
        }
    }

    private void sendEmail(String email, String subject, String body) {
        try {
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            // log silently
        }
    }
}