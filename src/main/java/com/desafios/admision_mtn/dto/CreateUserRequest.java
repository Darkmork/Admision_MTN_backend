package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import lombok.Data;

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
    
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;
    
    private String phone;
    
    @NotNull(message = "El rol es obligatorio")
    private User.UserRole role;
    
    private User.EducationalLevel educationalLevel;
    
    private User.Subject subject;
    
    // Contraseña opcional - si no se proporciona, se genera automáticamente
    private String password;
    
    private Boolean sendWelcomeEmail = true;
}