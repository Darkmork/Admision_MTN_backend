package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Interview.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class InterviewResponse {

    private Long id;
    
    // Información de la aplicación
    private Long applicationId;
    private String studentName;
    private String parentNames;
    private String gradeApplied;
    
    // Información del entrevistador
    private Long interviewerId;
    private String interviewerName;
    
    // Detalles de la entrevista
    private InterviewStatus status;
    private InterviewType type;
    private InterviewMode mode;
    
    // Programación
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private Integer duration;
    
    // Ubicación y acceso
    private String location;
    private String virtualMeetingLink;
    
    // Notas y preparación
    private String notes;
    private String preparation;
    
    // Resultados
    private InterviewResult result;
    private Double score;
    private String recommendations;
    private Boolean followUpRequired;
    private String followUpNotes;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    
    // Estados computados
    private Boolean isUpcoming;
    private Boolean isOverdue;
    private Boolean canBeCompleted;
    private Boolean canBeEdited;
    private Boolean canBeCancelled;

    public static InterviewResponse from(Interview interview) {
        InterviewResponse response = new InterviewResponse();
        
        response.setId(interview.getId());
        
        // Información de la aplicación
        response.setApplicationId(interview.getApplication().getId());
        response.setStudentName(interview.getStudentName());
        response.setParentNames(interview.getParentNames());
        response.setGradeApplied(interview.getGradeApplied());
        
        // Información del entrevistador
        response.setInterviewerId(interview.getInterviewer().getId());
        response.setInterviewerName(interview.getInterviewerName());
        
        // Detalles de la entrevista
        response.setStatus(interview.getStatus());
        response.setType(interview.getType());
        response.setMode(interview.getMode());
        
        // Programación
        response.setScheduledDate(interview.getScheduledDate());
        response.setScheduledTime(interview.getScheduledTime());
        response.setDuration(interview.getDuration());
        
        // Ubicación y acceso
        response.setLocation(interview.getLocation());
        response.setVirtualMeetingLink(interview.getVirtualMeetingLink());
        
        // Notas y preparación
        response.setNotes(interview.getNotes());
        response.setPreparation(interview.getPreparation());
        
        // Resultados
        response.setResult(interview.getResult());
        response.setScore(interview.getScore());
        response.setRecommendations(interview.getRecommendations());
        response.setFollowUpRequired(interview.getFollowUpRequired());
        response.setFollowUpNotes(interview.getFollowUpNotes());
        
        // Timestamps
        response.setCreatedAt(interview.getCreatedAt());
        response.setUpdatedAt(interview.getUpdatedAt());
        response.setCompletedAt(interview.getCompletedAt());
        
        // Estados computados
        response.setIsUpcoming(interview.isUpcoming());
        response.setIsOverdue(interview.isOverdue());
        response.setCanBeCompleted(interview.canBeCompleted());
        response.setCanBeEdited(interview.canBeEdited());
        response.setCanBeCancelled(interview.canBeCancelled());
        
        return response;
    }
}