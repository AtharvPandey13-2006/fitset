package com.fitset.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotNull(message = "Weight is required")
    private Double weight;
    
    @NotNull(message = "Height is required")
    private Double height;
    
    @NotNull(message = "Age is required")
    private Integer age;
    
    private String gender;
    private String fitnessGoal;
    private String activityLevel;
}
