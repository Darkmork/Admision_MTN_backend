package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.Interview.InterviewMode;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateInterviewRequest {

    private Long interviewerId;

    private InterviewType type;

    private InterviewMode mode;

    @Future(message = "La fecha debe ser futura")
    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

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