package com.fitset.service;

import com.fitset.model.User;
import com.fitset.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private BMIService bmiService;
    
    public User registerUser(User user) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Calculate BMI if weight and height are provided
        if (user.getWeight() != null && user.getHeight() != null) {
            Map<String, Object> bmiData = bmiService.calculateBMI(user.getWeight(), user.getHeight());
            user.setBmi((Double) bmiData.get("bmi"));
            user.setBmiCategory((String) bmiData.get("category"));
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public User updateUserProfile(String userId, User updatedUser) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update fields
        if (updatedUser.getUsername() != null) {
            user.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getWeight() != null) {
            user.setWeight(updatedUser.getWeight());
        }
        if (updatedUser.getHeight() != null) {
            user.setHeight(updatedUser.getHeight());
        }
        if (updatedUser.getAge() != null) {
            user.setAge(updatedUser.getAge());
        }
        if (updatedUser.getGender() != null) {
            user.setGender(updatedUser.getGender());
        }
        if (updatedUser.getFitnessGoal() != null) {
            user.setFitnessGoal(updatedUser.getFitnessGoal());
        }
        if (updatedUser.getActivityLevel() != null) {
            user.setActivityLevel(updatedUser.getActivityLevel());
        }
        
        // Recalculate BMI if weight or height changed
        if (user.getWeight() != null && user.getHeight() != null) {
            Map<String, Object> bmiData = bmiService.calculateBMI(user.getWeight(), user.getHeight());
            user.setBmi((Double) bmiData.get("bmi"));
            user.setBmiCategory((String) bmiData.get("category"));
        }
        
        return userRepository.save(user);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User getUserById(String id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
