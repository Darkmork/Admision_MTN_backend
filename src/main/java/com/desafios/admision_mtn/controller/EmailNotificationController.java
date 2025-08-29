package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.EmailNotification;
import com.desafios.admision_mtn.entity.EmailEvent;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@Slf4j
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class EmailNotificationController {

    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailEventRepository emailEventRepository;

    // ===== ENDPOINT PÚBLICO PARA DASHBOARD =====
    
    /**
     * Obtener todas las notificaciones de email (endpoint público para dashboard)
     */
    @GetMapping("/api/email-notifications")
    public ResponseEntity<List<EmailNotification>> getEmailNotificationsPublic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        try {
            log.info("📧 Solicitando notificaciones de email (endpoint público)");
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<EmailNotification> notificationsPage = emailNotificationRepository.findAllByOrderByCreatedAtDesc(pageable);
            
            List<EmailNotification> notifications = notificationsPage.getContent();
            log.info("✅ Enviando {} notificaciones de email", notifications.size());
            
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo notificaciones de email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== ENDPOINTS ADMIN CON AUTENTICACIÓN =====

    /**
     * Obtener todas las notificaciones de email con paginación (Admin)
     */
    @GetMapping("/api/admin/email-notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllEmailNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<EmailNotification> notificationsPage = emailNotificationRepository.findAllByOrderByCreatedAtDesc(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationsPage.getContent());
            response.put("currentPage", notificationsPage.getNumber());
            response.put("totalItems", notificationsPage.getTotalElements());
            response.put("totalPages", notificationsPage.getTotalPages());
            response.put("pageSize", notificationsPage.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo notificaciones de email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener notificaciones por aplicación
     */
    @GetMapping("/api/admin/email-notifications/application/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailNotification>> getNotificationsByApplication(@PathVariable Long applicationId) {
        try {
            List<EmailNotification> notifications = emailNotificationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error obteniendo notificaciones para aplicación {}: {}", applicationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener estadísticas de emails
     */
    @GetMapping("/api/admin/email-notifications/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEmailStats() {
        try {
            // Estadísticas generales
            Object[] generalStats = emailNotificationRepository.getEmailStats();
            
            // Estadísticas por tipo
            List<Object[]> typeStats = emailNotificationRepository.getEmailStatsByType();
            
            // Emails recientes (últimos 30 días)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<EmailNotification> recentEmails = emailNotificationRepository.findRecentEmails(thirtyDaysAgo);
            
            // Emails pendientes de respuesta
            List<EmailNotification> pendingResponses = emailNotificationRepository.findPendingResponses();
            
            // Emails no leídos
            List<EmailNotification> unopenedEmails = emailNotificationRepository.findUnopened();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Estadísticas generales
            if (generalStats != null && generalStats.length >= 3) {
                long total = generalStats[0] != null ? ((Number) generalStats[0]).longValue() : 0;
                long opened = generalStats[1] != null ? ((Number) generalStats[1]).longValue() : 0;
                long responded = generalStats[2] != null ? ((Number) generalStats[2]).longValue() : 0;
                
                stats.put("totalEmails", total);
                stats.put("openedEmails", opened);
                stats.put("respondedEmails", responded);
                stats.put("openRate", total > 0 ? Math.round((double) opened / total * 100) : 0);
                stats.put("responseRate", total > 0 ? Math.round((double) responded / total * 100) : 0);
            }
            
            // Estadísticas por tipo
            Map<String, Long> emailTypeStats = new HashMap<>();
            for (Object[] row : typeStats) {
                if (row.length >= 2 && row[0] != null && row[1] != null) {
                    emailTypeStats.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
            stats.put("emailsByType", emailTypeStats);
            
            // Contadores adicionales
            stats.put("recentEmailsCount", recentEmails.size());
            stats.put("pendingResponsesCount", pendingResponses.size());
            stats.put("unopenedEmailsCount", unopenedEmails.size());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener emails no leídos
     */
    @GetMapping("/api/admin/email-notifications/unopened")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailNotification>> getUnopenedEmails() {
        try {
            List<EmailNotification> unopenedEmails = emailNotificationRepository.findUnopened();
            return ResponseEntity.ok(unopenedEmails);
        } catch (Exception e) {
            log.error("Error obteniendo emails no leídos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener emails pendientes de respuesta
     */
    @GetMapping("/api/admin/email-notifications/pending-responses")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailNotification>> getPendingResponses() {
        try {
            List<EmailNotification> pendingResponses = emailNotificationRepository.findPendingResponses();
            return ResponseEntity.ok(pendingResponses);
        } catch (Exception e) {
            log.error("Error obteniendo emails pendientes de respuesta: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener eventos de una notificación específica
     */
    @GetMapping("/api/admin/email-notifications/{notificationId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailEvent>> getNotificationEvents(@PathVariable Long notificationId) {
        try {
            EmailNotification notification = emailNotificationRepository.findById(notificationId)
                .orElse(null);
                
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<EmailEvent> events = emailEventRepository.findByEmailNotificationOrderByCreatedAtDesc(notification);
            return ResponseEntity.ok(events);
            
        } catch (Exception e) {
            log.error("Error obteniendo eventos para notificación {}: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener detalle de una notificación específica
     */
    @GetMapping("/api/admin/email-notifications/{notificationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailNotification> getNotificationById(@PathVariable Long notificationId) {
        try {
            EmailNotification notification = emailNotificationRepository.findById(notificationId)
                .orElse(null);
                
            if (notification == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(notification);
            
        } catch (Exception e) {
            log.error("Error obteniendo notificación {}: {}", notificationId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtener estadísticas por escuela
     */
    @GetMapping("/api/admin/email-notifications/stats/by-school")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatsBySchool() {
        try {
            Map<String, Object> schoolStats = new HashMap<>();
            
            // Estadísticas por escuela objetivo
            List<EmailNotification> monteTabor = emailNotificationRepository.findByTargetSchoolOrderByCreatedAtDesc(
                EmailNotification.TargetSchool.MONTE_TABOR);
            List<EmailNotification> nazaret = emailNotificationRepository.findByTargetSchoolOrderByCreatedAtDesc(
                EmailNotification.TargetSchool.NAZARET);
            
            // Calcular métricas para Monte Tabor
            Map<String, Object> mtStats = new HashMap<>();
            mtStats.put("total", monteTabor.size());
            mtStats.put("opened", monteTabor.stream().mapToInt(n -> n.hasBeenOpened() ? 1 : 0).sum());
            mtStats.put("responded", monteTabor.stream().mapToInt(n -> n.hasResponded() ? 1 : 0).sum());
            
            // Calcular métricas para Nazaret
            Map<String, Object> nazStats = new HashMap<>();
            nazStats.put("total", nazaret.size());
            nazStats.put("opened", nazaret.stream().mapToInt(n -> n.hasBeenOpened() ? 1 : 0).sum());
            nazStats.put("responded", nazaret.stream().mapToInt(n -> n.hasResponded() ? 1 : 0).sum());
            
            schoolStats.put("MONTE_TABOR", mtStats);
            schoolStats.put("NAZARET", nazStats);
            
            return ResponseEntity.ok(schoolStats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas por escuela: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}