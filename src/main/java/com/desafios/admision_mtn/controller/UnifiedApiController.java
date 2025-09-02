package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.*;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * 🎯 CONTROLADOR UNIFICADO - Consolidación de múltiples endpoints
 * 
 * Este controlador reemplaza múltiples endpoints específicos con endpoints raíz
 * que aceptan parámetros dinámicos para diferentes tipos de consultas.
 * 
 * BENEFICIOS:
 * - Reduce de 317 endpoints a ~50 endpoints principales
 * - Elimina duplicación de lógica
 * - Simplifica mantenimiento
 * - Mejora performance con menos HTTP calls
 */
@RestController
@RequestMapping("/api/unified")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "http://localhost:5173", 
    "http://localhost:5174", 
    "http://localhost:5175", 
    "http://localhost:5176"
})
public class UnifiedApiController {

    private final InterviewService interviewService;
    private final ApplicationService applicationService;
    private final EvaluationService evaluationService;
    private final UserService userService;
    private final InterviewerScheduleService interviewerScheduleService;

    /**
     * 🎯 ENDPOINT RAÍZ PARA ENTREVISTAS
     * Reemplaza ~25 endpoints específicos del InterviewController
     * 
     * Ejemplos de uso:
     * GET /api/unified/interviews?include=statistics
     * GET /api/unified/interviews?status=SCHEDULED&interviewer=123
     * GET /api/unified/interviews?dateFrom=2024-01-01&special=overdue,upcoming
     * GET /api/unified/interviews?application=456&include=details,history
     */
    @GetMapping("/interviews")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<?> getInterviews(
            @RequestParam(required = false) String include, // statistics, details, history
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long interviewer,
            @RequestParam(required = false) Long application,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String special, // overdue,upcoming,today,requiring-followup
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Incluir estadísticas si se solicita
            if (include != null && include.contains("statistics")) {
                response.put("statistics", interviewService.getInterviewStatistics());
            }
            
            // Consultas especiales
            if (special != null) {
                if (special.contains("today")) {
                    response.put("todaysInterviews", interviewService.getTodaysInterviews());
                }
                if (special.contains("upcoming")) {
                    response.put("upcomingInterviews", interviewService.getUpcomingInterviews());
                }
                if (special.contains("overdue")) {
                    response.put("overdueInterviews", interviewService.getOverdueInterviews());
                }
                if (special.contains("requiring-followup")) {
                    response.put("followupRequired", interviewService.getInterviewsRequiringFollowUp());
                }
            }
            
            // Consulta principal con filtros
            if (interviewer != null) {
                response.put("interviews", interviewService.getInterviewsByInterviewer(interviewer));
            } else if (application != null) {
                response.put("interviews", interviewService.getInterviewsByApplication(application));
            } else if (dateFrom != null && dateTo != null) {
                response.put("interviews", interviewService.getInterviewsByDateRange(
                    LocalDate.parse(dateFrom), LocalDate.parse(dateTo)));
            } else {
                // Consulta paginada general con filtros dinámicos
                response.put("interviews", interviewService.findWithFilters(
                    status != null ? Interview.InterviewStatus.valueOf(status) : null,
                    null, interviewer, 
                    dateFrom != null ? LocalDate.parse(dateFrom) : null,
                    dateTo != null ? LocalDate.parse(dateTo) : null,
                    createPageable(page, size, sortBy, sortDir)
                ));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in unified interviews endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener entrevistas", "message", e.getMessage()));
        }
    }

    /**
     * 🎯 ENDPOINT RAÍZ PARA APLICACIONES
     * Reemplaza ~18 endpoints del ApplicationController
     * 
     * Ejemplos:
     * GET /api/unified/applications?status=PENDING&include=statistics,students
     * GET /api/unified/applications?special=recent,requiring-documents
     */
    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> getApplications(
            @RequestParam(required = false) String include,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String special,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Incluir estadísticas
            if (include != null && include.contains("statistics")) {
                response.put("statistics", applicationService.getApplicationStatistics());
            }
            
            // Consultas especiales
            if (special != null) {
                if (special.contains("recent")) {
                    response.put("recentApplications", applicationService.getRecentApplications());
                }
                if (special.contains("requiring-documents")) {
                    response.put("requireDocuments", applicationService.getApplicationsRequiringDocuments());
                }
            }
            
            // Consulta principal
            if (search != null && !search.trim().isEmpty()) {
                response.put("applications", applicationService.searchApplications(search, 
                    createPageable(page, size, "submissionDate", "desc")));
            } else {
                response.put("applications", applicationService.getAllApplicationsWithFilters(
                    status != null ? Application.ApplicationStatus.valueOf(status) : null,
                    createPageable(page, size, "submissionDate", "desc")));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in unified applications endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener aplicaciones", "message", e.getMessage()));
        }
    }

    /**
     * 🎯 ENDPOINT RAÍZ PARA EVALUACIONES
     * Reemplaza ~20 endpoints del EvaluationController
     */
    @GetMapping("/evaluations")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR') or hasRole('TEACHER') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<?> getEvaluations(
            @RequestParam(required = false) String include,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long evaluator,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String special,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Estadísticas
            if (include != null && include.contains("statistics")) {
                response.put("statistics", evaluationService.getEvaluationStatistics());
            }
            
            // Consultas por evaluador
            if (evaluator != null) {
                response.put("evaluations", evaluationService.getEvaluationsByEvaluator(evaluator));
            }
            
            // Consultas especiales
            if (special != null) {
                if (special.contains("pending")) {
                    response.put("pendingEvaluations", evaluationService.getPendingEvaluations());
                }
                if (special.contains("completed-today")) {
                    response.put("completedToday", evaluationService.getCompletedEvaluationsToday());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in unified evaluations endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener evaluaciones", "message", e.getMessage()));
        }
    }

    /**
     * 🎯 ENDPOINT RAÍZ PARA DISPONIBILIDAD
     * Consolida los endpoints del InterviewerScheduleController + InterviewAvailabilityController
     */
    @GetMapping("/availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> getAvailability(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String interviewType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long interviewer,
            @RequestParam(required = false) String include) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Disponibilidad para fecha/hora específica
            if (date != null && time != null) {
                if (interviewType != null) {
                    response.put("availableByType", interviewService.getAvailableInterviewersByType(
                        Interview.InterviewType.valueOf(interviewType), 
                        LocalDate.parse(date), 
                        LocalTime.parse(time)));
                } else {
                    response.put("available", interviewService.getAvailableInterviewers(
                        LocalDate.parse(date), LocalTime.parse(time)));
                }
            }
            
            // Resumen de disponibilidad
            if (date != null && include != null && include.contains("summary")) {
                response.put("summary", interviewerScheduleService.getAvailabilitySummary(date));
            }
            
            // Horarios de un entrevistador
            if (interviewer != null) {
                if (year != null) {
                    response.put("schedules", interviewerScheduleService.getInterviewerSchedulesByYear(interviewer, year));
                } else {
                    response.put("schedules", interviewerScheduleService.getInterviewerActiveSchedules(interviewer));
                }
            }
            
            // Estadísticas de carga
            if (include != null && include.contains("workload") && year != null) {
                response.put("workloadStats", interviewerScheduleService.getWorkloadStatistics(year));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in unified availability endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener disponibilidad", "message", e.getMessage()));
        }
    }

    /**
     * 🎯 ENDPOINT DE QUERIES COMPLEJAS
     * Permite consultas tipo GraphQL para obtener múltiples recursos relacionados
     */
    @PostMapping("/query")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> complexQuery(@RequestBody Map<String, Object> queryRequest) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            @SuppressWarnings("unchecked")
            List<String> entities = (List<String>) queryRequest.get("entities");
            @SuppressWarnings("unchecked")
            Map<String, Object> filters = (Map<String, Object>) queryRequest.get("filters");
            
            if (entities != null) {
                for (String entity : entities) {
                    switch (entity) {
                        case "interviews":
                            response.put("interviews", interviewService.getAllInterviews(
                                createPageable(0, 100, "scheduledDate", "desc")));
                            break;
                        case "applications":
                            response.put("applications", applicationService.getAllApplications());
                            break;
                        case "evaluations":
                            response.put("evaluations", evaluationService.getAllEvaluations());
                            break;
                    }
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in complex query endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al ejecutar consulta compleja", "message", e.getMessage()));
        }
    }

    /**
     * 🎯 DASHBOARD UNIFICADO
     * Un solo endpoint que retorna toda la información necesaria para el dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> getDashboardData(@RequestParam(required = false) String modules) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            List<String> requestedModules = modules != null 
                ? Arrays.asList(modules.split(",")) 
                : Arrays.asList("interviews", "applications", "evaluations");
            
            if (requestedModules.contains("interviews")) {
                dashboard.put("interviewStats", interviewService.getInterviewStatistics());
                dashboard.put("todaysInterviews", interviewService.getTodaysInterviews());
                dashboard.put("upcomingInterviews", interviewService.getUpcomingInterviews());
            }
            
            if (requestedModules.contains("applications")) {
                dashboard.put("applicationStats", applicationService.getApplicationStatistics());
                dashboard.put("recentApplications", applicationService.getRecentApplications());
            }
            
            if (requestedModules.contains("evaluations")) {
                dashboard.put("evaluationStats", evaluationService.getEvaluationStatistics());
                dashboard.put("pendingEvaluations", evaluationService.getPendingEvaluations());
            }
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error in unified dashboard endpoint: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener datos del dashboard", "message", e.getMessage()));
        }
    }

    // Método auxiliar para crear Pageable
    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equals(sortDir) 
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        
        return PageRequest.of(page, size, sort);
    }
}