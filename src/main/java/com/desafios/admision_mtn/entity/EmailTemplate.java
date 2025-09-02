package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_key", unique = true, nullable = false, length = 100)
    private String templateKey; // Clave única para identificar el template

    @Column(nullable = false, length = 200)
    private String name; // Nombre descriptivo del template

    @Column(columnDefinition = "TEXT")
    private String description; // Descripción del propósito del template

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type; // Tipo de template

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateCategory category; // Categoría del template

    @Column(nullable = false, length = 300)
    private String subject; // Asunto del correo (puede incluir variables)

    @Column(name = "html_content", columnDefinition = "TEXT", nullable = false)
    private String htmlContent; // Contenido HTML del template

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent; // Contenido en texto plano (fallback)

    @Column(columnDefinition = "TEXT")
    private String variables; // JSON con las variables disponibles

    @Column(nullable = false)
    private Boolean active = true; // Si el template está activo

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false; // Si es el template por defecto para su categoría

    @Column(length = 100)
    private String language = "es"; // Idioma del template

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy; // ID del usuario que creó el template

    @Column(name = "updated_by")
    private Long updatedBy; // ID del usuario que actualizó el template

    // Enums
    public enum TemplateType {
        NOTIFICATION, // Notificación general
        CONFIRMATION, // Confirmación de acción
        REMINDER,     // Recordatorio
        STATUS_UPDATE, // Actualización de estado
        WELCOME,      // Bienvenida
        REJECTION,    // Rechazo
        APPROVAL      // Aprobación
    }

    public enum TemplateCategory {
        INTERVIEW_ASSIGNMENT,    // Asignación de entrevistas
        INTERVIEW_CONFIRMATION,  // Confirmación de entrevista
        INTERVIEW_REMINDER,      // Recordatorio de entrevista
        INTERVIEW_RESCHEDULE,    // Reprogramación de entrevista
        APPLICATION_STATUS,      // Estado de aplicación
        STUDENT_SELECTION,       // Selección de estudiante
        STUDENT_REJECTION,       // Rechazo de estudiante
        GENERAL_NOTIFICATION,    // Notificación general
        WELCOME_MESSAGE,         // Mensaje de bienvenida
        ADMISSION_RESULTS        // Resultados de admisión
    }

    // Métodos de conveniencia
    public String getFullName() {
        return name + " (" + templateKey + ")";
    }

    public boolean isApplicableForCategory(TemplateCategory targetCategory) {
        return this.category == targetCategory && this.active;
    }

    public boolean canBeUsedAsDefault() {
        return this.active && !this.isDefault;
    }

    // Validaciones de negocio
    public void validateForSending() {
        if (!active) {
            throw new IllegalStateException("No se puede usar un template inactivo");
        }
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new IllegalStateException("El template debe tener contenido HTML");
        }
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalStateException("El template debe tener un asunto");
        }
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
        this.isDefault = false; // Un template inactivo no puede ser por defecto
    }

    public void setAsDefault() {
        if (!active) {
            throw new IllegalStateException("Un template inactivo no puede ser por defecto");
        }
        this.isDefault = true;
    }

    public void removeAsDefault() {
        this.isDefault = false;
    }
}