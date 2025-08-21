package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.Interview.InterviewResult;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CompleteInterviewRequest {

    @NotNull(message = "El resultado de la entrevista es obligatorio")
    private InterviewResult result;

    @DecimalMin(value = "1.0", message = "La puntuación mínima es 1.0")
    @DecimalMax(value = "10.0", message = "La puntuación máxima es 10.0")
    private Double score;

    @Size(max = 2000, message = "Las recomendaciones no pueden exceder 2000 caracteres")
    private String recommendations;

    private Boolean followUpRequired = false;

    @Size(max = 2000, message = "Las notas de seguimiento no pueden exceder 2000 caracteres")
    private String followUpNotes;
}