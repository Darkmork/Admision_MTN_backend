package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class EvaluationController {

    private final EvaluationService evaluationService;

    // Endpoints para administradores
    
    @PostMapping("/assign/{applicationId}")
    public ResponseEntity<List<Evaluation>> assignEvaluationsToApplication(@PathVariable Long applicationId) {
        try {
            List<Evaluation> evaluations = evaluationService.assignEvaluationsToApplication(applicationId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error asignando evaluaciones a aplicación {}", applicationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assign/{applicationId}/{evaluationType}/{evaluatorId}")
    public ResponseEntity<Evaluation> assignSpecificEvaluation(
            @PathVariable Long applicationId,
            @PathVariable String evaluationType,
            @PathVariable Long evaluatorId) {
        try {
            Evaluation.EvaluationType type = Evaluation.EvaluationType.valueOf(evaluationType);
            Evaluation evaluation = evaluationService.assignEvaluationToEvaluator(applicationId, type, evaluatorId);
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            log.error("Error asignando evaluación específica", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Map<String, Object>>> getEvaluationsByApplication(@PathVariable Long applicationId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByApplication(applicationId);
            List<Map<String, Object>> response = evaluations.stream()
                    .map(this::createEvaluationResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo evaluaciones para aplicación {}", applicationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/application/{applicationId}/progress")
    public ResponseEntity<Map<String, Object>> getEvaluationProgress(@PathVariable Long applicationId) {
        try {
            Map<String, Object> progress = evaluationService.getEvaluationProgress(applicationId);
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            log.error("Error obteniendo progreso de evaluaciones para aplicación {}", applicationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/evaluators/{role}")
    public ResponseEntity<List<Map<String, Object>>> getEvaluatorsByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role);
            List<User> evaluators = evaluationService.getEvaluatorsByRole(userRole);
            List<Map<String, Object>> response = evaluators.stream()
                    .map(this::createUserResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo evaluadores por rol {}", role, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoints públicos para desarrollo
    @GetMapping("/public/evaluators/{role}")
    public ResponseEntity<List<Map<String, Object>>> getEvaluatorsByRolePublic(@PathVariable String role) {
        try {
            // Mock data para desarrollo sin autenticación
            List<Map<String, Object>> mockEvaluators = List.of(
                Map.of(
                    "id", 1,
                    "firstName", "María",
                    "lastName", "González",
                    "email", "maria.gonzalez@mtn.cl",
                    "role", "TEACHER_MATHEMATICS"
                ),
                Map.of(
                    "id", 2,
                    "firstName", "Pedro",
                    "lastName", "Rodríguez",
                    "email", "pedro.rodriguez@mtn.cl",
                    "role", "TEACHER_MATHEMATICS"
                )
            );
            return ResponseEntity.ok(mockEvaluators);
        } catch (Exception e) {
            log.error("Error obteniendo evaluadores públicos por rol {}", role, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoint público para verificar evaluaciones de un profesor específico
    @GetMapping("/public/professor/{email}/evaluations")
    public ResponseEntity<List<Map<String, Object>>> getProfessorEvaluationsPublic(@PathVariable String email) {
        try {
            log.info("Obteniendo evaluaciones públicas para profesor: {}", email);
            
            List<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluator(email);
            List<Map<String, Object>> response = evaluations.stream()
                    .map(this::createEvaluationWithApplicationResponse)
                    .toList();
            
            log.info("Evaluaciones encontradas para {}: {}", email, response.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo evaluaciones públicas para profesor {}", email, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/public/assign/{applicationId}")
    public ResponseEntity<Map<String, Object>> assignEvaluationsToApplicationPublic(@PathVariable Long applicationId) {
        try {
            log.info("Asignando evaluaciones públicas para aplicación {}", applicationId);
            
            // Usar el servicio real para asignar evaluaciones
            List<Evaluation> evaluations = evaluationService.assignEvaluationsToApplication(applicationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("applicationId", applicationId);
            response.put("evaluationsCreated", evaluations.size());
            response.put("message", "Evaluaciones asignadas correctamente");
            
            // Convertir evaluaciones a formato de respuesta
            List<Map<String, Object>> evaluationResponses = evaluations.stream()
                .map(eval -> {
                    Map<String, Object> evalData = new HashMap<>();
                    evalData.put("id", eval.getId());
                    evalData.put("type", eval.getEvaluationType());
                    evalData.put("status", eval.getStatus());
                    
                    if (eval.getEvaluator() != null) {
                        evalData.put("evaluatorId", eval.getEvaluator().getId());
                        evalData.put("evaluatorEmail", eval.getEvaluator().getEmail());
                        evalData.put("evaluatorName", eval.getEvaluator().getFirstName() + " " + eval.getEvaluator().getLastName());
                    }
                    
                    return evalData;
                })
                .toList();
            
            response.put("evaluations", evaluationResponses);
            
            log.info("Evaluaciones asignadas exitosamente: {}", evaluationResponses.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error asignando evaluaciones públicas a aplicación {}", applicationId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error asignando evaluaciones");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/public/assign/bulk")
    public ResponseEntity<Map<String, Object>> assignBulkEvaluationsPublic(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Number> applicationIds = (List<Number>) request.get("applicationIds");
            
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("totalApplications", applicationIds.size());
            mockResponse.put("successCount", applicationIds.size());
            mockResponse.put("failureCount", 0);
            mockResponse.put("successful", applicationIds.stream().map(id -> "APP-" + id).toList());
            mockResponse.put("failed", List.of());
            mockResponse.put("isComplete", true);
            
            return ResponseEntity.ok(mockResponse);
        } catch (Exception e) {
            log.error("Error asignando evaluaciones masivas públicas", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public/statistics")
    public ResponseEntity<Map<String, Object>> getEvaluationStatisticsPublic() {
        try {
            Map<String, Object> mockStats = new HashMap<>();
            mockStats.put("totalEvaluations", 45);
            mockStats.put("statusBreakdown", Map.of(
                "PENDING", 12,
                "IN_PROGRESS", 8,
                "COMPLETED", 20,
                "REVIEWED", 5
            ));
            mockStats.put("typeBreakdown", Map.of(
                "LANGUAGE_EXAM", 15,
                "MATHEMATICS_EXAM", 15,
                "PSYCHOLOGICAL_INTERVIEW", 15
            ));
            mockStats.put("averageScoresByType", Map.of(
                "LANGUAGE_EXAM", 85.5,
                "MATHEMATICS_EXAM", 82.3,
                "PSYCHOLOGICAL_INTERVIEW", 88.7
            ));
            mockStats.put("evaluatorActivity", Map.of(
                "María González", 15,
                "Pedro Rodríguez", 12,
                "Ana López", 18
            ));
            mockStats.put("completionRate", 75.5);
            
            return ResponseEntity.ok(mockStats);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas públicas de evaluaciones", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Endpoints para evaluadores

    @GetMapping("/my-evaluations")
    public ResponseEntity<List<Map<String, Object>>> getMyEvaluations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String evaluatorEmail = authentication.getName();
            
            List<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluator(evaluatorEmail);
            List<Map<String, Object>> response = evaluations.stream()
                    .map(this::createEvaluationWithApplicationResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo evaluaciones del evaluador", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/my-pending")
    public ResponseEntity<List<Map<String, Object>>> getMyPendingEvaluations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String evaluatorEmail = authentication.getName();
            
            // Necesitamos obtener el ID del usuario primero
            // Por simplicidad, obtenemos todas las evaluaciones y filtramos pendientes
            List<Evaluation> evaluations = evaluationService.getEvaluationsByEvaluator(evaluatorEmail);
            List<Map<String, Object>> response = evaluations.stream()
                    .filter(e -> e.getStatus() == Evaluation.EvaluationStatus.PENDING || 
                               e.getStatus() == Evaluation.EvaluationStatus.IN_PROGRESS)
                    .map(this::createEvaluationWithApplicationResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo evaluaciones pendientes del evaluador", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{evaluationId}")
    public ResponseEntity<Map<String, Object>> updateEvaluation(
            @PathVariable Long evaluationId,
            @RequestBody Map<String, Object> evaluationData) {
        try {
            Evaluation updated = evaluationService.updateEvaluation(evaluationId, evaluationData);
            Map<String, Object> response = createEvaluationResponse(updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error actualizando evaluación {}", evaluationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{evaluationId}")
    public ResponseEntity<Map<String, Object>> getEvaluationById(@PathVariable Long evaluationId) {
        try {
            Evaluation evaluation = evaluationService.getEvaluationById(evaluationId);
            Map<String, Object> response = createEvaluationWithApplicationResponse(evaluation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo evaluación {}", evaluationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/assign/bulk")
    public ResponseEntity<Map<String, Object>> assignBulkEvaluations(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> applicationIds = (List<Integer>) request.get("applicationIds");
            List<Long> longApplicationIds = applicationIds.stream().map(Integer::longValue).toList();
            
            Map<String, Object> result = evaluationService.assignBulkEvaluations(longApplicationIds);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error en asignación masiva de evaluaciones", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{evaluationId}/reassign/{newEvaluatorId}")
    public ResponseEntity<Map<String, Object>> reassignEvaluation(
            @PathVariable Long evaluationId, 
            @PathVariable Long newEvaluatorId) {
        try {
            Evaluation evaluation = evaluationService.reassignEvaluation(evaluationId, newEvaluatorId);
            Map<String, Object> response = createEvaluationResponse(evaluation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error reasignando evaluación {} a evaluador {}", evaluationId, newEvaluatorId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEvaluationStatistics() {
        try {
            Map<String, Object> stats = evaluationService.getEvaluationStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de evaluaciones", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/application/{applicationId}/detailed")
    public ResponseEntity<List<Map<String, Object>>> getDetailedEvaluationsByApplication(@PathVariable Long applicationId) {
        try {
            List<Map<String, Object>> evaluations = evaluationService.getDetailedEvaluationsByApplication(applicationId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error obteniendo evaluaciones detalladas para aplicación {}", applicationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Métodos auxiliares para crear respuestas

    private Map<String, Object> createEvaluationResponse(Evaluation evaluation) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", evaluation.getId());
        response.put("evaluationType", evaluation.getEvaluationType());
        response.put("status", evaluation.getStatus());
        response.put("score", evaluation.getScore());
        response.put("grade", evaluation.getGrade());
        response.put("observations", evaluation.getObservations());
        response.put("strengths", evaluation.getStrengths());
        response.put("areasForImprovement", evaluation.getAreasForImprovement());
        response.put("recommendations", evaluation.getRecommendations());
        response.put("socialSkillsAssessment", evaluation.getSocialSkillsAssessment());
        response.put("emotionalMaturity", evaluation.getEmotionalMaturity());
        response.put("motivationAssessment", evaluation.getMotivationAssessment());
        response.put("familySupportAssessment", evaluation.getFamilySupportAssessment());
        response.put("academicReadiness", evaluation.getAcademicReadiness());
        response.put("behavioralAssessment", evaluation.getBehavioralAssessment());
        response.put("integrationPotential", evaluation.getIntegrationPotential());
        response.put("finalRecommendation", evaluation.getFinalRecommendation());
        response.put("evaluationDate", evaluation.getEvaluationDate());
        response.put("completionDate", evaluation.getCompletionDate());
        response.put("createdAt", evaluation.getCreatedAt());
        response.put("updatedAt", evaluation.getUpdatedAt());
        
        if (evaluation.getEvaluator() != null) {
            response.put("evaluator", createUserResponse(evaluation.getEvaluator()));
        }
        
        return response;
    }

    private Map<String, Object> createEvaluationWithApplicationResponse(Evaluation evaluation) {
        Map<String, Object> response = createEvaluationResponse(evaluation);
        
        if (evaluation.getApplication() != null) {
            Map<String, Object> applicationInfo = new HashMap<>();
            applicationInfo.put("id", evaluation.getApplication().getId());
            applicationInfo.put("status", evaluation.getApplication().getStatus());
            applicationInfo.put("submissionDate", evaluation.getApplication().getSubmissionDate());
            
            if (evaluation.getApplication().getStudent() != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("firstName", evaluation.getApplication().getStudent().getFirstName());
                studentInfo.put("lastName", evaluation.getApplication().getStudent().getLastName());
                studentInfo.put("rut", evaluation.getApplication().getStudent().getRut());
                studentInfo.put("gradeApplied", evaluation.getApplication().getStudent().getGradeApplied());
                // ✅ CAMPOS CRÍTICOS AGREGADOS para auto-rellenar el informe
                studentInfo.put("birthDate", evaluation.getApplication().getStudent().getBirthDate());
                studentInfo.put("currentSchool", evaluation.getApplication().getStudent().getCurrentSchool());
                applicationInfo.put("student", studentInfo);
            }
            
            response.put("application", applicationInfo);
        }
        
        return response;
    }

    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("active", user.getActive());
        return response;
    }
}