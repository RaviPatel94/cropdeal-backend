package com.cropdeal.notificationservice.service;

import com.cropdeal.notificationservice.dto.CropListingNotificationRequest;
import com.cropdeal.notificationservice.dto.DealNotificationRequest;
import com.cropdeal.notificationservice.dto.SubscriptionRequest;
import com.cropdeal.notificationservice.model.Subscription;
import java.util.List;

public interface NotificationServiceI {
    Subscription subscribe(Long dealerId, SubscriptionRequest request);
    void unsubscribe(Long subscriptionId, Long dealerId);
    List<Subscription> getMySubscriptions(Long dealerId);
    void notifySubscribedDealers(CropListingNotificationRequest request);
    void sendDealNotification(DealNotificationRequest request);
}