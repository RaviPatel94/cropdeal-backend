package com.cropdeal.notificationservice.repository;

import com.cropdeal.notificationservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByDealerId(Long dealerId);
    List<Subscription> findAllByCropNameIgnoreCase(String cropName);
    List<Subscription> findAllByCropTypeIgnoreCase(String cropType);
    boolean existsByDealerIdAndCropNameIgnoreCase(Long dealerId, String cropName);
}