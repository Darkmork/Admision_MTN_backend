package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.model.RolUsuario;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDto {
    private Long id;
    private String username;
    private String email;
    private RolUsuario rol;
    private LocalDateTime fechaRegistro;
    private int puntaje;

    // Getters y setters
}
