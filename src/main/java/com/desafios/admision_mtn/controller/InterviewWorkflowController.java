package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.InterviewWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gestionar el workflow completo de entrevistas
 */
@RestController
@RequestMapping("/api/interview-workflow")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CYCLE_DIRECTOR')")
public class InterviewWorkflowController {

    private final InterviewWorkflowService interviewWorkflowService;

    /**
     * Planifica automáticamente entrevistas para aplicaciones pendientes
     */
    @PostMapping("/planify-interviews")
    public ResponseEntity<Map<String, Object>> planifyInterviews() {
        try {
            log.info("🔄 Iniciando planificación automática de entrevistas via REST");
            
            Map<String, Object> result = interviewWorkflowService.planifyInterviewsForPendingApplications();
            
            log.info("✅ Planificación automática completada: {} entrevistas creadas", 
                    result.get("interviewsCreated"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error en planificación automática de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Actualiza el progreso de entrevistas y avanza aplicaciones
     */
    @PostMapping("/update-progress")
    public ResponseEntity<Map<String, Object>> updateInterviewProgress() {
        try {
            log.info("🔄 Actualizando progreso de entrevistas via REST");
            
            Map<String, Object> result = interviewWorkflowService.updateInterviewProgressAndAdvanceApplications();
            
            log.info("✅ Progreso actualizado: {} aplicaciones avanzadas", 
                    result.get("applicationsAdvanced"));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error actualizando progreso de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Genera reporte completo del estado de entrevistas
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getInterviewReport() {
        try {
            log.info("📊 Generando reporte de entrevistas via REST");
            
            Map<String, Object> report = interviewWorkflowService.generateInterviewReport();
            
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Ejecuta proceso completo de workflow de entrevistas
     */
    @PostMapping("/run-complete-workflow")
    public ResponseEntity<Map<String, Object>> runCompleteWorkflow() {
        try {
            log.info("🚀 Ejecutando workflow completo de entrevistas");
            
            Map<String, Object> completeResult = new HashMap<>();
            
            // Paso 1: Planificar entrevistas pendientes
            Map<String, Object> planifyResult = interviewWorkflowService.planifyInterviewsForPendingApplications();
            completeResult.put("planificationResult", planifyResult);
            
            // Paso 2: Actualizar progreso
            Map<String, Object> progressResult = interviewWorkflowService.updateInterviewProgressAndAdvanceApplications();
            completeResult.put("progressResult", progressResult);
            
            // Paso 3: Generar reporte final
            Map<String, Object> reportResult = interviewWorkflowService.generateInterviewReport();
            completeResult.put("reportResult", reportResult);
            
            // Resumen general
            completeResult.put("success", true);
            completeResult.put("message", "Workflow completo de entrevistas ejecutado exitosamente");
            completeResult.put("timestamp", LocalDateTime.now());
            
            log.info("🎉 Workflow completo ejecutado exitosamente");
            
            return ResponseEntity.ok(completeResult);
            
        } catch (Exception e) {
            log.error("❌ Error ejecutando workflow completo de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Obtiene estadísticas del sistema de entrevistas
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getInterviewStats() {
        try {
            log.info("📊 Obteniendo estadísticas del sistema de entrevistas");
            
            Map<String, Object> stats = new HashMap<>();
            
            // Obtener reporte completo
            Map<String, Object> report = interviewWorkflowService.generateInterviewReport();
            
            // Extraer estadísticas principales
            stats.put("totalInterviews", report.get("totalInterviews"));
            stats.put("statusDistribution", report.get("statusDistribution"));
            stats.put("typeDistribution", report.get("typeDistribution"));
            stats.put("modeDistribution", report.get("modeDistribution"));
            stats.put("upcomingInterviews", report.get("upcomingInterviews"));
            stats.put("overdueInterviews", report.get("overdueInterviews"));
            
            // Información del sistema
            stats.put("systemInfo", Map.of(
                "workflowActive", true,
                "version", "1.0.0",
                "features", Map.of(
                    "automaticPlanning", true,
                    "intelligentAssignment", true,
                    "scheduleManagement", true,
                    "progressTracking", true,
                    "reportGeneration", true,
                    "notificationIntegration", true
                )
            ));
            
            stats.put("timestamp", LocalDateTime.now());
            stats.put("success", true);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Endpoint para testing del workflow completo
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testInterviewWorkflow() {
        try {
            log.info("🧪 Ejecutando test completo del workflow de entrevistas");
            
            Map<String, Object> testResult = new HashMap<>();
            
            // Test de planificación
            try {
                Map<String, Object> planifyTest = interviewWorkflowService.planifyInterviewsForPendingApplications();
                testResult.put("planificationTest", Map.of(
                    "status", "OK",
                    "result", planifyTest
                ));
            } catch (Exception e) {
                testResult.put("planificationTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test de progreso
            try {
                Map<String, Object> progressTest = interviewWorkflowService.updateInterviewProgressAndAdvanceApplications();
                testResult.put("progressTest", Map.of(
                    "status", "OK",
                    "result", progressTest
                ));
            } catch (Exception e) {
                testResult.put("progressTest", Map.of(
                    "status", "ERROR", 
                    "error", e.getMessage()
                ));
            }
            
            // Test de reporte
            try {
                Map<String, Object> reportTest = interviewWorkflowService.generateInterviewReport();
                testResult.put("reportTest", Map.of(
                    "status", "OK",
                    "totalInterviews", reportTest.get("totalInterviews")
                ));
            } catch (Exception e) {
                testResult.put("reportTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            testResult.put("success", true);
            testResult.put("message", "Test completo del workflow de entrevistas ejecutado");
            testResult.put("timestamp", LocalDateTime.now());
            
            log.info("✅ Test del workflow de entrevistas completado");
            
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            log.error("❌ Error en test del workflow de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Información del sistema de workflow de entrevistas
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getWorkflowInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("workflowName", "Sistema de Workflow de Entrevistas");
        info.put("version", "1.0.0");
        info.put("description", "Sistema completo para gestión automática de entrevistas en proceso de admisión");
        
        info.put("capabilities", Map.of(
            "automaticPlanning", "Planificación automática de entrevistas para aplicaciones pendientes",
            "intelligentAssignment", "Asignación inteligente de entrevistadores basada en carga de trabajo",
            "scheduleManagement", "Gestión automática de horarios y disponibilidad",
            "progressTracking", "Seguimiento automático del progreso y estados",
            "reportGeneration", "Generación de reportes y métricas detalladas",
            "workflowIntegration", "Integración con sistema de workflow de aplicaciones"
        ));
        
        info.put("endpoints", Map.of(
            "POST /api/interview-workflow/planify-interviews", "Planifica entrevistas automáticamente",
            "POST /api/interview-workflow/update-progress", "Actualiza progreso de entrevistas",
            "GET /api/interview-workflow/report", "Genera reporte completo",
            "POST /api/interview-workflow/run-complete-workflow", "Ejecuta workflow completo",
            "GET /api/interview-workflow/stats", "Obtiene estadísticas del sistema"
        ));
        
        info.put("interviewTypes", Map.of(
            "FAMILY", "Entrevista familiar presencial",
            "PSYCHOLOGICAL", "Evaluación psicológica",
            "ACADEMIC", "Entrevista académica",
            "INDIVIDUAL", "Entrevista individual",
            "BEHAVIORAL", "Evaluación de comportamiento"
        ));
        
        info.put("timestamp", LocalDateTime.now());
        info.put("status", "ACTIVE");
        
        return ResponseEntity.ok(info);
    }
}