package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interviewer_user_id", nullable = false)
    private User interviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewMode mode;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(nullable = false)
    private Integer duration; // en minutos

    @Column(length = 500)
    private String location;

    @Column(name = "virtual_meeting_link", length = 1000)
    private String virtualMeetingLink;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String preparation;

    @Enumerated(EnumType.STRING)
    private InterviewResult result;

    @Column(precision = 3)
    private Double score; // 1.0 - 10.0

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    private String followUpNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Enums
    public enum InterviewStatus {
        SCHEDULED,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }

    public enum InterviewType {
        INDIVIDUAL,
        FAMILY,
        PSYCHOLOGICAL,
        ACADEMIC,
        BEHAVIORAL
    }

    public enum InterviewMode {
        IN_PERSON,
        VIRTUAL,
        HYBRID
    }

    public enum InterviewResult {
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        PENDING_REVIEW,
        REQUIRES_FOLLOW_UP
    }

    // Métodos de conveniencia
    public String getStudentName() {
        return application != null && application.getStudent() != null ?
            application.getStudent().getFirstName() + " " + 
            application.getStudent().getLastName() + " " + 
            application.getStudent().getMaternalLastName() : "";
    }
    
    public LocalDate getInterviewDate() {
        return scheduledDate;
    }
    
    public LocalTime getInterviewTime() {
        return scheduledTime;
    }

    public String getParentNames() {
        if (application == null) return "";
        
        StringBuilder parentNames = new StringBuilder();
        if (application.getFather() != null) {
            parentNames.append(application.getFather().getFullName());
        }
        if (application.getMother() != null) {
            if (parentNames.length() > 0) {
                parentNames.append(", ");
            }
            parentNames.append(application.getMother().getFullName());
        }
        return parentNames.toString();
    }

    public String getGradeApplied() {
        return application != null && application.getStudent() != null ?
            application.getStudent().getGradeApplied() : "";
    }

    public String getInterviewerName() {
        return interviewer != null ?
            interviewer.getFirstName() + " " + interviewer.getLastName() : "";
    }

    // Validaciones de negocio
    public boolean canBeCompleted() {
        return status == InterviewStatus.IN_PROGRESS || status == InterviewStatus.CONFIRMED;
    }

    public boolean canBeEdited() {
        return status != InterviewStatus.COMPLETED && status != InterviewStatus.CANCELLED;
    }

    public boolean canBeCancelled() {
        return status == InterviewStatus.SCHEDULED || status == InterviewStatus.CONFIRMED;
    }

    public boolean isUpcoming() {
        LocalDateTime interviewDateTime = LocalDateTime.of(scheduledDate, scheduledTime);
        LocalDateTime now = LocalDateTime.now();
        long hoursUntil = java.time.Duration.between(now, interviewDateTime).toHours();
        return hoursUntil >= 0 && hoursUntil <= 24;
    }

    public boolean isOverdue() {
        if (status == InterviewStatus.COMPLETED || status == InterviewStatus.CANCELLED) {
            return false;
        }
        LocalDateTime interviewDateTime = LocalDateTime.of(scheduledDate, scheduledTime);
        return interviewDateTime.isBefore(LocalDateTime.now());
    }

    // Métodos de transición de estado
    public void confirm() {
        if (status == InterviewStatus.SCHEDULED) {
            this.status = InterviewStatus.CONFIRMED;
        } else {
            throw new IllegalStateException("La entrevista solo puede ser confirmada si está programada");
        }
    }

    public void start() {
        if (status == InterviewStatus.CONFIRMED || status == InterviewStatus.SCHEDULED) {
            this.status = InterviewStatus.IN_PROGRESS;
        } else {
            throw new IllegalStateException("La entrevista solo puede iniciarse si está confirmada o programada");
        }
    }

    public void complete(InterviewResult result, Double score, String recommendations, Boolean followUpRequired, String followUpNotes) {
        if (!canBeCompleted()) {
            throw new IllegalStateException("La entrevista no puede ser completada en su estado actual");
        }
        
        this.status = InterviewStatus.COMPLETED;
        this.result = result;
        this.score = score;
        this.recommendations = recommendations;
        this.followUpRequired = followUpRequired != null ? followUpRequired : false;
        this.followUpNotes = followUpNotes;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("La entrevista no puede ser cancelada en su estado actual");
        }
        this.status = InterviewStatus.CANCELLED;
    }

    public void reschedule(LocalDate newDate, LocalTime newTime) {
        if (!canBeEdited()) {
            throw new IllegalStateException("La entrevista no puede ser reprogramada en su estado actual");
        }
        this.scheduledDate = newDate;
        this.scheduledTime = newTime;
        this.status = InterviewStatus.RESCHEDULED;
    }

    public void markAsNoShow() {
        if (status == InterviewStatus.CONFIRMED || status == InterviewStatus.SCHEDULED) {
            this.status = InterviewStatus.NO_SHOW;
        } else {
            throw new IllegalStateException("Solo se puede marcar como 'no asistió' entrevistas confirmadas o programadas");
        }
    }
}