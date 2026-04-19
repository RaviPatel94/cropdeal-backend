package com.cropdeal.notificationservice;

import com.cropdeal.notificationservice.dto.*;
import com.cropdeal.notificationservice.feign.UserServiceClient;
import com.cropdeal.notificationservice.model.Subscription;
import com.cropdeal.notificationservice.repository.SubscriptionRepository;
import com.cropdeal.notificationservice.service.EmailServiceI;
import com.cropdeal.notificationservice.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private EmailServiceI emailService;
    @Mock private UserServiceClient userServiceClient;

    @InjectMocks
    private NotificationService notificationService;

    private Subscription mockSubscription;

    @BeforeEach
    void setUp() {
        mockSubscription = Subscription.builder()
                .id(1L)
                .dealerId(2L)
                .dealerEmail("rakpanda8@gmail.com")
                .cropName("Tomato")
                .cropType("VEGETABLE")
                .build();
    }

    @Test
    void subscribe_ShouldSave() {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCropName("Tomato");
        request.setCropType("VEGETABLE");
        request.setDealerEmail("rakpanda8@gmail.com");

        when(subscriptionRepository.existsByDealerIdAndCropNameIgnoreCase(2L, "Tomato"))
                .thenReturn(false);
        when(subscriptionRepository.save(any())).thenReturn(mockSubscription);

        Subscription result = notificationService.subscribe(2L, request);

        assertNotNull(result);
        assertEquals("Tomato", result.getCropName());
        verify(subscriptionRepository).save(any());
    }

    @Test
    void subscribe_Duplicate_ShouldThrowException() {
        SubscriptionRequest request = new SubscriptionRequest();
        request.setCropName("Tomato");

        when(subscriptionRepository.existsByDealerIdAndCropNameIgnoreCase(2L, "Tomato"))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> notificationService.subscribe(2L, request));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void unsubscribe_ShouldDelete() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(mockSubscription));

        notificationService.unsubscribe(1L, 2L);

        verify(subscriptionRepository).delete(mockSubscription);
    }

    @Test
    void unsubscribe_WrongDealer_ShouldThrowException() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(mockSubscription));

        assertThrows(RuntimeException.class,
                () -> notificationService.unsubscribe(1L, 99L));
        verify(subscriptionRepository, never()).delete(any());
    }

    @Test
    void notifySubscribedDealers_ShouldSendEmail() {
        CropListingNotificationRequest request = new CropListingNotificationRequest();
        request.setCropName("Tomato");
        request.setCropType("VEGETABLE");
        request.setQuantity(500.0);
        request.setLocation("Nashik");
        request.setFarmerName("Ramesh");

        when(subscriptionRepository.findAllByCropNameIgnoreCase("Tomato"))
                .thenReturn(List.of(mockSubscription));

        notificationService.notifySubscribedDealers(request);

        verify(emailService).sendEmail(
                eq("rakpanda8@gmail.com"), anyString(), anyString());
    }

    @Test
    void notifySubscribedDealers_NoSubscribers_ShouldNotSendEmail() {
        CropListingNotificationRequest request = new CropListingNotificationRequest();
        request.setCropName("Broccoli");

        when(subscriptionRepository.findAllByCropNameIgnoreCase("Broccoli"))
                .thenReturn(List.of());

        notificationService.notifySubscribedDealers(request);

        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void sendDealNotification_ShouldEmailBothParties() {
        DealNotificationRequest request = new DealNotificationRequest();
        request.setDealId(1L);
        request.setFarmerId(1L);
        request.setDealerId(2L);
        request.setCropName("Tomato");
        request.setQuantity(100.0);
        request.setTotalAmount("2800.00");
        request.setStatus("ACCEPTED");

        UserServiceClient.UserDto farmer = new UserServiceClient.UserDto();
        farmer.setEmail("ravi404err@gmail.com");
        UserServiceClient.UserDto dealer = new UserServiceClient.UserDto();
        dealer.setEmail("rakpanda8@gmail.com");

        when(userServiceClient.getUserById(1L)).thenReturn(farmer);
        when(userServiceClient.getUserById(2L)).thenReturn(dealer);

        notificationService.sendDealNotification(request);

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }
}