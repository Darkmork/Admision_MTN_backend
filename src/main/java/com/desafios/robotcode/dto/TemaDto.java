package com.desafios.robotcode.dto;

import com.desafios.robotcode.model.Dificultad;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemaDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Dificultad dificultad;

    // Getters y setters
}
