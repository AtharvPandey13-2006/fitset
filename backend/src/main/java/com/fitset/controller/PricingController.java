package com.fitset.controller;

import com.fitset.model.PricingPlan;
import com.fitset.repository.PricingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "*")
public class PricingController {
    
    @Autowired
    private PricingPlanRepository pricingPlanRepository;
    
    @GetMapping("/plans")
    public ResponseEntity<List<PricingPlan>> getAllPlans() {
        List<PricingPlan> plans = pricingPlanRepository.findByActiveTrueOrderBySortOrderAsc();
        return ResponseEntity.ok(plans);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PricingPlan> getPlanById(@PathVariable String id) {
        return pricingPlanRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
