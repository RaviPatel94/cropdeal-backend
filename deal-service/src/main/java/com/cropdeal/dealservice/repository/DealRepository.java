package com.cropdeal.dealservice.repository;

import com.cropdeal.dealservice.model.Deal;
import com.cropdeal.dealservice.model.DealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {
    List<Deal> findAllByDealerId(Long dealerId);
    List<Deal> findAllByFarmerId(Long farmerId);
    List<Deal> findAllByStatus(DealStatus status);
    List<Deal> findAllByListingId(Long listingId);
}