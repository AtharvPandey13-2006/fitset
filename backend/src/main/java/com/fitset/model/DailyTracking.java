package com.fitset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "daily_tracking")
public class DailyTracking {
    @Id
    private String id;
    
    private String userId;
    private LocalDate date;
    
    // Exercise Tracking
    private List<Exercise> exercises;
    private Integer totalExerciseMinutes;
    private Integer caloriesBurned;
    
    // Meal Tracking
    private List<Meal> meals;
    private Integer totalCaloriesConsumed;
    
    // Hydration Tracking
    private Double waterIntake; // in liters
    private Integer waterGoal; // in liters
    
    // Progress Notes
    private String notes;
    private Double weight; // daily weight
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercise {
        private String name;
        private String type; // CARDIO, STRENGTH, FLEXIBILITY, SPORTS
        private Integer duration; // in minutes
        private Integer calories;
        private Boolean completed;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meal {
        private String name;
        private String type; // BREAKFAST, LUNCH, DINNER, SNACK
        private Integer calories;
        private Double protein; // in grams
        private Double carbs; // in grams
        private Double fats; // in grams
        private String description;
    }
}
