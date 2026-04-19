package com.cropdeal.paymentservice.repository;

import com.cropdeal.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByDealId(Long dealId);
    Optional<Payment> findByTransactionReference(String transactionReference);
    List<Payment> findAllByDealerId(Long dealerId);
    List<Payment> findAllByFarmerId(Long farmerId);
}
