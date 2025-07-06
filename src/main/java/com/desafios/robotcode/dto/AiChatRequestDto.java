package com.desafios.robotcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiChatRequestDto {
    
    @NotBlank(message = "El mensaje es requerido")
    @Size(min = 1, max = 1000, message = "El mensaje debe tener entre 1 y 1000 caracteres")
    private String message;
    
    private String context; // Contexto del problema actual (opcional)
    
    // Constructors
    public AiChatRequestDto() {}
    
    public AiChatRequestDto(String message, String context) {
        this.message = message;
        this.context = context;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getContext() {
        return context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    @Override
    public String toString() {
        return "AiChatRequestDto{" +
                "message='" + message + '\'' +
                ", context='" + context + '\'' +
                '}';
    }
}