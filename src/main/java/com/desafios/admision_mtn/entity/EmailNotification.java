package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "email_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonBackReference
    private Application application;
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false)
    private EmailType emailType;
    
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;
    
    @Column(name = "student_name", nullable = false)
    private String studentName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "student_gender", nullable = false)
    private Gender studentGender;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_school", nullable = false)
    private TargetSchool targetSchool;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "delivered")
    @Builder.Default
    private Boolean delivered = false;
    
    // Tracking de apertura
    @Column(name = "opened")
    @Builder.Default
    private Boolean opened = false;
    
    @Column(name = "opened_at")
    private LocalDateTime openedAt;
    
    @Column(name = "open_count")
    @Builder.Default
    private Integer openCount = 0;
    
    @Column(name = "tracking_token", unique = true, nullable = false)
    private String trackingToken;
    
    // Respuesta automática
    @Column(name = "response_required")
    @Builder.Default
    private Boolean responseRequired = false;
    
    @Column(name = "responded")
    @Builder.Default
    private Boolean responded = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "response_value")
    private ResponseValue responseValue;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    @Column(name = "response_token", unique = true)
    private String responseToken;
    
    // Relación con entrevista (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id")
    private Interview interview;
    
    // Datos adicionales como JSON
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_data", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> additionalData = Map.of();
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Enums
    public enum EmailType {
        INTERVIEW_SCHEDULED("Entrevista Programada"),
        INTERVIEW_REMINDER("Recordatorio de Entrevista"),
        INTERVIEW_CONFIRMED("Entrevista Confirmada"),
        INTERVIEW_RESCHEDULED("Entrevista Reprogramada"),
        INTERVIEW_CANCELLED("Entrevista Cancelada"),
        INTERVIEW_INVITATION("Invitación a Entrevista"),
        INTERVIEW_ASSIGNMENT("Asignación de Entrevista"),
        INTERVIEW_COMPLETE_SET("Set Completo de Entrevistas"),
        INTERVIEW_CONFIRMATION("Confirmación de Entrevista"),
        INTERVIEW_RESCHEDULE("Reprogramación de Entrevista"),
        APPLICATION_RECEIVED("Postulación Recibida"),
        APPLICATION_STATUS_UPDATE("Actualización de Estado"),
        DOCUMENTS_REQUIRED("Documentos Requeridos"),
        DOCUMENT_REMINDER("Recordatorio de Documentos"),
        EVALUATION_SCHEDULED("Evaluación Programada"),
        ACCEPTANCE_NOTIFICATION("Notificación de Aceptación"),
        REJECTION_NOTIFICATION("Notificación de Rechazo"),
        ADMISSION_RESULT("Resultado de Admisión"),
        STUDENT_SELECTION("Selección de Estudiante"),
        STUDENT_REJECTION("Rechazo de Estudiante"),
        ADMISSION_RESULTS("Resultados de Admisión"),
        WAITLIST_NOTIFICATION("Lista de Espera"),
        GENERAL_COMMUNICATION("Comunicación General");
        
        private final String displayName;
        
        EmailType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Gender {
        MALE("su hijo", "el"),
        FEMALE("su hija", "la");
        
        private final String article;
        private final String prefix;
        
        Gender(String article, String prefix) {
            this.article = article;
            this.prefix = prefix;
        }
        
        public String getArticle() {
            return article;
        }
        
        public String getPrefix() {
            return prefix;
        }
    }
    
    public enum TargetSchool {
        MONTE_TABOR("Monte Tabor"),
        NAZARET("Nazaret");
        
        private final String displayName;
        
        TargetSchool(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ResponseValue {
        ACCEPT("Aceptar"),
        REJECT("Rechazar"),
        RESCHEDULE("Reprogramar"),
        NEED_MORE_INFO("Necesita más información");
        
        private final String displayName;
        
        ResponseValue(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Métodos de conveniencia
    public String getPersonalizedStudentReference() {
        return studentGender.getArticle() + " " + studentName;
    }
    
    public String getSchoolReference() {
        return "Colegio " + targetSchool.getDisplayName();
    }
    
    public boolean hasBeenOpened() {
        return opened != null && opened;
    }
    
    public boolean hasResponded() {
        return responded != null && responded;
    }
    
    public boolean isResponsePending() {
        return responseRequired && !hasResponded();
    }
}