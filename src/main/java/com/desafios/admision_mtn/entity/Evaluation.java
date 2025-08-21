package com.desafios.admision_mtn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator; // Usuario que realiza la evaluación

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    // Campos para evaluaciones académicas
    @Column(name = "score")
    private Integer score; // Puntaje numérico (0-100)

    @Column(name = "grade")
    private String grade; // Calificación (A, B, C, D, F)

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations; // Observaciones generales

    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths; // Fortalezas identificadas

    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement; // Áreas de mejora

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations; // Recomendaciones

    // Campos específicos para entrevistas psicológicas
    @Column(name = "social_skills_assessment", columnDefinition = "TEXT")
    private String socialSkillsAssessment; // Evaluación de habilidades sociales

    @Column(name = "emotional_maturity", columnDefinition = "TEXT")
    private String emotionalMaturity; // Madurez emocional

    @Column(name = "motivation_assessment", columnDefinition = "TEXT")
    private String motivationAssessment; // Evaluación de motivación

    @Column(name = "family_support_assessment", columnDefinition = "TEXT")
    private String familySupportAssessment; // Evaluación del apoyo familiar

    // Campos para Director de Ciclo
    @Column(name = "academic_readiness", columnDefinition = "TEXT")
    private String academicReadiness; // Preparación académica

    @Column(name = "behavioral_assessment", columnDefinition = "TEXT")
    private String behavioralAssessment; // Evaluación conductual

    @Column(name = "integration_potential", columnDefinition = "TEXT")
    private String integrationPotential; // Potencial de integración

    @Column(name = "final_recommendation")
    private Boolean finalRecommendation; // Recomendación final (true = recomendar, false = no recomendar)

    // Referencia a la programación de la evaluación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private EvaluationSchedule schedule; // Programación asociada

    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate; // Fecha cuando se realizó la evaluación

    @Column(name = "completion_date")
    private LocalDateTime completionDate; // Fecha cuando se completó el reporte

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EvaluationType {
        LANGUAGE_EXAM("Examen de Lenguaje"),
        MATHEMATICS_EXAM("Examen de Matemáticas"), 
        ENGLISH_EXAM("Examen de Inglés"),
        CYCLE_DIRECTOR_REPORT("Informe Director de Ciclo"),
        CYCLE_DIRECTOR_INTERVIEW("Entrevista Director/a de Ciclo"),
        PSYCHOLOGICAL_INTERVIEW("Entrevista Psicológica");

        private final String displayName;

        EvaluationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum EvaluationStatus {
        PENDING("Pendiente"),
        IN_PROGRESS("En Progreso"),
        COMPLETED("Completada"),
        REVIEWED("Revisada"),
        APPROVED("Aprobada");

        private final String displayName;

        EvaluationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}