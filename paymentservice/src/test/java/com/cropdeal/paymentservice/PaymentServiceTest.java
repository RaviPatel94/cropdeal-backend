package com.cropdeal.paymentservice;

import com.cropdeal.paymentservice.dto.BillingDTO;
import com.cropdeal.paymentservice.dto.PaymentRequest;
import com.cropdeal.paymentservice.dto.PaymentResponse;
import com.cropdeal.paymentservice.feign.DealServiceClient;
import com.cropdeal.paymentservice.feign.UserServiceClient;
import com.cropdeal.paymentservice.model.Payment;
import com.cropdeal.paymentservice.model.PaymentMethod;
import com.cropdeal.paymentservice.model.PaymentStatus;
import com.cropdeal.paymentservice.repository.PaymentRepository;
import com.cropdeal.paymentservice.service.PaymentService;
import com.cropdeal.paymentservice.service.StripePaymentResult;
import com.cropdeal.paymentservice.service.StripeServiceI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private DealServiceClient dealServiceClient;
    private StripeServiceI stripeService;
    private UserServiceClient userClient;
    private PaymentService paymentService;

    private Payment mockPayment;
    private DealServiceClient.DealDto mockDeal;
    private PaymentRequest mockRequest;
    private StripePaymentResult mockStripeResult;
    private UserServiceClient.UserDto mockDealer;
    private UserServiceClient.UserDto mockFarmer;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        dealServiceClient = mock(DealServiceClient.class);
        stripeService = mock(StripeServiceI.class);
        userClient = mock(UserServiceClient.class);

        // updated constructor with userClient
        paymentService = new PaymentService(
                paymentRepository, dealServiceClient, stripeService, userClient);

        mockDeal = new DealServiceClient.DealDto();
        mockDeal.setId(1L);
        mockDeal.setDealerId(2L);
        mockDeal.setFarmerId(1L);
        mockDeal.setCropName("Tomato");
        mockDeal.setTotalAmount(new BigDecimal("2800.00"));
        mockDeal.setStatus("ACCEPTED");

        mockRequest = new PaymentRequest();
        mockRequest.setDealId(1L);

        mockPayment = Payment.builder()
                .id(1L)
                .dealId(1L)
                .dealerId(2L)
                .farmerId(1L)
                .amount(new BigDecimal("2800.00"))
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.SUCCESS)
                .transactionReference("cs_test_ABC123")
                .createdAt(LocalDateTime.now())
                .build();

        // stripe result now returns checkout session
        mockStripeResult = new StripePaymentResult() {
            @Override public String getStatus() { return "open"; }
            @Override public String getId() { return "cs_test_ABC123"; }
            @Override public String getPaymentUrl() { return "https://checkout.stripe.com/pay/cs_test_ABC123"; }
        };

        // mock user DTOs for invoice/receipt tests
        mockDealer = new UserServiceClient.UserDto();
        mockDealer.setId(2L);
        mockDealer.setName("Suresh Traders");
        mockDealer.setEmail("suresh@dealer.com");
        mockDealer.setPhone("9876500000");
        mockDealer.setAddress("Mumbai");

        mockFarmer = new UserServiceClient.UserDto();
        mockFarmer.setId(1L);
        mockFarmer.setName("Ramesh Kumar");
        mockFarmer.setEmail("ramesh@farmer.com");
        mockFarmer.setPhone("9876543210");
        mockFarmer.setAddress("Nashik");
    }

    // ─── initiatePayment ────────────────────────────────────────────────────

    @Test
    void initiatePayment_ShouldReturnPendingWithPaymentUrl() throws Exception {
        Payment pendingPayment = Payment.builder()
                .id(1L)
                .dealId(1L)
                .dealerId(2L)
                .farmerId(1L)
                .amount(new BigDecimal("2800.00"))
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.PENDING)
                .transactionReference("cs_test_ABC123")
                .build();

        when(dealServiceClient.getDealById(1L)).thenReturn(mockDeal);
        when(paymentRepository.findByDealId(1L)).thenReturn(Optional.empty());
        when(stripeService.createCheckoutSession(any(), any(), any())).thenReturn(mockStripeResult);
        when(paymentRepository.save(any())).thenReturn(pendingPayment);

        PaymentResponse response = paymentService.initiatePayment(2L, mockRequest);

        assertNotNull(response);
        // status is PENDING until user pays on stripe page
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("cs_test_ABC123", response.getTransactionReference());
        // payment url should be returned so frontend can redirect
        assertNotNull(response.getPaymentUrl());
        assertEquals("https://checkout.stripe.com/pay/cs_test_ABC123", response.getPaymentUrl());
        // deal should NOT be completed yet - only after verify
        verify(dealServiceClient, never()).completeDeal(any());
    }

    @Test
    void initiatePayment_NonAcceptedDeal_ShouldThrow() {
        mockDeal.setStatus("PENDING");
        when(dealServiceClient.getDealById(1L)).thenReturn(mockDeal);

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(2L, mockRequest));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePayment_DuplicatePayment_ShouldThrow() {
        when(dealServiceClient.getDealById(1L)).thenReturn(mockDeal);
        when(paymentRepository.findByDealId(1L)).thenReturn(Optional.of(mockPayment));

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(2L, mockRequest));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiatePayment_StripeFails_ShouldSaveFailedPayment() throws Exception {
        when(dealServiceClient.getDealById(1L)).thenReturn(mockDeal);
        when(paymentRepository.findByDealId(1L)).thenReturn(Optional.empty());
        when(stripeService.createCheckoutSession(any(), any(), any()))
                .thenThrow(new RuntimeException("Stripe error"));

        Payment failedPayment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.FAILED)
                .failureReason("Stripe error")
                .build();
        when(paymentRepository.save(any())).thenReturn(failedPayment);

        PaymentResponse response = paymentService.initiatePayment(2L, mockRequest);

        assertEquals(PaymentStatus.FAILED, response.getStatus());
        verify(dealServiceClient, never()).completeDeal(any());
    }

    // ─── getPaymentByDeal ───────────────────────────────────────────────────

    @Test
    void getPaymentByDeal_ShouldReturn() {
        when(paymentRepository.findByDealId(1L)).thenReturn(Optional.of(mockPayment));

        PaymentResponse response = paymentService.getPaymentByDeal(1L);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(1L, response.getDealId());
    }

    @Test
    void getPaymentByDeal_NotFound_ShouldThrow() {
        when(paymentRepository.findByDealId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentByDeal(99L));
    }

    // ─── getPaymentsByDealer / Farmer ───────────────────────────────────────

    @Test
    void getPaymentsByDealer_ShouldReturnList() {
        when(paymentRepository.findAllByDealerId(2L)).thenReturn(List.of(mockPayment));

        List<PaymentResponse> result = paymentService.getPaymentsByDealer(2L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getDealerId());
    }

    @Test
    void getPaymentsByFarmer_ShouldReturnList() {
        when(paymentRepository.findAllByFarmerId(1L)).thenReturn(List.of(mockPayment));

        List<PaymentResponse> result = paymentService.getPaymentsByFarmer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getFarmerId());
    }

    // ─── generateInvoice ────────────────────────────────────────────────────

    @Test
    void generateInvoice_SuccessPayment_ShouldReturnInvoice() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));
        when(userClient.getUserById(2L)).thenReturn(mockDealer);
        when(userClient.getUserById(1L)).thenReturn(mockFarmer);

        BillingDTO result = paymentService.generateInvoice(1L);

        assertNotNull(result);
        assertEquals("INVOICE", result.getTitle());
        assertEquals("Suresh Traders", result.getDealer().getName());
        assertEquals("Ramesh Kumar", result.getFarmer().getName());
        assertEquals(new BigDecimal("2800.00"), result.getAmount());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(1L, result.getPaymentId());
        assertEquals(1L, result.getDealId());
    }

    @Test
    void generateInvoice_PendingPayment_ShouldReturnProformaInvoice() {
        Payment pendingPayment = Payment.builder()
                .id(1L)
                .dealId(1L)
                .dealerId(2L)
                .farmerId(1L)
                .amount(new BigDecimal("2800.00"))
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(userClient.getUserById(2L)).thenReturn(mockDealer);
        when(userClient.getUserById(1L)).thenReturn(mockFarmer);

        BillingDTO result = paymentService.generateInvoice(1L);

        assertNotNull(result);
        assertEquals("PROFORMA INVOICE", result.getTitle());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    void generateInvoice_NotFound_ShouldThrow() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.generateInvoice(99L));
    }

    // ─── generateReceipt ────────────────────────────────────────────────────

    @Test
    void generateReceipt_SuccessPayment_ShouldReturnReceipt() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));
        when(userClient.getUserById(2L)).thenReturn(mockDealer);
        when(userClient.getUserById(1L)).thenReturn(mockFarmer);

        BillingDTO result = paymentService.generateReceipt(1L);

        assertNotNull(result);
        assertEquals("PAYMENT RECEIPT", result.getTitle());
        assertEquals("Suresh Traders", result.getDealer().getName());
        assertEquals("Ramesh Kumar", result.getFarmer().getName());
        assertEquals(new BigDecimal("2800.00"), result.getAmount());
        assertEquals("cs_test_ABC123", result.getTransactionReference());
    }

    @Test
    void generateReceipt_PendingPayment_ShouldThrow() {
        Payment pendingPayment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        assertThrows(RuntimeException.class, () -> paymentService.generateReceipt(1L));
        // should not call userClient if payment not successful
        verify(userClient, never()).getUserById(any());
    }

    @Test
    void generateReceipt_FailedPayment_ShouldThrow() {
        Payment failedPayment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.FAILED)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(failedPayment));

        assertThrows(RuntimeException.class, () -> paymentService.generateReceipt(1L));
        verify(userClient, never()).getUserById(any());
    }

    @Test
    void generateReceipt_NotFound_ShouldThrow() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.generateReceipt(99L));
    }
}