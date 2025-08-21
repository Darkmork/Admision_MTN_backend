package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluation_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Programación genérica (por tipo de evaluación y nivel)
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type")
    private Evaluation.EvaluationType evaluationType;

    @Column(name = "grade_level") // Nivel académico (ej: "Kinder", "1° Básico", etc.)
    private String gradeLevel;

    @Column(name = "subject") // Para exámenes académicos específicos
    private String subject;

    // Programación específica (para un estudiante individual)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application; // null = programación genérica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id")
    private User evaluator; // Quien realiza la evaluación/entrevista

    // Información de la cita
    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes; // Duración en minutos

    @Column(name = "location")
    private String location; // Sala, aula, etc.

    @Column(name = "meeting_link") // Para reuniones virtuales
    private String meetingLink;

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions; // Instrucciones especiales

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    // Información de contacto/confirmación
    @Column(name = "requires_confirmation")
    private Boolean requiresConfirmation = false;

    @Column(name = "confirmation_deadline")
    private LocalDateTime confirmationDeadline;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by_user_id")
    private User confirmedBy;

    // Información adicional para entrevistas familiares
    @Column(name = "attendees_required", columnDefinition = "TEXT")
    private String attendeesRequired; // "Estudiante, Padres", "Solo Padres", etc.

    @Column(name = "preparation_materials", columnDefinition = "TEXT")
    private String preparationMaterials; // Documentos que deben traer

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enum para tipo de programación
    public enum ScheduleType {
        GENERIC("Programación Genérica"), // Para todos los estudiantes del nivel
        INDIVIDUAL("Programación Individual"), // Para un estudiante específico
        GROUP("Programación Grupal"), // Para un grupo de estudiantes
        MAKEUP("Evaluación de Reposición"); // Para reprogramaciones

        private final String displayName;

        ScheduleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Enum para estado de la programación
    public enum ScheduleStatus {
        SCHEDULED("Programada"),
        CONFIRMED("Confirmada"),
        COMPLETED("Completada"),
        CANCELLED("Cancelada"),
        RESCHEDULED("Reprogramada"),
        NO_SHOW("No asistió");

        private final String displayName;

        ScheduleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}