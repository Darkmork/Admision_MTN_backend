package com.desafios.robotcode.dto;
import com.desafios.robotcode.model.RolUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioAuthRequestDto {
    private String username;
    private String email;    // opcional en login
    private String password;
    private RolUsuario rol;

}
