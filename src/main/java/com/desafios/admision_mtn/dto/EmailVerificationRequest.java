package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.EmailVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailVerificationRequest {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotNull(message = "El tipo de verificación es obligatorio")
    private EmailVerification.VerificationType type;
}