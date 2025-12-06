package com.fitset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommendations")
public class Recommendation {
    @Id
    private String id;
    
    private String userId;
    private LocalDateTime generatedAt;
    
    // Diet Plan
    private DietPlan dietPlan;
    
    // Workout Plan
    private WorkoutPlan workoutPlan;
    
    // Motivational Tips
    private List<String> tips;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DietPlan {
        private Integer dailyCalories;
        private Integer protein; // in grams
        private Integer carbs; // in grams
        private Integer fats; // in grams
        private List<MealSuggestion> meals;
        private List<String> guidelines;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealSuggestion {
        private String mealType;
        private String name;
        private Integer calories;
        private String description;
        private List<String> ingredients;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutPlan {
        private Integer weeklyWorkouts;
        private Integer sessionDuration; // in minutes
        private List<WorkoutSession> sessions;
        private List<String> guidelines;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutSession {
        private String day;
        private String focus; // CARDIO, STRENGTH, FLEXIBILITY, REST
        private List<WorkoutExercise> exercises;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutExercise {
        private String name;
        private String type;
        private String sets;
        private String reps;
        private Integer duration; // in minutes
        private String description;
    }
}
