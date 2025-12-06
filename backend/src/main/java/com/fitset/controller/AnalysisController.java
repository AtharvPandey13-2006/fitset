package com.fitset.controller;

import com.fitset.model.AnalysisResult;
import com.fitset.model.DailyTracking;
import com.fitset.model.Recommendation;
import com.fitset.model.User;
import com.fitset.repository.RecommendationRepository;
import com.fitset.security.JwtUtil;
import com.fitset.service.DailyTrackingService;
import com.fitset.service.UserService;
import com.fitset.service.WorkoutAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin
public class AnalysisController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private DailyTrackingService trackingService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private WorkoutAnalysisService analysisService;

    @PostMapping("/today")
    public ResponseEntity<?> analyzeToday(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            DailyTracking tracking = trackingService.getOrCreateTodayTracking(userId);
            Recommendation latest = recommendationRepository.findTopByUserIdOrderByGeneratedAtDesc(userId).orElse(null);

            if ((tracking.getExercises() == null || tracking.getExercises().isEmpty()) &&
                (tracking.getMeals() == null || tracking.getMeals().isEmpty())) {
                return ResponseEntity.badRequest().body("No workout or meal data logged today to analyze.");
            }

            AnalysisResult result = analysisService.analyzeToday(user, tracking, latest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> analyzeByDate(@RequestHeader("Authorization") String authHeader,
                                           @PathVariable String date) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);

            User user = userService.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            DailyTracking tracking = trackingService.getTrackingByDate(userId, LocalDate.parse(date))
                    .orElseThrow(() -> new RuntimeException("No tracking found for date"));
            Recommendation latest = recommendationRepository.findTopByUserIdOrderByGeneratedAtDesc(userId).orElse(null);

            AnalysisResult result = analysisService.analyzeToday(user, tracking, latest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
