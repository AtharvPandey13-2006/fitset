package com.fitset.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BMIService {
    
    public Map<String, Object> calculateBMI(Double weight, Double height) {
        // height in cm, convert to meters
        double heightInMeters = height / 100.0;
        
        // BMI = weight(kg) / (height(m))^2
        double bmi = weight / (heightInMeters * heightInMeters);
        
        // Round to 2 decimal places
        bmi = Math.round(bmi * 100.0) / 100.0;
        
        String category = getBMICategory(bmi);
        String recommendation = getBMIRecommendation(category);
        
        Map<String, Object> result = new HashMap<>();
        result.put("bmi", bmi);
        result.put("category", category);
        result.put("recommendation", recommendation);
        
        return result;
    }
    
    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 25) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    private String getBMIRecommendation(String category) {
        return switch (category) {
            case "Underweight" -> 
                "Focus on gaining weight through a nutrient-rich diet and strength training.";
            case "Normal weight" -> 
                "Maintain your current weight with balanced diet and regular exercise.";
            case "Overweight" -> 
                "Focus on weight loss through calorie deficit and increased physical activity.";
            case "Obese" -> 
                "Significant weight loss recommended. Consult healthcare provider for personalized plan.";
            default -> "Maintain a healthy lifestyle.";
        };
    }
}
