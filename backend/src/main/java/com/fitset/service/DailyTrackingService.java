package com.fitset.service;

import com.fitset.model.DailyTracking;
import com.fitset.repository.DailyTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DailyTrackingService {
    
    @Autowired
    private DailyTrackingRepository dailyTrackingRepository;
    
    public DailyTracking getOrCreateTodayTracking(String userId) {
        LocalDate today = LocalDate.now();
        Optional<DailyTracking> existing = dailyTrackingRepository.findByUserIdAndDate(userId, today);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        DailyTracking tracking = new DailyTracking();
        tracking.setUserId(userId);
        tracking.setDate(today);
        tracking.setWaterGoal(8); // 8 glasses default
        tracking.setWaterIntake(0.0);
        
        return dailyTrackingRepository.save(tracking);
    }
    
    public DailyTracking updateTracking(String userId, DailyTracking tracking) {
        LocalDate date = tracking.getDate() != null ? tracking.getDate() : LocalDate.now();
        Optional<DailyTracking> existing = dailyTrackingRepository.findByUserIdAndDate(userId, date);
        
        DailyTracking toSave;
        if (existing.isPresent()) {
            toSave = existing.get();
            // Update fields
            if (tracking.getExercises() != null) {
                toSave.setExercises(tracking.getExercises());
                toSave.setTotalExerciseMinutes(calculateTotalMinutes(tracking.getExercises()));
                toSave.setCaloriesBurned(calculateCaloriesBurned(tracking.getExercises()));
            }
            if (tracking.getMeals() != null) {
                toSave.setMeals(tracking.getMeals());
                toSave.setTotalCaloriesConsumed(calculateTotalCalories(tracking.getMeals()));
            }
            if (tracking.getWaterIntake() != null) {
                toSave.setWaterIntake(tracking.getWaterIntake());
            }
            if (tracking.getNotes() != null) {
                toSave.setNotes(tracking.getNotes());
            }
            if (tracking.getWeight() != null) {
                toSave.setWeight(tracking.getWeight());
            }
        } else {
            toSave = tracking;
            toSave.setUserId(userId);
            toSave.setDate(date);
            if (toSave.getExercises() != null) {
                toSave.setTotalExerciseMinutes(calculateTotalMinutes(tracking.getExercises()));
                toSave.setCaloriesBurned(calculateCaloriesBurned(tracking.getExercises()));
            }
            if (toSave.getMeals() != null) {
                toSave.setTotalCaloriesConsumed(calculateTotalCalories(tracking.getMeals()));
            }
        }
        
        return dailyTrackingRepository.save(toSave);
    }
    
    public Optional<DailyTracking> getTrackingByDate(String userId, LocalDate date) {
        return dailyTrackingRepository.findByUserIdAndDate(userId, date);
    }
    
    public List<DailyTracking> getTrackingHistory(String userId, LocalDate startDate, LocalDate endDate) {
        return dailyTrackingRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
    
    public List<DailyTracking> getAllUserTracking(String userId) {
        return dailyTrackingRepository.findByUserIdOrderByDateDesc(userId);
    }
    
    private Integer calculateTotalMinutes(List<DailyTracking.Exercise> exercises) {
        return exercises.stream()
            .mapToInt(e -> e.getDuration() != null ? e.getDuration() : 0)
            .sum();
    }
    
    private Integer calculateCaloriesBurned(List<DailyTracking.Exercise> exercises) {
        return exercises.stream()
            .mapToInt(e -> e.getCalories() != null ? e.getCalories() : 0)
            .sum();
    }
    
    private Integer calculateTotalCalories(List<DailyTracking.Meal> meals) {
        return meals.stream()
            .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
            .sum();
    }
}
