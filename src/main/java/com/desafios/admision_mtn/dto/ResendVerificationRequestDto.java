package com.desafios.admision_mtn.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class ResendVerificationRequestDto {
    
    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@(mtn\\.cl|alumnos\\.mtn\\.cl)$", 
             message = "Solo se permiten correos con dominios @mtn.cl o @alumnos.mtn.cl")
    private String email;
}