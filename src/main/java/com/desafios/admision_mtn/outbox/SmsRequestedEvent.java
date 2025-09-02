// src/main/java/com/desafios/admision_mtn/outbox/SmsRequestedEvent.java

package com.desafios.admision_mtn.outbox;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento SmsRequested.v1 para el patrón Outbox
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequestedEvent {

    @JsonProperty("schema")
    @Builder.Default
    private String schema = "notifications.sms.requested.v1";

    @JsonProperty("message_id")
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("to")
    private String to; // +56 9 1234 5678

    @JsonProperty("text")
    private String text; // <= 160 caracteres

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

    /**
     * Crea un evento de SMS simple
     */
    public static SmsRequestedEvent create(String to, String text, String idempotencyKey) {
        return SmsRequestedEvent.builder()
                .to(normalizePhoneNumber(to))
                .text(truncateText(text))
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateId(UUID.randomUUID().toString())
                .build();
    }

    /**
     * Crea un SMS de verificación de código
     */
    public static SmsRequestedEvent createVerificationSms(
            String to, String verificationCode, String idempotencyKey) {
        
        String text = String.format(
                "Tu código de verificación para Admisión MTN es: %s. " +
                "Este código expira en 15 minutos.", verificationCode);
        
        return SmsRequestedEvent.builder()
                .to(normalizePhoneNumber(to))
                .text(truncateText(text))
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.USER)
                .aggregateId(to)
                .build();
    }

    /**
     * Crea un SMS de recordatorio de entrevista
     */
    public static SmsRequestedEvent createInterviewReminderSms(
            String to, String studentName, String interviewDate, String idempotencyKey) {
        
        String text = String.format(
                "Recordatorio: Entrevista de admisión para %s el %s. " +
                "Colegio Monte Tabor y Nazaret.", studentName, interviewDate);
        
        return SmsRequestedEvent.builder()
                .to(normalizePhoneNumber(to))
                .text(truncateText(text))
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.INTERVIEW)
                .aggregateId(idempotencyKey) // Usar idempotency key como ID del agregado
                .build();
    }

    /**
     * Crea un SMS de notificación de estado de postulación
     */
    public static SmsRequestedEvent createApplicationStatusSms(
            String to, String studentName, String status, String idempotencyKey) {
        
        String statusMessage = switch (status.toUpperCase()) {
            case "APPROVED" -> "ha sido APROBADA";
            case "REJECTED" -> "no ha sido seleccionada en esta oportunidad";
            case "WAITLIST" -> "está en LISTA DE ESPERA";
            case "INTERVIEW_SCHEDULED" -> "tiene entrevista programada";
            default -> "ha sido actualizada";
        };
        
        String text = String.format(
                "Estimado apoderado, la postulación de %s %s. " +
                "Revise su email para más detalles.", studentName, statusMessage);
        
        return SmsRequestedEvent.builder()
                .to(normalizePhoneNumber(to))
                .text(truncateText(text))
                .idempotencyKey(idempotencyKey)
                .correlationId(UUID.randomUUID().toString())
                .aggregateType(OutboxEntity.AggregateTypes.APPLICATION)
                .aggregateId(idempotencyKey)
                .build();
    }

    /**
     * Normaliza un número de teléfono al formato internacional chileno
     */
    private static String normalizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        
        // Remover espacios y caracteres especiales
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Si ya tiene +56, mantenerlo
        if (cleaned.startsWith("+56")) {
            return formatChileanPhone(cleaned);
        }
        
        // Si empieza con 56, agregar +
        if (cleaned.startsWith("56") && cleaned.length() == 11) {
            return formatChileanPhone("+" + cleaned);
        }
        
        // Si es un número de 9 dígitos que empieza con 9, agregar +56
        if (cleaned.startsWith("9") && cleaned.length() == 9) {
            return formatChileanPhone("+56" + cleaned);
        }
        
        // Si es un número de 8 dígitos, asumir que le falta el 9 inicial
        if (cleaned.length() == 8) {
            return formatChileanPhone("+569" + cleaned);
        }
        
        // Devolver tal como está si no se puede normalizar
        return phone;
    }

    /**
     * Formatea un número chileno con espacios para legibilidad
     */
    private static String formatChileanPhone(String phone) {
        // +56912345678 -> +56 9 1234 5678
        if (phone.startsWith("+56") && phone.length() == 12) {
            return phone.substring(0, 3) + " " + 
                   phone.substring(3, 4) + " " +
                   phone.substring(4, 8) + " " +
                   phone.substring(8);
        }
        return phone;
    }

    /**
     * Trunca el texto a máximo 160 caracteres
     */
    private static String truncateText(String text) {
        if (text == null) {
            return null;
        }
        
        if (text.length() <= 160) {
            return text;
        }
        
        // Truncar a 157 caracteres y agregar "..."
        return text.substring(0, 157) + "...";
    }

    /**
     * Valida que el evento tenga los campos requeridos
     */
    public boolean isValid() {
        return to != null && !to.trim().isEmpty() &&
               text != null && !text.trim().isEmpty() &&
               text.length() <= 160 &&
               idempotencyKey != null && !idempotencyKey.trim().isEmpty() &&
               isValidPhoneNumber(to);
    }

    /**
     * Verifica si es un número de teléfono válido
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        
        // Verificar formato básico de teléfono chileno
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Formatos válidos:
        // +56912345678 (12 dígitos)
        // 56912345678 (11 dígitos)
        // 912345678 (9 dígitos empezando en 9)
        return cleaned.matches("^(\\+56|56)?9\\d{8}$");
    }

    /**
     * Obtiene la longitud del texto
     */
    public int getTextLength() {
        return text != null ? text.length() : 0;
    }

    /**
     * Verifica si el texto está dentro del límite de SMS
     */
    public boolean isWithinSmsLimit() {
        return getTextLength() <= 160;
    }

    /**
     * Obtiene el número de teléfono enmascarado para logs
     */
    public String getMaskedPhoneNumber() {
        if (to == null || to.length() < 4) {
            return "***";
        }
        return "***" + to.substring(to.length() - 4);
    }

    /**
     * Verifica si es un número de teléfono chileno
     */
    public boolean isChileanPhoneNumber() {
        return to != null && (to.startsWith("+56") || to.startsWith("56") || 
               (to.startsWith("9") && to.replaceAll("[\\s\\-\\(\\)]", "").length() == 9));
    }
}