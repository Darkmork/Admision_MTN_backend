package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.ApplicationWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gestionar el flujo automático de aplicaciones
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Slf4j
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
@PreAuthorize("hasRole('ADMIN')")
public class WorkflowController {

    private final ApplicationWorkflowService workflowService;

    /**
     * Evalúa una aplicación específica para transición automática
     */
    @PostMapping("/evaluate/{applicationId}")
    public ResponseEntity<Map<String, Object>> evaluateApplication(@PathVariable Long applicationId) {
        try {
            log.info("🔄 Evaluando aplicación {} para transición automática", applicationId);
            
            boolean advanced = workflowService.evaluateAndAdvanceApplication(applicationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("applicationId", applicationId);
            response.put("transitioned", advanced);
            response.put("message", advanced ? 
                "Aplicación avanzó al siguiente estado automáticamente" : 
                "Aplicación no cumple condiciones para avanzar");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error evaluando aplicación {}", applicationId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("applicationId", applicationId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Evalúa todas las aplicaciones activas para transiciones automáticas
     */
    @PostMapping("/evaluate-all")
    public ResponseEntity<Map<String, Object>> evaluateAllApplications() {
        try {
            log.info("🔄 Iniciando evaluación masiva de todas las aplicaciones");
            
            workflowService.evaluateAllApplicationsForTransition();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Evaluación masiva completada exitosamente");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en evaluación masiva", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene información del estado actual del workflow
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWorkflowStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("workflowActive", true);
            status.put("message", "Sistema de workflow automático activo");
            status.put("timestamp", LocalDateTime.now());
            status.put("version", "1.0.0");
            
            // Info sobre las transiciones posibles
            status.put("transitions", Map.of(
                "PENDING", "UNDER_REVIEW (cuando documentos completos)",
                "UNDER_REVIEW", "INTERVIEW_SCHEDULED (cuando evaluaciones asignadas)",
                "INTERVIEW_SCHEDULED", "EXAM_SCHEDULED (cuando entrevista completada)",  
                "EXAM_SCHEDULED", "APPROVED/REJECTED/WAITLIST (cuando evaluaciones completas)",
                "DOCUMENTS_REQUESTED", "UNDER_REVIEW (cuando documentos se suben)"
            ));
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo estado del workflow", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Endpoint para forzar una transición específica (solo para testing)
     */
    @PostMapping("/force-transition/{applicationId}")
    public ResponseEntity<Map<String, Object>> forceTransition(
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {
        try {
            String targetStatus = request.get("targetStatus");
            if (targetStatus == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "targetStatus es requerido")
                );
            }
            
            log.warn("⚠️ FORZANDO transición para aplicación {} a estado {}", applicationId, targetStatus);
            
            // Este endpoint es solo para testing - en producción debería estar deshabilitado
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Transición forzada (solo testing)");
            response.put("applicationId", applicationId);
            response.put("targetStatus", targetStatus);
            response.put("timestamp", LocalDateTime.now());
            response.put("warning", "Este es un endpoint de testing - usar con precaución");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en transición forzada", e);
            return ResponseEntity.badRequest().body(
                Map.of("error", e.getMessage())
            );
        }
    }
}