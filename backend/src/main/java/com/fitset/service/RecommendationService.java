package com.fitset.service;

import com.fitset.model.Recommendation;
import com.fitset.model.User;
import com.fitset.repository.RecommendationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public Recommendation generateRecommendations(User user) {
        Recommendation recommendation = new Recommendation();
        recommendation.setUserId(user.getId());
        recommendation.setGeneratedAt(LocalDateTime.now());
        
        // Generate user profile summary for AI
        String userProfile = buildUserProfile(user);
        
        // Try AI-first structured generation; fallback to rule-based if parsing fails
        boolean aiSucceeded = tryFillPlansWithAI(recommendation, userProfile);
        if (!aiSucceeded) {
            // Fallback: rule-based
            recommendation.setDietPlan(generateDietPlan(user, userProfile));
            recommendation.setWorkoutPlan(generateWorkoutPlan(user, userProfile));
            recommendation.setTips(geminiService.generateMotivationalTips(userProfile));
        }
        
        return recommendationRepository.save(recommendation);
    }

    private boolean tryFillPlansWithAI(Recommendation rec, String userProfile) {
        try {
            String aiText = geminiService.generateStructuredPlan(userProfile);
            if (aiText == null) return false;
            // If wrapped in fences, strip
            String trimmed = aiText.trim();
            int first = trimmed.indexOf('{');
            int last = trimmed.lastIndexOf('}');
            if (first >= 0 && last > first) trimmed = trimmed.substring(first, last + 1);
            JsonNode root = objectMapper.readTree(trimmed);
            // Map dietPlan
            JsonNode diet = root.path("dietPlan");
            if (diet.isMissingNode()) return false;
            Recommendation.DietPlan dp = new Recommendation.DietPlan();
            dp.setDailyCalories(diet.path("dailyCalories").asInt());
            dp.setProtein(diet.path("protein").asInt());
            dp.setCarbs(diet.path("carbs").asInt());
            dp.setFats(diet.path("fats").asInt());
            java.util.List<Recommendation.MealSuggestion> meals = new java.util.ArrayList<>();
            diet.path("meals").forEach(m -> meals.add(new Recommendation.MealSuggestion(
                    m.path("mealType").asText("OTHER"),
                    m.path("name").asText("Meal"),
                    m.path("calories").isNumber() ? m.path("calories").asInt() : null,
                    m.path("description").asText(""),
                    jsonArrayToList(m.path("ingredients"))
            )));
            dp.setMeals(meals);
            dp.setGuidelines(jsonArrayToList(diet.path("guidelines")));
            rec.setDietPlan(dp);
            
            // Map workoutPlan
            JsonNode w = root.path("workoutPlan");
            Recommendation.WorkoutPlan wp = new Recommendation.WorkoutPlan();
            wp.setWeeklyWorkouts(w.path("weeklyWorkouts").asInt(5));
            wp.setSessionDuration(w.path("sessionDuration").asInt(45));
            java.util.List<Recommendation.WorkoutSession> sessions = new java.util.ArrayList<>();
            w.path("sessions").forEach(s -> {
                java.util.List<Recommendation.WorkoutExercise> exs = new java.util.ArrayList<>();
                s.path("exercises").forEach(ex -> exs.add(new Recommendation.WorkoutExercise(
                        ex.path("name").asText("Exercise"),
                        ex.path("type").asText("STRENGTH"),
                        ex.path("sets").asText(null),
                        ex.path("reps").asText(null),
                        ex.path("duration").isNumber() ? ex.path("duration").asInt() : null,
                        ex.path("description").asText("")
                )));
                sessions.add(new Recommendation.WorkoutSession(
                        s.path("day").asText("Day"),
                        s.path("focus").asText("STRENGTH"),
                        exs
                ));
            });
            wp.setSessions(sessions);
            wp.setGuidelines(jsonArrayToList(w.path("guidelines")));
            rec.setWorkoutPlan(wp);
            
            // Tips
            java.util.List<String> tips = jsonArrayToList(root.path("tips"));
            if (tips == null || tips.isEmpty())
                tips = geminiService.generateMotivationalTips(userProfile);
            rec.setTips(tips);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private java.util.List<String> jsonArrayToList(JsonNode node) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }
    
    private String buildUserProfile(User user) {
        return String.format(
            "User Profile:\n" +
            "- Gender: %s\n" +
            "- Age: %d years\n" +
            "- Weight: %.1f kg\n" +
            "- Height: %.1f cm\n" +
            "- BMI: %.1f (%s)\n" +
            "- Fitness Goal: %s\n" +
            "- Activity Level: %s",
            user.getGender(),
            user.getAge(),
            user.getWeight(),
            user.getHeight(),
            user.getBmi(),
            user.getBmiCategory(),
            user.getFitnessGoal(),
            user.getActivityLevel()
        );
    }
    
    private Recommendation.DietPlan generateDietPlan(User user, String userProfile) {
        Recommendation.DietPlan dietPlan = new Recommendation.DietPlan();
        
        // Calculate daily calorie needs based on BMI and goals
        int baseCalories = calculateBaseCalories(user);
        int dailyCalories = adjustCaloriesForGoal(baseCalories, user.getFitnessGoal());
        
        dietPlan.setDailyCalories(dailyCalories);
        dietPlan.setProtein((int) (dailyCalories * 0.30 / 4)); // 30% of calories from protein
        dietPlan.setCarbs((int) (dailyCalories * 0.40 / 4)); // 40% from carbs
        dietPlan.setFats((int) (dailyCalories * 0.30 / 9)); // 30% from fats
        
        dietPlan.setMeals(generateMealSuggestions(user.getBmiCategory(), dailyCalories));
        dietPlan.setGuidelines(generateDietGuidelines(user));
        
        return dietPlan;
    }
    
    private int calculateBaseCalories(User user) {
        // Basic Mifflin-St Jeor Equation
        double bmr;
        if ("MALE".equalsIgnoreCase(user.getGender())) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        
        // Adjust for activity level
        double activityMultiplier = switch (user.getActivityLevel() != null ? user.getActivityLevel() : "MODERATE") {
            case "SEDENTARY" -> 1.2;
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE" -> 1.9;
            default -> 1.55;
        };
        
        return (int) (bmr * activityMultiplier);
    }
    
    private int adjustCaloriesForGoal(int baseCalories, String goal) {
        return switch (goal != null ? goal : "MAINTENANCE") {
            case "WEIGHT_LOSS" -> baseCalories - 500;
            case "MUSCLE_GAIN" -> baseCalories + 300;
            default -> baseCalories;
        };
    }
    
    private List<Recommendation.MealSuggestion> generateMealSuggestions(String bmiCategory, int dailyCalories) {
        List<Recommendation.MealSuggestion> meals = new ArrayList<>();
        
        // Breakfast
        meals.add(new Recommendation.MealSuggestion(
            "BREAKFAST",
            "Protein-Rich Breakfast",
            dailyCalories / 4,
            "Start your day with energy",
            List.of("Oatmeal with berries", "Greek yogurt", "Scrambled eggs", "Whole grain toast", "Green tea")
        ));
        
        // Lunch
        meals.add(new Recommendation.MealSuggestion(
            "LUNCH",
            "Balanced Lunch",
            dailyCalories / 3,
            "Fuel your afternoon",
            List.of("Grilled chicken breast", "Brown rice", "Mixed vegetables", "Side salad", "Olive oil dressing")
        ));
        
        // Dinner
        meals.add(new Recommendation.MealSuggestion(
            "DINNER",
            "Light Dinner",
            dailyCalories / 3,
            "End your day right",
            List.of("Baked fish or tofu", "Quinoa", "Steamed broccoli", "Sweet potato", "Herbal tea")
        ));
        
        // Snacks
        meals.add(new Recommendation.MealSuggestion(
            "SNACK",
            "Healthy Snacks",
            dailyCalories / 10,
            "Between meals",
            List.of("Almonds", "Apple", "Protein bar", "Carrot sticks with hummus")
        ));
        
        return meals;
    }
    
    private List<String> generateDietGuidelines(User user) {
        List<String> guidelines = new ArrayList<>();
        guidelines.add("Drink at least 8 glasses of water daily");
        guidelines.add("Eat 5-6 small meals throughout the day");
        guidelines.add("Include protein in every meal");
        guidelines.add("Avoid processed foods and sugary drinks");
        guidelines.add("Eat plenty of fruits and vegetables");
        guidelines.add("Control portion sizes");
        
        if ("Underweight".equals(user.getBmiCategory())) {
            guidelines.add("Focus on calorie-dense, nutritious foods");
            guidelines.add("Add healthy fats like nuts and avocados");
        } else if ("Overweight".equals(user.getBmiCategory()) || "Obese".equals(user.getBmiCategory())) {
            guidelines.add("Reduce refined carbohydrates");
            guidelines.add("Increase fiber intake");
            guidelines.add("Practice mindful eating");
        }
        
        return guidelines;
    }
    
    private Recommendation.WorkoutPlan generateWorkoutPlan(User user, String userProfile) {
        Recommendation.WorkoutPlan workoutPlan = new Recommendation.WorkoutPlan();
        
        workoutPlan.setWeeklyWorkouts(5);
        workoutPlan.setSessionDuration(45);
        workoutPlan.setSessions(generateWorkoutSessions(user));
        workoutPlan.setGuidelines(generateWorkoutGuidelines(user));
        
        return workoutPlan;
    }
    
    private List<Recommendation.WorkoutSession> generateWorkoutSessions(User user) {
        List<Recommendation.WorkoutSession> sessions = new ArrayList<>();
        
        // Monday - Upper Body Strength
        sessions.add(createWorkoutSession("Monday", "STRENGTH", List.of(
            new Recommendation.WorkoutExercise("Push-ups", "STRENGTH", "3", "12-15", null, "Build upper body strength"),
            new Recommendation.WorkoutExercise("Dumbbell Rows", "STRENGTH", "3", "10-12", null, "Strengthen back muscles"),
            new Recommendation.WorkoutExercise("Shoulder Press", "STRENGTH", "3", "10-12", null, "Build shoulder strength"),
            new Recommendation.WorkoutExercise("Bicep Curls", "STRENGTH", "3", "12-15", null, "Tone arms")
        )));
        
        // Tuesday - Cardio
        sessions.add(createWorkoutSession("Tuesday", "CARDIO", List.of(
            new Recommendation.WorkoutExercise("Running/Jogging", "CARDIO", null, null, 20, "Burn calories and improve endurance"),
            new Recommendation.WorkoutExercise("Jump Rope", "CARDIO", "3", null, 5, "High-intensity cardio"),
            new Recommendation.WorkoutExercise("Burpees", "CARDIO", "3", "10", null, "Full body cardio")
        )));
        
        // Wednesday - Lower Body Strength
        sessions.add(createWorkoutSession("Wednesday", "STRENGTH", List.of(
            new Recommendation.WorkoutExercise("Squats", "STRENGTH", "4", "15-20", null, "Build leg strength"),
            new Recommendation.WorkoutExercise("Lunges", "STRENGTH", "3", "12 each leg", null, "Tone legs and glutes"),
            new Recommendation.WorkoutExercise("Leg Raises", "STRENGTH", "3", "15", null, "Core and lower abs"),
            new Recommendation.WorkoutExercise("Calf Raises", "STRENGTH", "3", "20", null, "Strengthen calves")
        )));
        
        // Thursday - Active Recovery
        sessions.add(createWorkoutSession("Thursday", "FLEXIBILITY", List.of(
            new Recommendation.WorkoutExercise("Yoga", "FLEXIBILITY", null, null, 30, "Improve flexibility and reduce stress"),
            new Recommendation.WorkoutExercise("Stretching", "FLEXIBILITY", null, null, 15, "Prevent injuries")
        )));
        
        // Friday - Full Body
        sessions.add(createWorkoutSession("Friday", "STRENGTH", List.of(
            new Recommendation.WorkoutExercise("Planks", "STRENGTH", "3", "30-60 sec", null, "Core strength"),
            new Recommendation.WorkoutExercise("Mountain Climbers", "CARDIO", "3", "20", null, "Full body workout"),
            new Recommendation.WorkoutExercise("Deadlifts", "STRENGTH", "3", "10-12", null, "Posterior chain"),
            new Recommendation.WorkoutExercise("Russian Twists", "STRENGTH", "3", "20", null, "Obliques")
        )));
        
        // Weekend - Rest or Light Activity
        sessions.add(createWorkoutSession("Saturday", "REST", List.of(
            new Recommendation.WorkoutExercise("Light Walking", "CARDIO", null, null, 30, "Stay active, recover"),
            new Recommendation.WorkoutExercise("Stretching", "FLEXIBILITY", null, null, 15, "Maintain flexibility")
        )));
        
        sessions.add(createWorkoutSession("Sunday", "REST", List.of(
            new Recommendation.WorkoutExercise("Rest Day", "REST", null, null, null, "Complete rest and recovery")
        )));
        
        return sessions;
    }
    
    private Recommendation.WorkoutSession createWorkoutSession(String day, String focus, 
                                                               List<Recommendation.WorkoutExercise> exercises) {
        Recommendation.WorkoutSession session = new Recommendation.WorkoutSession();
        session.setDay(day);
        session.setFocus(focus);
        session.setExercises(exercises);
        return session;
    }
    
    private List<String> generateWorkoutGuidelines(User user) {
        List<String> guidelines = new ArrayList<>();
        guidelines.add("Always warm up for 5-10 minutes before exercising");
        guidelines.add("Cool down and stretch after each workout");
        guidelines.add("Stay hydrated throughout your workout");
        guidelines.add("Listen to your body and rest when needed");
        guidelines.add("Gradually increase intensity over time");
        guidelines.add("Maintain proper form to prevent injuries");
        
        if ("Underweight".equals(user.getBmiCategory())) {
            guidelines.add("Focus on strength training over cardio");
            guidelines.add("Allow adequate rest between workouts");
        } else if ("Overweight".equals(user.getBmiCategory()) || "Obese".equals(user.getBmiCategory())) {
            guidelines.add("Start with low-impact exercises");
            guidelines.add("Increase cardio duration gradually");
        }
        
        return guidelines;
    }
}
