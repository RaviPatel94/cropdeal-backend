package com.cropdeal.eureka_server;

import com.cropdeal.inventoryservice.dto.CropListingRequest;
import com.cropdeal.inventoryservice.dto.CropListingResponse;
import com.cropdeal.inventoryservice.feign.NotificationServiceClient;
import com.cropdeal.inventoryservice.feign.UserServiceClient;
import com.cropdeal.inventoryservice.model.CropListing;
import com.cropdeal.inventoryservice.model.CropType;
import com.cropdeal.inventoryservice.model.ListingStatus;
import com.cropdeal.inventoryservice.repository.CropListingRepository;
import com.cropdeal.inventoryservice.service.CropListingService;

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
class CropListingServiceTest {

    @Mock private CropListingRepository cropListingRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private CropListingService cropListingService;

    private CropListing mockListing;
    private CropListingRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockListing = CropListing.builder()
                .id(1L)
                .farmerId(1L)
                .cropType(CropType.VEGETABLE)
                .cropName("Tomato")
                .quantity(500.0)
                .quantityUnit("kg")
                .pricePerUnit(new BigDecimal("25.00"))
                .location("Nashik")
                .status(ListingStatus.AVAILABLE)
                .build();

        mockRequest = new CropListingRequest();
        mockRequest.setCropType(CropType.VEGETABLE);
        mockRequest.setCropName("Tomato");
        mockRequest.setQuantity(500.0);
        mockRequest.setQuantityUnit("kg");
        mockRequest.setPricePerUnit(new BigDecimal("25.00"));
        mockRequest.setLocation("Nashik");
    }

    @Test
    void createListing_ShouldSaveAndReturn() {
        when(cropListingRepository.save(any())).thenReturn(mockListing);
        UserServiceClient.UserDto farmer = new UserServiceClient.UserDto();
        farmer.setName("Ramesh");
        when(userServiceClient.getUserById(1L)).thenReturn(farmer);

        CropListingResponse response = cropListingService.createListing(1L, mockRequest);

        assertNotNull(response);
        assertEquals("Tomato", response.getCropName());
        assertEquals(ListingStatus.AVAILABLE, response.getStatus());
        verify(cropListingRepository).save(any());
    }

    @Test
    void getAllAvailableListings_ShouldReturnAvailable() {
        when(cropListingRepository.findAllByStatus(ListingStatus.AVAILABLE))
                .thenReturn(List.of(mockListing));
        UserServiceClient.UserDto farmer = new UserServiceClient.UserDto();
        farmer.setName("Ramesh");
        when(userServiceClient.getUserById(any())).thenReturn(farmer);

        List<CropListingResponse> result = cropListingService.getAllAvailableListings();

        assertEquals(1, result.size());
        assertEquals(ListingStatus.AVAILABLE, result.get(0).getStatus());
    }

    @Test
    void updateListing_WrongFarmer_ShouldThrowException() {
        when(cropListingRepository.findById(1L)).thenReturn(Optional.of(mockListing));

        assertThrows(RuntimeException.class,
                () -> cropListingService.updateListing(1L, 99L, mockRequest));
    }

    @Test
    void updateListing_CorrectFarmer_ShouldUpdate() {
        when(cropListingRepository.findById(1L)).thenReturn(Optional.of(mockListing));
        when(cropListingRepository.save(any())).thenReturn(mockListing);
        UserServiceClient.UserDto farmer = new UserServiceClient.UserDto();
        farmer.setName("Ramesh");
        when(userServiceClient.getUserById(any())).thenReturn(farmer);

        CropListingResponse result = cropListingService.updateListing(1L, 1L, mockRequest);

        assertNotNull(result);
        verify(cropListingRepository).save(any());
    }

    @Test
    void cancelListing_ShouldSetCancelled() {
        when(cropListingRepository.findById(1L)).thenReturn(Optional.of(mockListing));
        when(cropListingRepository.save(any())).thenReturn(mockListing);

        cropListingService.cancelListing(1L, 1L);

        verify(cropListingRepository).save(argThat(l -> l.getStatus() == ListingStatus.CANCELLED));
    }

    @Test
    void cancelListing_WrongFarmer_ShouldThrowException() {
        when(cropListingRepository.findById(1L)).thenReturn(Optional.of(mockListing));

        assertThrows(RuntimeException.class,
                () -> cropListingService.cancelListing(1L, 99L));
    }

    @Test
    void markAsSold_ShouldSetSold() {
        when(cropListingRepository.findById(1L)).thenReturn(Optional.of(mockListing));
        when(cropListingRepository.save(any())).thenReturn(mockListing);

        cropListingService.markAsSold(1L);

        verify(cropListingRepository).save(argThat(l -> l.getStatus() == ListingStatus.SOLD));
    }

    @Test
    void getListingById_InvalidId_ShouldThrowException() {
        when(cropListingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cropListingService.getListingById(99L));
    }

    @Test
    void getListingsByFarmer_ShouldReturnFarmerListings() {
        when(cropListingRepository.findAllByFarmerId(1L)).thenReturn(List.of(mockListing));
        UserServiceClient.UserDto farmer = new UserServiceClient.UserDto();
        farmer.setName("Ramesh");
        when(userServiceClient.getUserById(any())).thenReturn(farmer);

        List<CropListingResponse> result = cropListingService.getListingsByFarmer(1L);

        assertEquals(1, result.size());
    }
}