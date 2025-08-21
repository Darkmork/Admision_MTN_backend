package com.desafios.admision_mtn.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatsDto {
    private Long usuarioId;
    private long totalProblemas;
    private long problemasSolved;
    private long problemasInProgress;
    private int puntaje;
    
    // Constructores
    public UserStatsDto() {}
    
    public UserStatsDto(Long usuarioId, long totalProblemas, long problemasSolved, 
                       long problemasInProgress, int puntaje) {
        this.usuarioId = usuarioId;
        this.totalProblemas = totalProblemas;
        this.problemasSolved = problemasSolved;
        this.problemasInProgress = problemasInProgress;
        this.puntaje = puntaje;
    }
}