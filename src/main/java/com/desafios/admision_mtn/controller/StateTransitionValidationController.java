package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.service.StateTransitionValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Controller para validación de transiciones de estado
 * 
 * Proporciona endpoints para validar y consultar transiciones permitidas
 * en el flujo de estados de aplicaciones del proceso de admisión
 */
@RestController
@RequestMapping("/api/state-validation")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'CYCLE_DIRECTOR')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class StateTransitionValidationController {

    private final StateTransitionValidationService validationService;

    /**
     * Valida si una transición específica es permitida
     */
    @PostMapping("/validate-transition")
    public ResponseEntity<Map<String, Object>> validateTransition(
            @RequestParam Long applicationId,
            @RequestParam String fromStatus,
            @RequestParam String toStatus) {
        
        try {
            log.info("🔍 Validando transición via REST: aplicación {} de {} a {}", 
                    applicationId, fromStatus, toStatus);
            
            Application.ApplicationStatus from = Application.ApplicationStatus.valueOf(fromStatus.toUpperCase());
            Application.ApplicationStatus to = Application.ApplicationStatus.valueOf(toStatus.toUpperCase());
            
            StateTransitionValidationService.ValidationResult result = 
                    validationService.validateTransition(applicationId, from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("message", result.getMessage());
            response.put("errors", result.getErrors());
            response.put("metadata", result.getMetadata());
            response.put("applicationId", applicationId);
            response.put("fromStatus", fromStatus);
            response.put("toStatus", toStatus);
            response.put("timestamp", LocalDateTime.now());
            response.put("success", true);
            
            if (result.isValid()) {
                log.info("✅ Transición validada correctamente");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ Transición no válida: {}", result.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Estado inválido en validación", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("valid", false);
            errorResponse.put("error", "Estado inválido: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("❌ Error validando transición", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("valid", false);
            errorResponse.put("error", "Error interno: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene las transiciones válidas desde un estado específico
     */
    @GetMapping("/valid-transitions/{fromStatus}")
    public ResponseEntity<Map<String, Object>> getValidTransitions(@PathVariable String fromStatus) {
        try {
            log.info("📋 Consultando transiciones válidas desde estado: {}", fromStatus);
            
            Application.ApplicationStatus from = Application.ApplicationStatus.valueOf(fromStatus.toUpperCase());
            Set<Application.ApplicationStatus> validTransitions = validationService.getValidTransitions(from);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fromStatus", fromStatus);
            response.put("validTransitions", validTransitions);
            response.put("transitionCount", validTransitions.size());
            response.put("timestamp", LocalDateTime.now());
            response.put("success", true);
            
            log.info("✅ Encontradas {} transiciones válidas desde {}", validTransitions.size(), fromStatus);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Estado inválido: {}", fromStatus, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Estado inválido: " + fromStatus);
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            log.error("❌ Error consultando transiciones válidas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtiene información completa sobre el flujo de estados
     */
    @GetMapping("/workflow-info")
    public ResponseEntity<Map<String, Object>> getWorkflowInfo() {
        try {
            log.info("📊 Generando información del flujo de estados");
            
            Map<String, Object> workflowInfo = new HashMap<>();
            
            // Información de todos los estados
            Map<String, Object> allStates = new HashMap<>();
            for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
                Map<String, Object> stateInfo = new HashMap<>();
                stateInfo.put("name", status.name());
                stateInfo.put("validTransitions", validationService.getValidTransitions(status));
                stateInfo.put("isFinal", isFinalStatus(status));
                stateInfo.put("description", getStatusDescription(status));
                allStates.put(status.name(), stateInfo);
            }
            
            workflowInfo.put("states", allStates);
            workflowInfo.put("totalStates", Application.ApplicationStatus.values().length);
            
            // Flujo principal
            workflowInfo.put("mainFlow", java.util.List.of(
                "PENDING",
                "UNDER_REVIEW", 
                "INTERVIEW_SCHEDULED",
                "EXAM_SCHEDULED",
                "APPROVED/REJECTED/WAITLIST"
            ));
            
            // Estados especiales
            workflowInfo.put("specialStates", Map.of(
                "DOCUMENTS_REQUESTED", "Puede ocurrir desde cualquier estado cuando faltan documentos",
                "REJECTED", "Estado final, puede ocurrir desde cualquier estado anterior",
                "APPROVED", "Estado final exitoso del proceso",
                "WAITLIST", "Estado final de lista de espera"
            ));
            
            workflowInfo.put("timestamp", LocalDateTime.now());
            workflowInfo.put("success", true);
            
            return ResponseEntity.ok(workflowInfo);
            
        } catch (Exception e) {
            log.error("❌ Error generando información del flujo", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Valida múltiples transiciones en lote
     */
    @PostMapping("/validate-batch")
    public ResponseEntity<Map<String, Object>> validateBatchTransitions(
            @RequestBody Map<String, Object> batchRequest) {
        
        try {
            log.info("🔄 Validando transiciones en lote");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> transitions = 
                    (java.util.List<Map<String, Object>>) batchRequest.get("transitions");
            
            if (transitions == null || transitions.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Lista de transiciones vacía o inválida");
                errorResponse.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            int validCount = 0;
            int invalidCount = 0;
            
            for (Map<String, Object> transition : transitions) {
                try {
                    Long applicationId = Long.valueOf(transition.get("applicationId").toString());
                    String fromStatus = transition.get("fromStatus").toString();
                    String toStatus = transition.get("toStatus").toString();
                    
                    Application.ApplicationStatus from = Application.ApplicationStatus.valueOf(fromStatus.toUpperCase());
                    Application.ApplicationStatus to = Application.ApplicationStatus.valueOf(toStatus.toUpperCase());
                    
                    StateTransitionValidationService.ValidationResult result = 
                            validationService.validateTransition(applicationId, from, to);
                    
                    Map<String, Object> transitionResult = new HashMap<>();
                    transitionResult.put("applicationId", applicationId);
                    transitionResult.put("fromStatus", fromStatus);
                    transitionResult.put("toStatus", toStatus);
                    transitionResult.put("valid", result.isValid());
                    transitionResult.put("message", result.getMessage());
                    transitionResult.put("errors", result.getErrors());
                    
                    results.add(transitionResult);
                    
                    if (result.isValid()) {
                        validCount++;
                    } else {
                        invalidCount++;
                    }
                    
                } catch (Exception e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("transition", transition);
                    errorResult.put("valid", false);
                    errorResult.put("error", "Error procesando transición: " + e.getMessage());
                    results.add(errorResult);
                    invalidCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("summary", Map.of(
                "total", transitions.size(),
                "valid", validCount,
                "invalid", invalidCount
            ));
            response.put("timestamp", LocalDateTime.now());
            response.put("success", true);
            
            log.info("✅ Validación en lote completada: {} válidas, {} inválidas de {} total", 
                    validCount, invalidCount, transitions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en validación en lote", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Endpoint de testing para el sistema de validaciones
     */
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testValidationSystem() {
        try {
            log.info("🧪 Ejecutando test del sistema de validaciones");
            
            Map<String, Object> testResult = new HashMap<>();
            
            // Test 1: Validar transición básica permitida
            try {
                Set<Application.ApplicationStatus> pendingTransitions = 
                        validationService.getValidTransitions(Application.ApplicationStatus.PENDING);
                
                testResult.put("basicTransitionTest", Map.of(
                    "status", "OK",
                    "pendingValidTransitions", pendingTransitions.size(),
                    "hasUnderReview", pendingTransitions.contains(Application.ApplicationStatus.UNDER_REVIEW)
                ));
            } catch (Exception e) {
                testResult.put("basicTransitionTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test 2: Validar información del workflow
            try {
                Map<String, Object> workflowInfo = new HashMap<>();
                int totalStates = Application.ApplicationStatus.values().length;
                workflowInfo.put("totalStates", totalStates);
                
                testResult.put("workflowInfoTest", Map.of(
                    "status", "OK",
                    "totalStates", totalStates
                ));
            } catch (Exception e) {
                testResult.put("workflowInfoTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test 3: Validar estados finales
            try {
                boolean approvedIsFinal = isFinalStatus(Application.ApplicationStatus.APPROVED);
                boolean pendingIsFinal = isFinalStatus(Application.ApplicationStatus.PENDING);
                
                testResult.put("finalStatesTest", Map.of(
                    "status", "OK",
                    "approvedIsFinal", approvedIsFinal,
                    "pendingIsFinal", pendingIsFinal
                ));
            } catch (Exception e) {
                testResult.put("finalStatesTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            testResult.put("success", true);
            testResult.put("message", "Test del sistema de validaciones ejecutado");
            testResult.put("timestamp", LocalDateTime.now());
            
            log.info("✅ Test del sistema de validaciones completado");
            
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            log.error("❌ Error en test del sistema de validaciones", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Información del sistema de validaciones
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getValidationInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("systemName", "Sistema de Validación de Transiciones de Estado");
        info.put("version", "1.0.0");
        info.put("description", "Sistema completo para validar transiciones de estados en el proceso de admisión");
        
        info.put("capabilities", Map.of(
            "transitionValidation", "Validación de transiciones individuales entre estados",
            "batchValidation", "Validación en lote de múltiples transiciones",
            "workflowInfo", "Información completa del flujo de estados",
            "businessRules", "Aplicación de reglas de negocio específicas",
            "stateConsistency", "Garantiza consistencia en el flujo del proceso"
        ));
        
        info.put("endpoints", Map.of(
            "POST /api/state-validation/validate-transition", "Valida una transición específica",
            "GET /api/state-validation/valid-transitions/{status}", "Obtiene transiciones válidas desde un estado",
            "GET /api/state-validation/workflow-info", "Información completa del flujo de estados",
            "POST /api/state-validation/validate-batch", "Validación en lote de transiciones",
            "GET /api/state-validation/test", "Test del sistema de validaciones"
        ));
        
        info.put("validationTypes", Map.of(
            "basic", "Validaciones básicas de existencia y coherencia",
            "specific", "Validaciones específicas por estado destino", 
            "business", "Reglas de negocio del proceso de admisión",
            "temporal", "Validaciones basadas en tiempo y fechas",
            "workflow", "Coherencia con el flujo del proceso"
        ));
        
        info.put("businessRules", Map.of(
            "noBackwardTransitions", "No se permiten retrocesos excepto para documentos",
            "finalStateImmutable", "Estados finales no pueden modificarse",
            "timeConstraints", "Restricciones temporales entre estados",
            "prerequisiteValidation", "Validación de prerequisitos por estado",
            "documentRequirements", "Validación de documentos requeridos"
        ));
        
        info.put("timestamp", LocalDateTime.now());
        info.put("status", "ACTIVE");
        
        return ResponseEntity.ok(info);
    }

    // ================== MÉTODOS AUXILIARES ==================

    private boolean isFinalStatus(Application.ApplicationStatus status) {
        return status == Application.ApplicationStatus.APPROVED ||
               status == Application.ApplicationStatus.REJECTED ||
               status == Application.ApplicationStatus.WAITLIST ||
               status == Application.ApplicationStatus.ARCHIVED;
    }

    private String getStatusDescription(Application.ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "Aplicación enviada, pendiente de revisión inicial";
            case UNDER_REVIEW -> "En proceso de revisión por parte del equipo administrativo";
            case INTERVIEW_SCHEDULED -> "Entrevistas programadas, esperando completar el proceso";
            case EXAM_SCHEDULED -> "Exámenes académicos programados";
            case DOCUMENTS_REQUESTED -> "Se requieren documentos adicionales del postulante";
            case APPROVED -> "Aplicación aprobada - estado final";
            case REJECTED -> "Aplicación rechazada - estado final";
            case WAITLIST -> "En lista de espera - estado final";
            case ARCHIVED -> "Aplicación archivada - estado final";
        };
    }
}