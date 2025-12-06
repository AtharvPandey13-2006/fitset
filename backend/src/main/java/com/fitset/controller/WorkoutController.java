package com.fitset.controller;

import com.fitset.model.Workout;
import com.fitset.repository.WorkoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
public class WorkoutController {
    
    @Autowired
    private WorkoutRepository workoutRepository;
    
    @GetMapping("/all")
    public ResponseEntity<List<Workout>> getAllWorkouts() {
        List<Workout> workouts = workoutRepository.findByActiveTrue();
        return ResponseEntity.ok(workouts);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Workout>> getWorkoutsByCategory(@PathVariable String category) {
        List<Workout> workouts = workoutRepository.findByCategoryAndActiveTrue(category.toUpperCase());
        return ResponseEntity.ok(workouts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable String id) {
        return workoutRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
