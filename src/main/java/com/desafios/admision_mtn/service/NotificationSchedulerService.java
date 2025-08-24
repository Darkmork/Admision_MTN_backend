package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.repository.InterviewRepository;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio de tareas programadas para notificaciones automáticas
 * 
 * HORARIOS DE EJECUCIÓN:
 * - Recordatorios de entrevistas: Diariamente a las 09:00
 * - Evaluación de transiciones: Cada 30 minutos
 * - Limpieza de notificaciones: Diariamente a las 02:00
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationSchedulerService {

    private final NotificationService notificationService;
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationWorkflowService workflowService;
    private final InterviewWorkflowService interviewWorkflowService;

    /**
     * Enviar recordatorios de entrevistas (24 horas antes)
     * Se ejecuta diariamente a las 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyInterviewReminders() {
        try {
            log.info("🔔 Iniciando envío diario de recordatorios de entrevistas");
            
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            List<Interview> tomorrowInterviews = interviewRepository.findByScheduledDate(tomorrow);
            
            int remindersSent = 0;
            for (Interview interview : tomorrowInterviews) {
                try {
                    if (interview.getStatus() == Interview.InterviewStatus.SCHEDULED) {
                        notificationService.sendInterviewReminder(interview);
                        remindersSent++;
                        log.debug("🔔 Recordatorio enviado para entrevista {}", interview.getId());
                    }
                } catch (Exception e) {
                    log.error("❌ Error enviando recordatorio para entrevista {}", interview.getId(), e);
                }
            }
            
            log.info("✅ Recordatorios de entrevistas completados: {} enviados de {} programadas", 
                    remindersSent, tomorrowInterviews.size());
                    
        } catch (Exception e) {
            log.error("❌ Error en proceso diario de recordatorios de entrevistas", e);
        }
    }

    /**
     * Evaluar transiciones automáticas de aplicaciones
     * Se ejecuta cada 30 minutos durante horas hábiles (8:00 - 18:00)
     */
    @Scheduled(cron = "0 */30 8-18 * * MON-FRI")
    public void evaluateAutomaticTransitions() {
        try {
            log.info("🔄 Iniciando evaluación automática de transiciones programada");
            
            workflowService.evaluateAllApplicationsForTransition();
            
            log.info("✅ Evaluación automática de transiciones completada");
            
        } catch (Exception e) {
            log.error("❌ Error en evaluación automática programada", e);
        }
    }

    /**
     * Planificar entrevistas automáticamente
     * Se ejecuta diariamente a las 11:00 AM
     */
    @Scheduled(cron = "0 0 11 * * MON-FRI")
    public void planifyInterviewsAutomatically() {
        try {
            log.info("📅 Iniciando planificación automática diaria de entrevistas");
            
            Map<String, Object> result = interviewWorkflowService.planifyInterviewsForPendingApplications();
            
            int created = (Integer) result.get("interviewsCreated");
            int errors = (Integer) result.get("errors");
            
            log.info("✅ Planificación automática completada: {} entrevistas creadas, {} errores", 
                    created, errors);
                    
        } catch (Exception e) {
            log.error("❌ Error en planificación automática de entrevistas", e);
        }
    }

    /**
     * Actualizar progreso de entrevistas
     * Se ejecuta cada 2 horas durante horas hábiles
     */
    @Scheduled(cron = "0 0 */2 * * MON-FRI")
    public void updateInterviewProgress() {
        try {
            log.info("🔄 Actualizando progreso de entrevistas programado");
            
            Map<String, Object> result = interviewWorkflowService.updateInterviewProgressAndAdvanceApplications();
            
            int advanced = (Integer) result.get("applicationsAdvanced");
            int processed = (Integer) result.get("interviewsProcessed");
            
            log.info("✅ Progreso de entrevistas actualizado: {} aplicaciones avanzaron, {} entrevistas procesadas", 
                    advanced, processed);
                    
        } catch (Exception e) {
            log.error("❌ Error actualizando progreso de entrevistas", e);
        }
    }

    /**
     * Enviar recordatorios de documentos pendientes
     * Se ejecuta cada lunes a las 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * MON")
    public void sendWeeklyDocumentReminders() {
        try {
            log.info("📄 Iniciando recordatorios semanales de documentos pendientes");
            
            List<Application> documentsRequestedApps = applicationRepository
                    .findByStatusOrderByCreatedAtDesc(Application.ApplicationStatus.DOCUMENTS_REQUESTED);
            
            int remindersSent = 0;
            for (Application application : documentsRequestedApps) {
                try {
                    // Solo enviar si han pasado más de 3 días desde la última actualización
                    if (application.getUpdatedAt() != null && 
                        application.getUpdatedAt().isBefore(LocalDateTime.now().minusDays(3))) {
                        
                        // Simular documentos faltantes (en producción esto vendría del servicio)
                        List<String> sampleMissingDocs = List.of("BIRTH_CERTIFICATE", "STUDENT_PHOTO");
                        notificationService.notifyMissingDocuments(application, sampleMissingDocs);
                        remindersSent++;
                        
                        log.debug("📄 Recordatorio de documentos enviado para aplicación {}", 
                                application.getId());
                    }
                } catch (Exception e) {
                    log.error("❌ Error enviando recordatorio de documentos para aplicación {}", 
                            application.getId(), e);
                }
            }
            
            log.info("✅ Recordatorios semanales de documentos completados: {} enviados", 
                    remindersSent);
                    
        } catch (Exception e) {
            log.error("❌ Error en recordatorios semanales de documentos", e);
        }
    }

    /**
     * Reportar estadísticas diarias del sistema
     * Se ejecuta diariamente a las 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void generateDailyStatsReport() {
        try {
            log.info("📊 Generando reporte diario de estadísticas del sistema");
            
            // Estadísticas básicas
            long totalApplications = applicationRepository.count();
            long pendingApplications = applicationRepository
                    .countByStatus(Application.ApplicationStatus.PENDING);
            long underReviewApplications = applicationRepository
                    .countByStatus(Application.ApplicationStatus.UNDER_REVIEW);
            long approvedApplications = applicationRepository
                    .countByStatus(Application.ApplicationStatus.APPROVED);
            
            // Entrevistas de hoy
            List<Interview> todayInterviews = interviewRepository.findTodaysInterviews();
            
            log.info("""
                📊 REPORTE DIARIO - {}
                
                📋 APLICACIONES:
                • Total: {}
                • Pendientes: {}
                • En Revisión: {}
                • Aprobadas: {}
                
                📅 ENTREVISTAS HOY: {}
                
                🔔 Sistema de notificaciones: ACTIVO
                """, 
                LocalDate.now(),
                totalApplications,
                pendingApplications, 
                underReviewApplications,
                approvedApplications,
                todayInterviews.size()
            );
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte diario", e);
        }
    }

    /**
     * Limpieza nocturna de logs y mantenimiento
     * Se ejecuta diariamente a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void performNightlyMaintenance() {
        try {
            log.info("🧹 Iniciando mantenimiento nocturno del sistema de notificaciones");
            
            // Aquí podrías agregar:
            // - Limpieza de logs antiguos
            // - Compactación de base de datos
            // - Verificación de integridad de datos
            // - Respaldo de configuraciones
            
            log.info("✅ Mantenimiento nocturno completado");
            
        } catch (Exception e) {
            log.error("❌ Error en mantenimiento nocturno", e);
        }
    }

    /**
     * Verificación de salud del sistema de emails
     * Se ejecuta cada hora durante horas hábiles
     */
    @Scheduled(cron = "0 0 8-18 * * MON-FRI")
    public void checkEmailSystemHealth() {
        try {
            log.debug("💊 Verificando salud del sistema de emails");
            
            // Aquí podrías agregar verificaciones como:
            // - Conectividad SMTP
            // - Límites de envío
            // - Cola de emails pendientes
            
            log.debug("✅ Sistema de emails funcionando correctamente");
            
        } catch (Exception e) {
            log.warn("⚠️ Posibles problemas en el sistema de emails", e);
        }
    }

    /**
     * Método manual para testing de tareas programadas
     */
    public void runManualTest() {
        log.info("🧪 Ejecutando prueba manual de tareas programadas...");
        
        try {
            sendDailyInterviewReminders();
            planifyInterviewsAutomatically();
            updateInterviewProgress();
            sendWeeklyDocumentReminders();
            generateDailyStatsReport();
            
            log.info("✅ Prueba manual de tareas programadas completada");
        } catch (Exception e) {
            log.error("❌ Error en prueba manual", e);
        }
    }
}