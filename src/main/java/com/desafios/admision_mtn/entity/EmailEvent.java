package com.desafios.admision_mtn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "email_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_notification_id", nullable = false)
    @JsonBackReference
    private EmailNotification emailNotification;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;
    
    @Column(name = "description", columnDefinition = "text")
    private String description;
    
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_info", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> additionalInfo = Map.of();
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum EventType {
        QUEUED("En cola"),
        SENT("Enviado"),
        OPENED("Abierto"),
        CLICKED("Enlace clicado"),
        RESPONDED("Respondido"),
        BOUNCED("Rebotado"),
        FAILED("Falló"),
        DELIVERED("Entregado"),
        MARKED_SPAM("Marcado como spam"),
        UNSUBSCRIBED("Desuscrito");
        
        private final String displayName;
        
        EventType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Métodos estáticos para crear eventos comunes
    public static EmailEvent createSentEvent(EmailNotification notification) {
        return EmailEvent.builder()
            .emailNotification(notification)
            .eventType(EventType.SENT)
            .build();
    }
    
    public static EmailEvent createOpenedEvent(EmailNotification notification, String ipAddress, String userAgent) {
        return EmailEvent.builder()
            .emailNotification(notification)
            .eventType(EventType.OPENED)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
    }
    
    public static EmailEvent createClickedEvent(EmailNotification notification, String ipAddress, String userAgent, String clickedUrl) {
        return EmailEvent.builder()
            .emailNotification(notification)
            .eventType(EventType.CLICKED)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .additionalInfo(Map.of("clicked_url", clickedUrl))
            .build();
    }
    
    public static EmailEvent createRespondedEvent(EmailNotification notification, String response) {
        return EmailEvent.builder()
            .emailNotification(notification)
            .eventType(EventType.RESPONDED)
            .additionalInfo(Map.of("response", response))
            .build();
    }
}