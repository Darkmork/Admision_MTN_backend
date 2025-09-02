// src/main/java/com/desafios/admision_mtn/outbox/EmailRequestedEvent.java

package com.desafios.admision_mtn.outbox;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Evento EmailRequested.v1 para el patrón Outbox
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestedEvent {

    @JsonProperty("schema")
    @Builder.Default
    private String schema = "notifications.email.requested.v1";

    @JsonProperty("message_id")
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("to")
    private List<String> to;

    @JsonProperty("cc")
    private List<String> cc;

    @JsonProperty("bcc")
    private List<String> bcc;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("variables")
    private Map<String, Object> variables;

    @JsonProperty("body_text")
    private String bodyText;

    @JsonProperty("body_html")
    private String bodyHtml;

    @JsonProperty("attachments")
    private List<Attachment> attachments;

    @JsonProperty("priority")
    @Builder.Default
    private String priority = "normal"; // normal | high

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", timezone = "UTC")
    @Builder.Default
    private Instant createdAt = Instant.now();

    // Campos adicionales para el contexto del agregado
    @JsonProperty("aggregate_type")
    @Builder.Default
    private String aggregateType = OutboxEntity.AggregateTypes.SYSTEM;

    @JsonProperty("aggregate_id")
    private String aggregateId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        @JsonProperty("name")
        private String name;

        @JsonProperty("content_base64")
        private String contentBase64;

        @JsonProperty("content_type")
        private String contentType;
    }

    /**
     * Crea un evento de email simple con template
     */
    public static EmailRequestedEvent createWithTemplate(
            String to, String subject, String templateId, 
            Map<String, Object> variables, String idempotencyKey) {
        
        return EmailRequestedEvent.builder()
                .to(List.of(to))
                .subject(subject)
                .templateId(templateId)
                .variables(variables)
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Crea un evento de email con contenido directo
     */
    public static EmailRequestedEvent createWithContent(
            String to, String subject, String bodyText, String bodyHtml, 
            String idempotencyKey) {
        
        return EmailRequestedEvent.builder()
                .to(List.of(to))
                .subject(subject)
                .bodyText(bodyText)
                .bodyHtml(bodyHtml)
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Crea un evento de email de verificación
     */
    public static EmailRequestedEvent createVerificationEmail(
            String to, String verificationCode, String userFullName, String idempotencyKey) {
        
        Map<String, Object> variables = Map.of(
                "nombre", userFullName,
                "codigo_verificacion", verificationCode,
                "url_verificacion", "https://admision.mtn.cl/verificar?code=" + verificationCode
        );
        
        return EmailRequestedEvent.builder()
                .to(List.of(to))
                .subject("Verificación de cuenta - Colegio Monte Tabor y Nazaret")
                .templateId("email_verification")
                .variables(variables)
                .priority("high")
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.USER)
                .aggregateId(to) // Usar email como ID del agregado para usuarios
                .build();
    }

    /**
     * Crea un evento de email de bienvenida con credenciales
     */
    public static EmailRequestedEvent createWelcomeEmail(
            String to, String userFullName, String temporaryPassword, String idempotencyKey) {
        
        Map<String, Object> variables = Map.of(
                "nombre", userFullName,
                "email", to,
                "password_temporal", temporaryPassword,
                "url_login", "https://admision.mtn.cl/login"
        );
        
        return EmailRequestedEvent.builder()
                .to(List.of(to))
                .subject("Bienvenido al Sistema de Admisión - Credenciales de acceso")
                .templateId("welcome_credentials")
                .variables(variables)
                .priority("high")
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.USER)
                .aggregateId(to)
                .build();
    }

    /**
     * Crea un evento de email de postulación recibida
     */
    public static EmailRequestedEvent createApplicationReceivedEmail(
            String to, String studentName, String applicationId, String idempotencyKey) {
        
        Map<String, Object> variables = Map.of(
                "nombre_estudiante", studentName,
                "numero_postulacion", applicationId,
                "fecha_recepcion", Instant.now().toString(),
                "url_seguimiento", "https://admision.mtn.cl/seguimiento/" + applicationId
        );
        
        return EmailRequestedEvent.builder()
                .to(List.of(to))
                .subject("Postulación recibida - " + studentName)
                .templateId("application_received")
                .variables(variables)
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.APPLICATION)
                .aggregateId(applicationId)
                .build();
    }

    /**
     * Valida que el evento tenga los campos requeridos
     */
    public boolean isValid() {
        return to != null && !to.isEmpty() &&
               to.get(0) != null && to.get(0).contains("@") &&
               (subject != null && !subject.trim().isEmpty()) &&
               (templateId != null || bodyText != null || bodyHtml != null) &&
               idempotencyKey != null && !idempotencyKey.trim().isEmpty();
    }

    /**
     * Obtiene el primer destinatario
     */
    public String getPrimaryRecipient() {
        return to != null && !to.isEmpty() ? to.get(0) : null;
    }

    /**
     * Verifica si es un email de alta prioridad
     */
    public boolean isHighPriority() {
        return "high".equals(priority);
    }

    /**
     * Verifica si usa template
     */
    public boolean usesTemplate() {
        return templateId != null && !templateId.trim().isEmpty();
    }

    /**
     * Verifica si tiene contenido directo
     */
    public boolean hasDirectContent() {
        return (bodyText != null && !bodyText.trim().isEmpty()) ||
               (bodyHtml != null && !bodyHtml.trim().isEmpty());
    }

    /**
     * Verifica si tiene adjuntos
     */
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
}