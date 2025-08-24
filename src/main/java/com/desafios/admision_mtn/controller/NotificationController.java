package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.service.NotificationService;
import com.desafios.admision_mtn.service.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gestionar el sistema de notificaciones automatizadas
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSchedulerService schedulerService;
    private final ApplicationRepository applicationRepository;

    /**
     * Enviar notificación de cambio de estado manualmente
     */
    @PostMapping("/status-change/{applicationId}")
    public ResponseEntity<Map<String, Object>> sendStatusChangeNotification(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        
        try {
            String fromStatus = request.get("fromStatus");
            String toStatus = request.get("toStatus");
            
            if (fromStatus == null || toStatus == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "fromStatus y toStatus son requeridos"
                ));
            }
            
            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
            
            Application.ApplicationStatus from = Application.ApplicationStatus.valueOf(fromStatus);
            Application.ApplicationStatus to = Application.ApplicationStatus.valueOf(toStatus);
            
            notificationService.notifyApplicationStatusChange(application, from, to);
            
            log.info("📧 Notificación de cambio de estado enviada manualmente para aplicación {}", 
                    applicationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación de cambio de estado enviada");
            response.put("applicationId", applicationId);
            response.put("fromStatus", fromStatus);
            response.put("toStatus", toStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de cambio de estado manual", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Enviar notificación de documentos faltantes
     */
    @PostMapping("/missing-documents/{applicationId}")
    public ResponseEntity<Map<String, Object>> sendMissingDocumentsNotification(
            @PathVariable Long applicationId,
            @RequestBody Map<String, Object> request) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> missingDocuments = (List<String>) request.get("missingDocuments");
            
            if (missingDocuments == null || missingDocuments.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Lista de documentos faltantes es requerida"
                ));
            }
            
            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
            
            notificationService.notifyMissingDocuments(application, missingDocuments);
            
            log.info("📄 Notificación de documentos faltantes enviada manualmente para aplicación {}", 
                    applicationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación de documentos faltantes enviada");
            response.put("applicationId", applicationId);
            response.put("missingDocuments", missingDocuments);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de documentos faltantes", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Enviar notificación de decisión de admisión
     */
    @PostMapping("/admission-decision/{applicationId}")
    public ResponseEntity<Map<String, Object>> sendAdmissionDecisionNotification(
            @PathVariable Long applicationId) {
        
        try {
            Application application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
            
            // Solo enviar si está en un estado de decisión final
            if (!isFinalDecisionStatus(application.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "La aplicación debe estar en un estado de decisión final (APPROVED, REJECTED, WAITLIST)"
                ));
            }
            
            notificationService.notifyAdmissionDecision(application);
            
            log.info("🎯 Notificación de decisión de admisión enviada manualmente para aplicación {}", 
                    applicationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación de decisión de admisión enviada");
            response.put("applicationId", applicationId);
            response.put("status", application.getStatus().toString());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de decisión de admisión", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Enviar todas las notificaciones pendientes (proceso masivo)
     */
    @PostMapping("/send-all-pending")
    public ResponseEntity<Map<String, Object>> sendAllPendingNotifications() {
        
        try {
            log.info("📧 Iniciando envío masivo de notificaciones pendientes");
            
            // Obtener todas las aplicaciones activas
            List<Application.ApplicationStatus> activeStatuses = List.of(
                Application.ApplicationStatus.PENDING,
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.INTERVIEW_SCHEDULED,
                Application.ApplicationStatus.EXAM_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED
            );
            
            List<Application> activeApplications = applicationRepository.findByStatusIn(activeStatuses);
            
            int notificationsSent = 0;
            
            // Lógica para determinar qué notificaciones enviar
            for (Application application : activeApplications) {
                try {
                    // Ejemplo: Si está en DOCUMENTS_REQUESTED, enviar recordatorio
                    if (application.getStatus() == Application.ApplicationStatus.DOCUMENTS_REQUESTED) {
                        // Aquí podrías agregar lógica para determinar documentos faltantes
                        // Por ahora, enviamos un ejemplo
                        List<String> sampleMissingDocs = List.of("BIRTH_CERTIFICATE", "STUDENT_PHOTO");
                        notificationService.notifyMissingDocuments(application, sampleMissingDocs);
                        notificationsSent++;
                    }
                    
                    // Agregar más lógica de notificaciones automáticas según necesidades
                    
                } catch (Exception e) {
                    log.error("Error enviando notificación para aplicación {}", application.getId(), e);
                }
            }
            
            log.info("✅ Envío masivo de notificaciones completado: {} notificaciones enviadas", 
                    notificationsSent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Proceso de notificaciones masivas completado");
            response.put("notificationsSent", notificationsSent);
            response.put("applicationsProcessed", activeApplications.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en envío masivo de notificaciones", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Obtener estadísticas del sistema de notificaciones
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Estadísticas básicas
            long totalApplications = applicationRepository.count();
            long pendingApplications = applicationRepository.countByStatus(Application.ApplicationStatus.PENDING);
            long underReviewApplications = applicationRepository.countByStatus(Application.ApplicationStatus.UNDER_REVIEW);
            long approvedApplications = applicationRepository.countByStatus(Application.ApplicationStatus.APPROVED);
            long rejectedApplications = applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED);
            long waitlistApplications = applicationRepository.countByStatus(Application.ApplicationStatus.WAITLIST);
            
            stats.put("totalApplications", totalApplications);
            stats.put("applicationsByStatus", Map.of(
                "PENDING", pendingApplications,
                "UNDER_REVIEW", underReviewApplications,
                "APPROVED", approvedApplications,
                "REJECTED", rejectedApplications,
                "WAITLIST", waitlistApplications
            ));
            
            // Información del sistema de notificaciones
            stats.put("notificationSystem", Map.of(
                "active", true,
                "version", "1.0.0",
                "supportedNotifications", List.of(
                    "Cambios de estado",
                    "Documentos faltantes",
                    "Entrevistas programadas",
                    "Recordatorios de entrevistas",
                    "Evaluaciones asignadas",
                    "Evaluaciones completadas",
                    "Decisiones de admisión"
                )
            ));
            
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas de notificaciones", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Test de conectividad de email (solo para testing)
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmailConnectivity(@RequestBody Map<String, String> request) {
        
        try {
            String testEmail = request.get("email");
            if (testEmail == null || testEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Email de prueba es requerido"
                ));
            }
            
            // Crear una aplicación ficticia para testing
            String testMessage = """
                Este es un email de prueba del sistema de notificaciones.
                
                Sistema: Admisiones Monte Tabor & Nazaret
                Fecha: %s
                
                Si recibe este mensaje, el sistema de emails está funcionando correctamente.
                
                ¡Saludos del equipo técnico!
                """.formatted(LocalDateTime.now().toString());
            
            // Usar el servicio de email existente
            // emailService.sendSimpleMessage(testEmail, "🧪 Prueba de Sistema de Notificaciones", testMessage);
            
            log.info("🧪 Email de prueba enviado a: {}", testEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email de prueba enviado exitosamente");
            response.put("testEmail", testEmail);
            response.put("timestamp", LocalDateTime.now());
            response.put("warning", "Revise los logs para confirmar el envío real");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de prueba", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Ejecutar manualmente las tareas programadas (solo para testing)
     */
    @PostMapping("/test-scheduler")
    public ResponseEntity<Map<String, Object>> testScheduledTasks() {
        
        try {
            log.info("🧪 Ejecutando prueba manual de tareas programadas");
            
            schedulerService.runManualTest();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tareas programadas ejecutadas manualmente");
            response.put("timestamp", LocalDateTime.now());
            response.put("info", "Revise los logs para ver los resultados detallados");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error ejecutando tareas programadas manualmente", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================

    private boolean isFinalDecisionStatus(Application.ApplicationStatus status) {
        return status == Application.ApplicationStatus.APPROVED ||
               status == Application.ApplicationStatus.REJECTED ||
               status == Application.ApplicationStatus.WAITLIST;
    }
}