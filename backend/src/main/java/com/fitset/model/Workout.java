package com.fitset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "workouts")
public class Workout {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String category; // STRENGTH, CARDIO, YOGA, HIIT
    private String difficulty; // Beginner, Intermediate, Advanced, All Levels
    private Integer duration; // in minutes
    private Integer calories;
    private String iconClass; // FontAwesome class
    private Boolean active;
    
    private List<String> targetMuscles;
    private List<String> equipment;
}
