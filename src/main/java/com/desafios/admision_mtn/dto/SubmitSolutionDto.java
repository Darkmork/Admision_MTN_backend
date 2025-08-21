package com.desafios.admision_mtn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmitSolutionDto {
    private Long usuarioId;
    private Long problemaId;
    private String codigo;
    private boolean correct;
    private String dificultad; // Nueva: para calcular puntos desde frontend
    private String temaId; // Nueva: para tracking por tema
    
    // Constructores
    public SubmitSolutionDto() {}
    
    public SubmitSolutionDto(Long usuarioId, Long problemaId, String codigo, boolean correct) {
        this.usuarioId = usuarioId;
        this.problemaId = problemaId;
        this.codigo = codigo;
        this.correct = correct;
    }
    
    public SubmitSolutionDto(Long usuarioId, Long problemaId, String codigo, boolean correct, String dificultad, String temaId) {
        this.usuarioId = usuarioId;
        this.problemaId = problemaId;
        this.codigo = codigo;
        this.correct = correct;
        this.dificultad = dificultad;
        this.temaId = temaId;
    }
}