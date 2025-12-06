package com.fitset.repository;

import com.fitset.model.Workout;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends MongoRepository<Workout, String> {
    List<Workout> findByActiveTrue();
    List<Workout> findByCategoryAndActiveTrue(String category);
    List<Workout> findByDifficultyAndActiveTrue(String difficulty);
}
