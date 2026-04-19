package com.cropdeal.dealservice;

import com.cropdeal.dealservice.dto.DealRequest;
import com.cropdeal.dealservice.dto.DealResponse;
import com.cropdeal.dealservice.feign.InventoryServiceClient;
import com.cropdeal.dealservice.feign.NotificationServiceClient;
import com.cropdeal.dealservice.feign.UserServiceClient;
import com.cropdeal.dealservice.model.Deal;
import com.cropdeal.dealservice.model.DealStatus;
import com.cropdeal.dealservice.repository.DealRepository;
import com.cropdeal.dealservice.service.DealService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock private DealRepository dealRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private InventoryServiceClient inventoryServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private DealService dealService;

    private Deal mockDeal;
    private DealRequest mockRequest;
    private InventoryServiceClient.ListingDto mockListing;

    @BeforeEach
    void setUp() {
        mockDeal = Deal.builder()
                .id(1L)
                .dealerId(2L)
                .farmerId(1L)
                .listingId(1L)
                .quantity(100.0)
                .pricePerUnit(new BigDecimal("28.00"))
                .totalAmount(new BigDecimal("2800.00"))
                .status(DealStatus.PENDING)
                .build();

        mockRequest = new DealRequest();
        mockRequest.setListingId(1L);
        mockRequest.setFarmerId(1L);
        mockRequest.setQuantity(100.0);
        mockRequest.setPricePerUnit(new BigDecimal("28.00"));

        mockListing = new InventoryServiceClient.ListingDto();
        mockListing.setId(1L);
        mockListing.setCropName("Tomato");
        mockListing.setStatus("AVAILABLE");
        mockListing.setQuantity(100.0);
    }

    @Test
    void createDeal_ShouldSaveAndReturn() {
        when(inventoryServiceClient.getListingById(1L)).thenReturn(mockListing);
        when(dealRepository.save(any())).thenReturn(mockDeal);
        UserServiceClient.UserDto user = new UserServiceClient.UserDto();
        user.setName("Test");
        when(userServiceClient.getUserById(any())).thenReturn(user);

        DealResponse response = dealService.createDeal(2L, mockRequest);

        assertNotNull(response);
        assertEquals(DealStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("2800.00"), response.getTotalAmount());
        verify(dealRepository).save(any());
    }

    @Test
    void createDeal_UnavailableListing_ShouldThrowException() {
        mockListing.setStatus("SOLD");
        when(inventoryServiceClient.getListingById(1L)).thenReturn(mockListing);

        assertThrows(RuntimeException.class, () -> dealService.createDeal(2L, mockRequest));
        verify(dealRepository, never()).save(any());
    }

    @Test
    void acceptDeal_ShouldChangeToAccepted() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));
        when(dealRepository.save(any())).thenReturn(mockDeal);
        when(inventoryServiceClient.getListingById(any())).thenReturn(mockListing);

        dealService.acceptDeal(1L, 1L);

        verify(dealRepository).save(argThat(d -> d.getStatus() == DealStatus.ACCEPTED));
        verify(inventoryServiceClient).markAsSold(1L);
    }

    @Test
    void acceptDeal_WrongFarmer_ShouldThrowException() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));

        assertThrows(RuntimeException.class, () -> dealService.acceptDeal(1L, 99L));
        verify(dealRepository, never()).save(any());
    }

    @Test
    void rejectDeal_ShouldChangeToRejected() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));
        when(dealRepository.save(any())).thenReturn(mockDeal);
        when(inventoryServiceClient.getListingById(any())).thenReturn(mockListing);

        dealService.rejectDeal(1L, 1L);

        verify(dealRepository).save(argThat(d -> d.getStatus() == DealStatus.REJECTED));
    }

    @Test
    void rejectDeal_WrongFarmer_ShouldThrowException() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));

        assertThrows(RuntimeException.class, () -> dealService.rejectDeal(1L, 99L));
    }

    @Test
    void cancelDeal_ShouldChangeToCancelled() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));
        when(dealRepository.save(any())).thenReturn(mockDeal);

        dealService.cancelDeal(1L, 2L);

        verify(dealRepository).save(argThat(d -> d.getStatus() == DealStatus.CANCELLED));
    }

    @Test
    void cancelDeal_NonPendingDeal_ShouldThrowException() {
        mockDeal.setStatus(DealStatus.ACCEPTED);
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));

        assertThrows(RuntimeException.class, () -> dealService.cancelDeal(1L, 2L));
    }

    @Test
    void cancelDeal_WrongDealer_ShouldThrowException() {
        when(dealRepository.findById(1L)).thenReturn(Optional.of(mockDeal));

        assertThrows(RuntimeException.class, () -> dealService.cancelDeal(1L, 99L));
    }

    @Test
    void getDealsByDealer_ShouldReturnList() {
        when(dealRepository.findAllByDealerId(2L)).thenReturn(List.of(mockDeal));
        when(inventoryServiceClient.getListingById(anyLong())).thenReturn(mockListing);

        UserServiceClient.UserDto user = new UserServiceClient.UserDto();
        user.setName("Test");
        when(userServiceClient.getUserById(anyLong())).thenReturn(user);

        List<DealResponse> result = dealService.getDealsByDealer(2L);

        assertEquals(1, result.size());
        verify(dealRepository).findAllByDealerId(2L);
    }

    @Test
    void getDealsByFarmer_ShouldReturnList() {
        when(dealRepository.findAllByFarmerId(1L)).thenReturn(List.of(mockDeal));

        when(inventoryServiceClient.getListingById(anyLong())).thenReturn(mockListing);

        UserServiceClient.UserDto user = new UserServiceClient.UserDto();
        user.setName("Test");
        when(userServiceClient.getUserById(anyLong())).thenReturn(user);

        List<DealResponse> result = dealService.getDealsByFarmer(1L);

        assertEquals(1, result.size());
        verify(dealRepository).findAllByFarmerId(1L);
    }
}