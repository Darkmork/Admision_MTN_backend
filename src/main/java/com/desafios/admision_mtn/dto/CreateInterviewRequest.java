package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.Interview.InterviewMode;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateInterviewRequest {

    @NotNull(message = "El ID de la aplicación es obligatorio")
    private Long applicationId;

    @NotNull(message = "El ID del entrevistador es obligatorio")
    private Long interviewerId;

    @NotNull(message = "El tipo de entrevista es obligatorio")
    private InterviewType type;

    @NotNull(message = "La modalidad de entrevista es obligatoria")
    private InterviewMode mode;

    @NotNull(message = "La fecha programada es obligatoria")
    @Future(message = "La fecha debe ser futura")
    private LocalDate scheduledDate;

    @NotNull(message = "La hora programada es obligatoria")
    private LocalTime scheduledTime;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Max(value = 480, message = "La duración máxima es 8 horas (480 minutos)")
    private Integer duration;

    @Size(max = 500, message = "La ubicación no puede exceder 500 caracteres")
    private String location;

    @Size(max = 1000, message = "El enlace de reunión virtual no puede exceder 1000 caracteres")
    private String virtualMeetingLink;

    @Size(max = 2000, message = "Las notas no pueden exceder 2000 caracteres")
    private String notes;

    @Size(max = 2000, message = "La preparación no puede exceder 2000 caracteres")
    private String preparation;
}