package com.desafios.robotcode.dto;

public class AiChatResponseDto {
    
    private boolean success;
    private String response;
    private String error;
    private int tokensUsed;
    
    // Constructors
    public AiChatResponseDto() {}
    
    public AiChatResponseDto(boolean success, String response) {
        this.success = success;
        this.response = response;
    }
    
    public AiChatResponseDto(boolean success, String response, String error) {
        this.success = success;
        this.response = response;
        this.error = error;
    }
    
    // Static factory methods
    public static AiChatResponseDto success(String response, int tokensUsed) {
        AiChatResponseDto dto = new AiChatResponseDto();
        dto.success = true;
        dto.response = response;
        dto.tokensUsed = tokensUsed;
        return dto;
    }
    
    public static AiChatResponseDto error(String error) {
        AiChatResponseDto dto = new AiChatResponseDto();
        dto.success = false;
        dto.error = error;
        return dto;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public int getTokensUsed() {
        return tokensUsed;
    }
    
    public void setTokensUsed(int tokensUsed) {
        this.tokensUsed = tokensUsed;
    }
}