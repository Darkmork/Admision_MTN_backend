package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.model.Dificultad;

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
