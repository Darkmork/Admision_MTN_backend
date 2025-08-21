package com.desafios.admision_mtn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPuntosDto {
    private Long usuarioId;
    private int puntos;
    
    public TestPuntosDto() {}
    
    public TestPuntosDto(Long usuarioId, int puntos) {
        this.usuarioId = usuarioId;
        this.puntos = puntos;
    }
}