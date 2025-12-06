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
@Document(collection = "trainers")
public class Trainer {
    @Id
    private String id;
    
    private String name;
    private String specialty;
    private String experience;
    private Integer pricePerSession; // in INR
    private Double rating;
    private Boolean certified;
    private Boolean available;
    
    private List<String> specialties;
    private String bio;
    private String imageUrl;
}
