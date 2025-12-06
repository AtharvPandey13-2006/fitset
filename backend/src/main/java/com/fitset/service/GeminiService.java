package com.fitset.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String modelName;
    
    public GeminiService(ObjectMapper objectMapper) {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
        this.objectMapper = objectMapper;
    }
    
    public String generateRecommendation(String prompt) {
        try {
            // Build request body matching Google's example format
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));
            
            // Create request
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(apiUrl + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
            
            // Execute request
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Gemini API Error: " + response.code() + " - " + response.message());
                    return null;
                }
                
                String responseBody = response.body().string();
                
                // Parse response
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode candidates = jsonNode.get("candidates");
                
                if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                    JsonNode content0 = candidates.get(0).get("content");
                    if (content0 != null) {
                        JsonNode parts = content0.get("parts");
                        if (parts != null && parts.isArray() && parts.size() > 0) {
                            JsonNode textNode = parts.get(0).get("text");
                            if (textNode != null) {
                                return textNode.asText();
                            }
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public String generateDietPlan(String userProfile) {
        String prompt = String.format(
            "As a professional nutritionist, create a personalized daily diet plan based on the following profile:\n\n%s\n\n" +
            "Provide:\n" +
            "1. Specific meal suggestions with calories, protein, carbs, and fats\n" +
            "2. Practical dietary guidelines\n" +
            "3. Keep it concise and actionable\n\n" +
            "Format the response as a structured plan.",
            userProfile
        );
        
        return generateRecommendation(prompt);
    }
    
    public String generateWorkoutPlan(String userProfile) {
        String prompt = String.format(
            "As a certified fitness trainer, create a personalized weekly workout plan based on the following profile:\n\n%s\n\n" +
            "Provide:\n" +
            "1. A 7-day workout schedule with specific exercises\n" +
            "2. Sets, reps, or duration for each exercise\n" +
            "3. Focus areas for each day (e.g., Upper Body, Lower Body, Cardio)\n" +
            "4. Practical workout guidelines\n" +
            "5. Keep it concise and actionable\n\n" +
            "Format the response as a structured plan.",
            userProfile
        );
        
        return generateRecommendation(prompt);
    }

    /**
     * Ask Gemini for a STRICT JSON structured plan combining diet, workout, and tips.
     * Returns raw JSON text (no code fences). Caller is responsible for parsing.
     */
    public String generateStructuredPlan(String userProfile) {
        String prompt = "You are a fitness and nutrition expert. Create a personalized plan for the user.\n" +
                "User Profile:\n" + userProfile + "\n\n" +
                "Return STRICT JSON ONLY (no code fences, no commentary) matching this schema:\n" +
                "{\n" +
                "  \"dietPlan\": {\n" +
                "    \"dailyCalories\": 2000,\n" +
                "    \"protein\": 150,\n" +
                "    \"carbs\": 200,\n" +
                "    \"fats\": 70,\n" +
                "    \"meals\": [{\n" +
                "      \"mealType\": \"BREAKFAST\",\n" +
                "      \"name\": \"Greek Yogurt Bowl\",\n" +
                "      \"calories\": 400,\n" +
                "      \"description\": \"Short description\",\n" +
                "      \"ingredients\": [\"ingredient\"]\n" +
                "    }],\n" +
                "    \"guidelines\": [\"guideline\"]\n" +
                "  },\n" +
                "  \"workoutPlan\": {\n" +
                "    \"weeklyWorkouts\": 5,\n" +
                "    \"sessionDuration\": 45,\n" +
                "    \"sessions\": [{\n" +
                "      \"day\": \"Monday\",\n" +
                "      \"focus\": \"STRENGTH\",\n" +
                "      \"exercises\": [{\n" +
                "        \"name\": \"Push-ups\",\n" +
                "        \"type\": \"STRENGTH\",\n" +
                "        \"sets\": \"3\",\n" +
                "        \"reps\": \"12\",\n" +
                "        \"duration\": null,\n" +
                "        \"description\": \"Short description\"\n" +
                "      }]\n" +
                "    }],\n" +
                "    \"guidelines\": [\"guideline\"]\n" +
                "  },\n" +
                "  \"tips\": [\"short motivational tip\"]\n" +
                "}";
        return generateRecommendation(prompt);
    }
    
    public List<String> generateMotivationalTips(String userProfile) {
        String prompt = String.format(
            "As a fitness coach, provide 5 short motivational tips (one sentence each) for someone with this profile:\n\n%s\n\n" +
            "Make them encouraging and actionable.",
            userProfile
        );
        
        String response = generateRecommendation(prompt);
        
        if (response != null) {
            // Parse tips from response (split by newlines or numbered list)
            String[] lines = response.split("\n");
            return List.of(lines).stream()
                .filter(line -> !line.trim().isEmpty())
                .map(line -> line.replaceAll("^[0-9]+\\.\\s*", "").trim())
                .filter(line -> !line.isEmpty())
                .limit(5)
                .toList();
        }
        
        return List.of(
            "Stay consistent with your workouts",
            "Progress takes time, be patient",
            "Celebrate small victories",
            "Listen to your body",
            "You're stronger than you think"
        );
    }
    
    /**
     * Generate a general chat response using Gemini AI
     */
    public String generateResponse(String userMessage) {
        return generateRecommendation(userMessage);
    }
    
    /**
     * Generate a fitness-focused chat response with conversation history
     */
    public String generateFitnessResponse(String userMessage, String conversationHistory) {
        String systemPrompt = "You are FitSet AI, a friendly and knowledgeable fitness assistant. " +
            "You help users with workout advice, nutrition guidance, motivation, and fitness tips. " +
            "Keep responses conversational, supportive, and concise. " +
            "Use emojis occasionally to keep things friendly.";
        
        String fullPrompt = systemPrompt + "\n\n";
        
        if (conversationHistory != null && !conversationHistory.trim().isEmpty()) {
            fullPrompt += "Recent conversation:\n" + conversationHistory + "\n\n";
        }
        
        fullPrompt += "User: " + userMessage + "\n\nAssistant:";
        
        return generateRecommendation(fullPrompt);
    }
}
