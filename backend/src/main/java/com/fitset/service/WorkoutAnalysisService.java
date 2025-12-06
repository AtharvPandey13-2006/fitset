package com.fitset.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitset.model.AnalysisResult;
import com.fitset.model.DailyTracking;
import com.fitset.model.Recommendation;
import com.fitset.model.User;
import com.fitset.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkoutAnalysisService {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public AnalysisResult analyzeToday(User user, DailyTracking tracking, Recommendation latestRec) {
        try {
            String userProfile = buildUserProfile(user);
            String trackingJson = objectMapper.writeValueAsString(tracking);
            String planJson = latestRec != null ? objectMapper.writeValueAsString(latestRec) : "{}";

            String prompt = buildAnalysisPrompt(userProfile, trackingJson, planJson);
            String aiText = geminiService.generateRecommendation(prompt);

            // Try to parse JSON if present, else fallback to heuristic extraction
            AnalysisResult result = tryParseAnalysis(aiText);
            if (result == null) {
                result = new AnalysisResult();
                result.setDate(tracking.getDate().toString());
                result.setSummary(aiText != null ? aiText.substring(0, Math.min(aiText.length(), 600)) : "No analysis generated.");
                result.setUsefulnessScore(60);
                result.setUsefulnessReason("Default score when AI returned unstructured text.");
            }
            if (result.getDate() == null) {
                result.setDate(tracking.getDate().format(DateTimeFormatter.ISO_DATE));
            }
            return result;
        } catch (Exception e) {
            AnalysisResult fallback = new AnalysisResult();
            fallback.setDate(tracking.getDate().format(DateTimeFormatter.ISO_DATE));
            fallback.setSummary("Could not generate AI analysis at this time.");
            fallback.setUsefulnessScore(0);
            fallback.setUsefulnessReason(e.getMessage());
            return fallback;
        }
    }

    private String buildUserProfile(User user) {
        return String.format("Gender: %s, Age: %d, Weight: %.1f kg, Height: %.1f cm, BMI: %.1f (%s), Goal: %s, Activity: %s",
                user.getGender(), user.getAge(), user.getWeight(), user.getHeight(), user.getBmi(), user.getBmiCategory(),
                user.getFitnessGoal(), user.getActivityLevel());
    }

    private String buildAnalysisPrompt(String userProfile, String trackingJson, String planJson) {
        return "You are a senior fitness coach and sports scientist. Analyze the user's TODAY workout and day tracking.\n" +
                "User Profile: " + userProfile + "\n\n" +
                "Latest AI Plan (JSON):\n" + planJson + "\n\n" +
                "Today's Tracking (JSON):\n" + trackingJson + "\n\n" +
                "Return STRICT JSON ONLY with this schema (no commentary):\n" +
                "{\n" +
                "  \"date\": \"YYYY-MM-DD\",\n" +
                "  \"summary\": \"1-2 sentences summarizing what the user did and training effect\",\n" +
                "  \"usefulnessScore\": 0-100,\n" +
                "  \"usefulnessReason\": \"why this score\",\n" +
                "  \"expectedResults\": [\"short bullet on expected adaptation or soreness\"],\n" +
                "  \"positives\": [\"what went well\"],\n" +
                "  \"improvements\": [\"what to improve next time\"],\n" +
                "  \"nextSteps\": [\"concrete next action for tomorrow\"]\n" +
                "}";
    }

    private AnalysisResult tryParseAnalysis(String aiText) {
        if (aiText == null) return null;
        try {
            // In case AI wrapped JSON in code fences
            String trimmed = aiText.trim();
            if (trimmed.startsWith("```)")) {
                int first = trimmed.indexOf('{');
                int last = trimmed.lastIndexOf('}');
                if (first >= 0 && last > first) {
                    trimmed = trimmed.substring(first, last + 1);
                }
            }
            JsonNode root = objectMapper.readTree(trimmed);
            AnalysisResult ar = new AnalysisResult();
            ar.setDate(root.path("date").asText(null));
            ar.setSummary(root.path("summary").asText(null));
            ar.setUsefulnessScore(root.path("usefulnessScore").asInt(0));
            ar.setUsefulnessReason(root.path("usefulnessReason").asText(null));
            ar.setExpectedResults(jsonArrayToList(root.path("expectedResults")));
            ar.setPositives(jsonArrayToList(root.path("positives")));
            ar.setImprovements(jsonArrayToList(root.path("improvements")));
            ar.setNextSteps(jsonArrayToList(root.path("nextSteps")));
            return ar;
        } catch (Exception ignore) {
            return null;
        }
    }

    private java.util.List<String> jsonArrayToList(JsonNode node) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }
}
