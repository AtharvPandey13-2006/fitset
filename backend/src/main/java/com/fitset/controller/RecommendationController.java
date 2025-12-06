package com.fitset.controller;

import com.fitset.model.Recommendation;
import com.fitset.model.User;
import com.fitset.repository.RecommendationRepository;
import com.fitset.security.JwtUtil;
import com.fitset.service.RecommendationService;
import com.fitset.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateRecommendations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getWeight() == null || user.getHeight() == null || user.getAge() == null) {
                return ResponseEntity.badRequest()
                    .body("Please complete your profile (weight, height, age) before generating recommendations");
            }
            
            Recommendation recommendation = recommendationService.generateRecommendations(user);
            
            return ResponseEntity.ok(recommendation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestRecommendations(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            Optional<Recommendation> recommendation = 
                recommendationRepository.findTopByUserIdOrderByGeneratedAtDesc(userId);
            
            if (recommendation.isEmpty()) {
                return ResponseEntity.ok().body("No recommendations found. Generate new recommendations.");
            }
            
            return ResponseEntity.ok(recommendation.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
