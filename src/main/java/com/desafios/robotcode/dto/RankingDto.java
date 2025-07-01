package com.desafios.robotcode.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankingDto {
    private Long id;
    private Long usuarioId;
    private int puntaje;
    private LocalDateTime fechaActualizacion;

    // Getters y setters
}
