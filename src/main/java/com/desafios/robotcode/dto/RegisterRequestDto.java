package com.desafios.robotcode.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequestDto {
    
    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "El nombre de usuario solo puede contener letras, números, guiones y puntos")
    private String username;
    
    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@(mtn\\.cl|alumnos\\.mtn\\.cl)$", 
             message = "Solo se permiten correos con dominios @mtn.cl o @alumnos.mtn.cl")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;
}