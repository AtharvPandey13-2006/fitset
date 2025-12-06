package com.fitset.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String username;
    private String message;
}
