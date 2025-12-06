package com.fitset.repository;

import com.fitset.model.PricingPlan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingPlanRepository extends MongoRepository<PricingPlan, String> {
    List<PricingPlan> findByActiveTrueOrderBySortOrderAsc();
}
