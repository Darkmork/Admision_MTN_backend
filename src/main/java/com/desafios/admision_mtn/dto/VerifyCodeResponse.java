package com.desafios.admision_mtn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeResponse {
    
    private boolean success;
    private String message;
    
    @JsonProperty("isValid")
    private boolean isValid;
    
    public static VerifyCodeResponse success(String message) {
        return new VerifyCodeResponse(true, message, true);
    }
    
    public static VerifyCodeResponse error(String message) {
        return new VerifyCodeResponse(false, message, false);
    }
}