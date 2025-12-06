package com.fitset.controller;

import com.fitset.model.DailyTracking;
import com.fitset.security.JwtUtil;
import com.fitset.service.DailyTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@CrossOrigin
public class TrackingController {
    
    @Autowired
    private DailyTrackingService trackingService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/today")
    public ResponseEntity<?> getTodayTracking(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            DailyTracking tracking = trackingService.getOrCreateTodayTracking(userId);
            
            return ResponseEntity.ok(tracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/update")
    public ResponseEntity<?> updateTracking(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody DailyTracking tracking) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            DailyTracking updated = trackingService.updateTracking(userId, tracking);
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getTrackingByDate(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            return trackingService.getTrackingByDate(userId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getTrackingHistory(@RequestHeader("Authorization") String authHeader,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            List<DailyTracking> history = trackingService.getTrackingHistory(userId, startDate, endDate);
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<?> getAllTracking(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserId(token);
            
            List<DailyTracking> allTracking = trackingService.getAllUserTracking(userId);
            
            return ResponseEntity.ok(allTracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
