package com.fitset.controller;

import com.fitset.model.User;
import com.fitset.model.DailyTracking;
import com.fitset.repository.DailyTrackingRepository;
import com.fitset.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {
    
    @Autowired
    private DailyTrackingRepository trackingRepository;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            // Get authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String userId = authentication.getName();
            Map<String, Object> stats = new HashMap<>();
            
            // Get user info
            User user = userService.getUserById(userId);
            
            // Calculate date ranges
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minus(7, ChronoUnit.DAYS);
            LocalDate monthAgo = today.minus(30, ChronoUnit.DAYS);
            
            // Get tracking data
            List<DailyTracking> weekTracking = trackingRepository.findByUserIdAndDateBetween(
                userId, weekAgo, today);
            List<DailyTracking> monthTracking = trackingRepository.findByUserIdAndDateBetween(
                userId, monthAgo, today);
            
            // Calculate stats for this week
            int weekWorkoutDays = (int) weekTracking.stream()
                .filter(t -> t.getTotalExerciseMinutes() != null && t.getTotalExerciseMinutes() > 0)
                .count();
            
            int weekTotalMinutes = weekTracking.stream()
                .mapToInt(t -> t.getTotalExerciseMinutes() != null ? t.getTotalExerciseMinutes() : 0)
                .sum();
            
            int weekTotalCalories = weekTracking.stream()
                .mapToInt(t -> t.getCaloriesBurned() != null ? t.getCaloriesBurned() : 0)
                .sum();
            
            // Calculate stats for this month
            int monthWorkoutDays = (int) monthTracking.stream()
                .filter(t -> t.getTotalExerciseMinutes() != null && t.getTotalExerciseMinutes() > 0)
                .count();
            
            int monthTotalMinutes = monthTracking.stream()
                .mapToInt(t -> t.getTotalExerciseMinutes() != null ? t.getTotalExerciseMinutes() : 0)
                .sum();
            
            int monthTotalCalories = monthTracking.stream()
                .mapToInt(t -> t.getCaloriesBurned() != null ? t.getCaloriesBurned() : 0)
                .sum();
            
            // Calculate averages
            double avgWorkoutDuration = weekWorkoutDays > 0 ? 
                (double) weekTotalMinutes / weekWorkoutDays : 0;
            
            // Build response
            stats.put("user", Map.of(
                "username", user.getUsername(),
                "bmi", user.getBmi() != null ? user.getBmi() : 0,
                "bmiCategory", user.getBmiCategory() != null ? user.getBmiCategory() : "N/A",
                "weight", user.getWeight() != null ? user.getWeight() : 0,
                "fitnessGoal", user.getFitnessGoal() != null ? user.getFitnessGoal() : "MAINTENANCE"
            ));
            
            stats.put("week", Map.of(
                "workoutDays", weekWorkoutDays,
                "totalMinutes", weekTotalMinutes,
                "totalCalories", weekTotalCalories,
                "avgDuration", Math.round(avgWorkoutDuration)
            ));
            
            stats.put("month", Map.of(
                "workoutDays", monthWorkoutDays,
                "totalMinutes", monthTotalMinutes,
                "totalCalories", monthTotalCalories
            ));
            
            // Recent achievements
            stats.put("achievements", calculateAchievements(weekTracking, monthTracking));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load stats: " + e.getMessage()));
        }
    }
    
    private List<Map<String, String>> calculateAchievements(
            List<DailyTracking> weekTracking, 
            List<DailyTracking> monthTracking) {
        
        List<Map<String, String>> achievements = new java.util.ArrayList<>();
        
        // Check for workout streaks
        long consecutiveDays = getConsecutiveWorkoutDays(weekTracking);
        if (consecutiveDays >= 7) {
            achievements.add(Map.of(
                "icon", "fa-fire",
                "title", "Week Streak!",
                "description", consecutiveDays + " days in a row"
            ));
        } else if (consecutiveDays >= 3) {
            achievements.add(Map.of(
                "icon", "fa-bolt",
                "title", "Getting Consistent!",
                "description", consecutiveDays + " days streak"
            ));
        }
        
        // Check for calories burned
        int totalCalories = weekTracking.stream()
            .mapToInt(t -> t.getCaloriesBurned() != null ? t.getCaloriesBurned() : 0)
            .sum();
        
        if (totalCalories >= 3000) {
            achievements.add(Map.of(
                "icon", "fa-fire-alt",
                "title", "Calorie Crusher",
                "description", totalCalories + " calories this week"
            ));
        }
        
        // Check for workout count
        long workoutDays = weekTracking.stream()
            .filter(t -> t.getTotalExerciseMinutes() != null && t.getTotalExerciseMinutes() > 0)
            .count();
        
        if (workoutDays >= 5) {
            achievements.add(Map.of(
                "icon", "fa-trophy",
                "title", "Workout Warrior",
                "description", workoutDays + " workouts this week"
            ));
        }
        
        // If no achievements, add a motivational one
        if (achievements.isEmpty()) {
            achievements.add(Map.of(
                "icon", "fa-star",
                "title", "Getting Started",
                "description", "Your fitness journey begins now!"
            ));
        }
        
        return achievements;
    }
    
    private long getConsecutiveWorkoutDays(List<DailyTracking> tracking) {
        if (tracking.isEmpty()) return 0;
        
        tracking.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        long streak = 0;
        LocalDate expectedDate = LocalDate.now();
        
        for (DailyTracking t : tracking) {
            if (t.getTotalExerciseMinutes() != null && t.getTotalExerciseMinutes() > 0) {
                if (t.getDate().equals(expectedDate)) {
                    streak++;
                    expectedDate = expectedDate.minusDays(1);
                } else {
                    break;
                }
            }
        }
        
        return streak;
    }
}
