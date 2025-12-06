package com.fitset.controller;

import com.fitset.dto.ErrorResponse;
import com.fitset.dto.LoginRequest;
import com.fitset.dto.RegisterRequest;
import com.fitset.dto.AuthResponse;
import com.fitset.model.User;
import com.fitset.security.JwtUtil;
import com.fitset.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email already registered"));
            }
            
            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setWeight(request.getWeight());
            user.setHeight(request.getHeight());
            user.setAge(request.getAge());
            user.setGender(request.getGender());
            user.setFitnessGoal(request.getFitnessGoal());
            user.setActivityLevel(request.getActivityLevel());
            
            User savedUser = userService.registerUser(user);
            
            // Generate token
            String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail());
            
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUserId(savedUser.getId());
            response.setEmail(savedUser.getEmail());
            response.setUsername(savedUser.getUsername());
            response.setMessage("Registration successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userService.findByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
            }
            
            User user = userOpt.get();
            
            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid email or password"));
            }
            
            // Generate token
            String token = jwtUtil.generateToken(user.getId(), user.getEmail());
            
            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setUsername(user.getUsername());
            response.setMessage("Login successful");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }
}
