package com.desafios.admision_mtn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationResponse {
    
    private boolean success;
    private String message;
    private Long expiresInMinutes;
    
    public static EmailVerificationResponse success(String message, Long expiresInMinutes) {
        return new EmailVerificationResponse(true, message, expiresInMinutes);
    }
    
    public static EmailVerificationResponse error(String message) {
        return new EmailVerificationResponse(false, message, null);
    }
}