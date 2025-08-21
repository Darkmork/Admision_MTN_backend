package com.desafios.admision_mtn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean success;
    private String message;
    
    public static AuthResponse success(String token, String email, String firstName, String lastName, String role) {
        return new AuthResponse(token, email, firstName, lastName, role, true, "Autenticación exitosa");
    }
    
    public static AuthResponse error(String message) {
        return new AuthResponse(null, null, null, null, null, false, message);
    }
}