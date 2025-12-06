package com.fitset.config;

import com.fitset.model.Workout;
import com.fitset.model.Trainer;
import com.fitset.model.PricingPlan;
import com.fitset.repository.WorkoutRepository;
import com.fitset.repository.TrainerRepository;
import com.fitset.repository.PricingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private WorkoutRepository workoutRepository;
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    @Autowired
    private PricingPlanRepository pricingPlanRepository;
    
    @Override
    public void run(String... args) {
        // Only seed if collections are empty
        if (workoutRepository.count() == 0) {
            seedWorkouts();
        }
        
        if (trainerRepository.count() == 0) {
            seedTrainers();
        }
        
        if (pricingPlanRepository.count() == 0) {
            seedPricingPlans();
        }
    }
    
    private void seedWorkouts() {
        List<Workout> workouts = Arrays.asList(
            new Workout(null, "Full Body Strength", 
                "Build muscle and strength with this comprehensive full-body workout routine.",
                "STRENGTH", "Beginner", 45, 300, "fa-dumbbell", true,
                Arrays.asList("Chest", "Back", "Legs", "Arms"),
                Arrays.asList("Dumbbells", "Barbell", "Bench")),
                
            new Workout(null, "Cardio Blast", 
                "High-energy cardio workout to burn calories and improve cardiovascular health.",
                "CARDIO", "Intermediate", 30, 400, "fa-running", true,
                Arrays.asList("Full Body"),
                Arrays.asList("Treadmill", "Jump Rope")),
                
            new Workout(null, "Morning Yoga Flow", 
                "Start your day with this energizing yoga sequence for flexibility and mindfulness.",
                "YOGA", "All Levels", 25, 150, "fa-leaf", true,
                Arrays.asList("Full Body", "Core"),
                Arrays.asList("Yoga Mat")),
                
            new Workout(null, "HIIT Power", 
                "High-intensity interval training for maximum fat burning and fitness gains.",
                "HIIT", "Advanced", 20, 500, "fa-bolt", true,
                Arrays.asList("Full Body"),
                Arrays.asList("None")),
                
            new Workout(null, "Upper Body Focus", 
                "Target your chest, shoulders, and arms with this focused upper body routine.",
                "STRENGTH", "Intermediate", 40, 280, "fa-weight-hanging", true,
                Arrays.asList("Chest", "Shoulders", "Arms"),
                Arrays.asList("Dumbbells", "Resistance Bands")),
                
            new Workout(null, "Cycling Cardio", 
                "Low-impact cardio workout perfect for beginners and recovery days.",
                "CARDIO", "Beginner", 35, 250, "fa-bicycle", true,
                Arrays.asList("Legs"),
                Arrays.asList("Stationary Bike"))
        );
        
        workoutRepository.saveAll(workouts);
        System.out.println("✅ Workouts seeded successfully!");
    }
    
    private void seedTrainers() {
        List<Trainer> trainers = Arrays.asList(
            new Trainer(null, "Shivam Gautam", 
                "Strength & Conditioning Coach",
                "Experienced in powerlifting and bodybuilding",
                499, 5.0, true, true,
                Arrays.asList("Strength Training", "Bodybuilding"),
                "Certified personal trainer with 8+ years of experience in strength training and bodybuilding. Specialized in helping clients build muscle and increase strength.",
                null),
                
            new Trainer(null, "Bhoomika Chaudhary", 
                "HIIT & Cardio Specialist",
                "Specializing in high-intensity training",
                249, 5.0, true, true,
                Arrays.asList("HIIT", "Cardio"),
                "High-energy trainer passionate about helping clients achieve their cardio fitness goals through innovative HIIT workouts.",
                null),
                
            new Trainer(null, "Shilpa Shetty", 
                "Yoga & Flexibility Coach",
                "Experienced in yoga instruction and mobility training",
                199, 4.0, true, true,
                Arrays.asList("Yoga", "Flexibility"),
                "Certified yoga instructor with expertise in various yoga styles including Vinyasa, Hatha, and Restorative yoga.",
                null),
                
            new Trainer(null, "Akriti Yadav", 
                "Nutrition & Wellness Coach",
                "Experienced in sports nutrition and weight management",
                299, 5.0, true, true,
                Arrays.asList("Nutrition", "Weight Loss"),
                "Registered dietitian and nutrition coach specializing in sports nutrition and sustainable weight management strategies.",
                null)
        );
        
        trainerRepository.saveAll(trainers);
        System.out.println("✅ Trainers seeded successfully!");
    }
    
    private void seedPricingPlans() {
        List<PricingPlan> plans = Arrays.asList(
            new PricingPlan(null, "Basic", 1199, "/month", false, true,
                Arrays.asList(
                    "Access to all workouts",
                    "Basic nutrition plans",
                    "Progress tracking",
                    "Mobile app access"
                ),
                "Perfect for getting started with your fitness journey",
                1),
                
            new PricingPlan(null, "Pro", 1299, "/month", true, true,
                Arrays.asList(
                    "Everything in Basic",
                    "Personalized meal plans",
                    "1-on-1 trainer sessions",
                    "Advanced analytics",
                    "Priority support"
                ),
                "Most popular plan for serious fitness enthusiasts",
                2),
                
            new PricingPlan(null, "Elite", 1499, "/month", false, true,
                Arrays.asList(
                    "Everything in Pro",
                    "Unlimited trainer sessions",
                    "Custom workout plans",
                    "Nutrition coaching",
                    "24/7 support"
                ),
                "Ultimate plan for maximum results",
                3)
        );
        
        pricingPlanRepository.saveAll(plans);
        System.out.println("✅ Pricing plans seeded successfully!");
    }
}
