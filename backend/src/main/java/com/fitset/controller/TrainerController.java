package com.fitset.controller;

import com.fitset.model.Trainer;
import com.fitset.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@CrossOrigin(origins = "*")
public class TrainerController {
    
    @Autowired
    private TrainerRepository trainerRepository;
    
    @GetMapping("/all")
    public ResponseEntity<List<Trainer>> getAllTrainers() {
        List<Trainer> trainers = trainerRepository.findByAvailableTrue();
        return ResponseEntity.ok(trainers);
    }
    
    @GetMapping("/certified")
    public ResponseEntity<List<Trainer>> getCertifiedTrainers() {
        List<Trainer> trainers = trainerRepository.findByCertifiedTrueAndAvailableTrue();
        return ResponseEntity.ok(trainers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Trainer> getTrainerById(@PathVariable String id) {
        return trainerRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
