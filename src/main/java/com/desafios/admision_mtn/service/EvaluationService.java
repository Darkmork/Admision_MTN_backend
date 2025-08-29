package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    /**
     * Asigna evaluaciones automáticamente para una aplicación
     */
    public List<Evaluation> assignEvaluationsToApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        List<Evaluation> evaluations = new ArrayList<>();

        // Tipos de evaluación requeridos
        List<Evaluation.EvaluationType> requiredEvaluations = Arrays.asList(
            Evaluation.EvaluationType.LANGUAGE_EXAM,
            Evaluation.EvaluationType.MATHEMATICS_EXAM,
            Evaluation.EvaluationType.ENGLISH_EXAM,
            Evaluation.EvaluationType.CYCLE_DIRECTOR_REPORT,
            Evaluation.EvaluationType.CYCLE_DIRECTOR_INTERVIEW
        );

        for (Evaluation.EvaluationType type : requiredEvaluations) {
            // Verificar si ya existe una evaluación de este tipo
            Optional<Evaluation> existing = evaluationRepository
                    .findByApplicationIdAndEvaluationType(applicationId, type);
            
            if (existing.isEmpty()) {
                // Buscar evaluador disponible para este tipo
                User evaluator = findAvailableEvaluator(type);
                if (evaluator != null) {
                    Evaluation evaluation = createEvaluation(application, evaluator, type);
                    evaluations.add(evaluationRepository.save(evaluation));
                    log.info("Evaluación {} asignada a {} para aplicación {}", 
                            type, evaluator.getEmail(), applicationId);
                }
            }
        }

        return evaluations;
    }

    /**
     * Asigna una evaluación específica a un evaluador
     */
    public Evaluation assignEvaluationToEvaluator(Long applicationId, Evaluation.EvaluationType evaluationType, Long evaluatorId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
        
        User evaluator = userRepository.findById(evaluatorId)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado"));

        // Verificar si ya existe una evaluación de este tipo
        Optional<Evaluation> existing = evaluationRepository
                .findByApplicationIdAndEvaluationType(applicationId, evaluationType);
        
        if (existing.isPresent()) {
            // Actualizar evaluador existente
            Evaluation evaluation = existing.get();
            evaluation.setEvaluator(evaluator);
            evaluation.setStatus(Evaluation.EvaluationStatus.PENDING);
            return evaluationRepository.save(evaluation);
        } else {
            // Crear nueva evaluación
            Evaluation evaluation = createEvaluation(application, evaluator, evaluationType);
            return evaluationRepository.save(evaluation);
        }
    }

    /**
     * Actualiza una evaluación con los resultados
     */
    public Evaluation updateEvaluation(Long evaluationId, Map<String, Object> evaluationData) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));

        // Actualizar campos básicos
        if (evaluationData.containsKey("score")) {
            evaluation.setScore((Integer) evaluationData.get("score"));
        }
        if (evaluationData.containsKey("grade")) {
            evaluation.setGrade((String) evaluationData.get("grade"));
        }
        if (evaluationData.containsKey("observations")) {
            evaluation.setObservations((String) evaluationData.get("observations"));
        }
        if (evaluationData.containsKey("strengths")) {
            evaluation.setStrengths((String) evaluationData.get("strengths"));
        }
        if (evaluationData.containsKey("areasForImprovement")) {
            evaluation.setAreasForImprovement((String) evaluationData.get("areasForImprovement"));
        }
        if (evaluationData.containsKey("recommendations")) {
            evaluation.setRecommendations((String) evaluationData.get("recommendations"));
        }

        // Campos específicos para entrevistas psicológicas
        if (evaluationData.containsKey("socialSkillsAssessment")) {
            evaluation.setSocialSkillsAssessment((String) evaluationData.get("socialSkillsAssessment"));
        }
        if (evaluationData.containsKey("emotionalMaturity")) {
            evaluation.setEmotionalMaturity((String) evaluationData.get("emotionalMaturity"));
        }
        if (evaluationData.containsKey("motivationAssessment")) {
            evaluation.setMotivationAssessment((String) evaluationData.get("motivationAssessment"));
        }
        if (evaluationData.containsKey("familySupportAssessment")) {
            evaluation.setFamilySupportAssessment((String) evaluationData.get("familySupportAssessment"));
        }

        // Campos para Director de Ciclo
        if (evaluationData.containsKey("academicReadiness")) {
            evaluation.setAcademicReadiness((String) evaluationData.get("academicReadiness"));
        }
        if (evaluationData.containsKey("behavioralAssessment")) {
            evaluation.setBehavioralAssessment((String) evaluationData.get("behavioralAssessment"));
        }
        if (evaluationData.containsKey("integrationPotential")) {
            evaluation.setIntegrationPotential((String) evaluationData.get("integrationPotential"));
        }
        if (evaluationData.containsKey("finalRecommendation")) {
            evaluation.setFinalRecommendation((Boolean) evaluationData.get("finalRecommendation"));
        }

        // Actualizar estado y fechas
        if (evaluationData.containsKey("status")) {
            String statusStr = (String) evaluationData.get("status");
            evaluation.setStatus(Evaluation.EvaluationStatus.valueOf(statusStr));
        }
        
        if (evaluation.getStatus() == Evaluation.EvaluationStatus.COMPLETED) {
            evaluation.setCompletionDate(LocalDateTime.now());
        }

        evaluation.setEvaluationDate(LocalDateTime.now());

        return evaluationRepository.save(evaluation);
    }

    /**
     * Obtiene todas las evaluaciones de una aplicación
     */
    public List<Evaluation> getEvaluationsByApplication(Long applicationId) {
        return evaluationRepository.findByApplicationIdWithEvaluator(applicationId);
    }

    /**
     * Obtiene las evaluaciones asignadas a un evaluador
     */
    public List<Evaluation> getEvaluationsByEvaluator(String evaluatorEmail) {
        return evaluationRepository.findByEvaluatorEmailOrderByCreatedAtDesc(evaluatorEmail);
    }

    /**
     * Obtiene las evaluaciones pendientes de un evaluador
     */
    public List<Evaluation> getPendingEvaluationsByEvaluator(Long evaluatorId) {
        return evaluationRepository.findPendingEvaluationsByEvaluatorId(evaluatorId);
    }

    /**
     * Obtiene el progreso de evaluaciones de una aplicación
     */
    public Map<String, Object> getEvaluationProgress(Long applicationId) {
        List<Evaluation> evaluations = getEvaluationsByApplication(applicationId);
        
        long totalEvaluations = evaluations.size();
        long completedEvaluations = evaluations.stream()
                .mapToLong(e -> e.getStatus() == Evaluation.EvaluationStatus.COMPLETED ? 1 : 0)
                .sum();
        
        double completionPercentage = totalEvaluations > 0 ? 
                (double) completedEvaluations / totalEvaluations * 100 : 0;

        Map<String, Object> progress = new HashMap<>();
        progress.put("applicationId", applicationId);
        progress.put("totalEvaluations", totalEvaluations);
        progress.put("completedEvaluations", completedEvaluations);
        progress.put("completionPercentage", Math.round(completionPercentage));
        progress.put("isComplete", totalEvaluations > 0 && completedEvaluations == totalEvaluations);
        
        return progress;
    }

    /**
     * Obtiene todos los evaluadores por rol
     */
    public List<User> getEvaluatorsByRole(User.UserRole role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }

    /**
     * Obtiene una evaluación por su ID
     */
    public Evaluation getEvaluationById(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));
    }

    /**
     * Asigna evaluaciones en lote a múltiples aplicaciones
     */
    public Map<String, Object> assignBulkEvaluations(List<Long> applicationIds) {
        Map<String, Object> result = new HashMap<>();
        List<String> successfulAssignments = new ArrayList<>();
        List<String> failedAssignments = new ArrayList<>();
        
        for (Long applicationId : applicationIds) {
            try {
                List<Evaluation> evaluations = assignEvaluationsToApplication(applicationId);
                successfulAssignments.add("Aplicación " + applicationId + ": " + evaluations.size() + " evaluaciones asignadas");
                log.info("Bulk assignment successful for application {}: {} evaluations assigned", 
                        applicationId, evaluations.size());
            } catch (Exception e) {
                failedAssignments.add("Aplicación " + applicationId + ": " + e.getMessage());
                log.error("Bulk assignment failed for application {}", applicationId, e);
            }
        }
        
        result.put("totalApplications", applicationIds.size());
        result.put("successCount", successfulAssignments.size());
        result.put("failureCount", failedAssignments.size());
        result.put("successful", successfulAssignments);
        result.put("failed", failedAssignments);
        result.put("isComplete", failedAssignments.isEmpty());
        
        return result;
    }

    /**
     * Obtiene estadísticas generales de evaluaciones
     */
    public Map<String, Object> getEvaluationStatistics() {
        List<Evaluation> allEvaluations = evaluationRepository.findAll();
        
        Map<String, Long> statusCounts = allEvaluations.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().toString(), 
                    Collectors.counting()
                ));
        
        Map<String, Long> typeCounts = allEvaluations.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getEvaluationType().toString(),
                    Collectors.counting()
                ));
        
        // Calcular promedios por tipo
        Map<String, Double> averageScores = new HashMap<>();
        for (Evaluation.EvaluationType type : Evaluation.EvaluationType.values()) {
            List<Evaluation> evaluationsOfType = allEvaluations.stream()
                    .filter(e -> e.getEvaluationType() == type && e.getScore() != null)
                    .toList();
            
            if (!evaluationsOfType.isEmpty()) {
                double average = evaluationsOfType.stream()
                        .mapToInt(Evaluation::getScore)
                        .average()
                        .orElse(0.0);
                averageScores.put(type.toString(), Math.round(average * 100.0) / 100.0);
            }
        }
        
        // Evaluadores más activos
        Map<String, Long> evaluatorActivity = allEvaluations.stream()
                .filter(e -> e.getEvaluator() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getEvaluator().getFirstName() + " " + e.getEvaluator().getLastName(),
                    Collectors.counting()
                ));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvaluations", allEvaluations.size());
        stats.put("statusBreakdown", statusCounts);
        stats.put("typeBreakdown", typeCounts);
        stats.put("averageScoresByType", averageScores);
        stats.put("evaluatorActivity", evaluatorActivity);
        stats.put("completionRate", calculateCompletionRate(allEvaluations));
        
        return stats;
    }

    /**
     * Reasigna una evaluación a otro evaluador
     */
    public Evaluation reassignEvaluation(Long evaluationId, Long newEvaluatorId) {
        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada"));
        
        User newEvaluator = userRepository.findById(newEvaluatorId)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado"));
        
        // Verificar que el evaluador tenga el rol correcto
        User.UserRole requiredRole = getRequiredRoleForEvaluationType(evaluation.getEvaluationType());
        if (newEvaluator.getRole() != requiredRole) {
            throw new RuntimeException("El evaluador no tiene el rol requerido para este tipo de evaluación");
        }
        
        evaluation.setEvaluator(newEvaluator);
        evaluation.setStatus(Evaluation.EvaluationStatus.PENDING);
        evaluation.setEvaluationDate(null);
        evaluation.setCompletionDate(null);
        
        log.info("Evaluation {} reassigned from {} to {}", 
                evaluationId, 
                evaluation.getEvaluator() != null ? evaluation.getEvaluator().getEmail() : "unassigned",
                newEvaluator.getEmail());
        
        return evaluationRepository.save(evaluation);
    }

    /**
     * Obtiene evaluaciones por aplicación con información completa
     */
    public List<Map<String, Object>> getDetailedEvaluationsByApplication(Long applicationId) {
        List<Evaluation> evaluations = evaluationRepository.findByApplicationIdWithEvaluator(applicationId);
        
        return evaluations.stream().map(evaluation -> {
            Map<String, Object> evalData = new HashMap<>();
            evalData.put("id", evaluation.getId());
            evalData.put("evaluationType", evaluation.getEvaluationType());
            evalData.put("status", evaluation.getStatus());
            evalData.put("score", evaluation.getScore());
            evalData.put("grade", evaluation.getGrade());
            evalData.put("evaluationDate", evaluation.getEvaluationDate());
            evalData.put("completionDate", evaluation.getCompletionDate());
            evalData.put("createdAt", evaluation.getCreatedAt());
            
            if (evaluation.getEvaluator() != null) {
                Map<String, Object> evaluatorData = new HashMap<>();
                evaluatorData.put("id", evaluation.getEvaluator().getId());
                evaluatorData.put("firstName", evaluation.getEvaluator().getFirstName());
                evaluatorData.put("lastName", evaluation.getEvaluator().getLastName());
                evaluatorData.put("email", evaluation.getEvaluator().getEmail());
                evaluatorData.put("role", evaluation.getEvaluator().getRole());
                evalData.put("evaluator", evaluatorData);
            }
            
            return evalData;
        }).toList();
    }

    private double calculateCompletionRate(List<Evaluation> evaluations) {
        if (evaluations.isEmpty()) return 0.0;
        
        long completedCount = evaluations.stream()
                .mapToLong(e -> e.getStatus() == Evaluation.EvaluationStatus.COMPLETED ? 1 : 0)
                .sum();
        
        return Math.round(((double) completedCount / evaluations.size()) * 100.0 * 100.0) / 100.0;
    }

    private Evaluation createEvaluation(Application application, User evaluator, Evaluation.EvaluationType type) {
        Evaluation evaluation = new Evaluation();
        evaluation.setApplication(application);
        evaluation.setEvaluator(evaluator);
        evaluation.setEvaluationType(type);
        evaluation.setStatus(Evaluation.EvaluationStatus.PENDING);
        return evaluation;
    }

    private User findAvailableEvaluator(Evaluation.EvaluationType evaluationType) {
        User.UserRole requiredRole = getRequiredRoleForEvaluationType(evaluationType);
        List<User> availableEvaluators = userRepository.findByRoleAndActiveTrue(requiredRole);
        
        // Por ahora, retorna el primer evaluador disponible
        // En el futuro se puede implementar lógica más sofisticada de balanceo de carga
        return availableEvaluators.isEmpty() ? null : availableEvaluators.get(0);
    }

    private User.UserRole getRequiredRoleForEvaluationType(Evaluation.EvaluationType evaluationType) {
        return switch (evaluationType) {
            case LANGUAGE_EXAM -> User.UserRole.TEACHER;
            case MATHEMATICS_EXAM -> User.UserRole.TEACHER;
            case ENGLISH_EXAM -> User.UserRole.TEACHER;
            case CYCLE_DIRECTOR_REPORT, CYCLE_DIRECTOR_INTERVIEW -> User.UserRole.CYCLE_DIRECTOR;
            case PSYCHOLOGICAL_INTERVIEW -> User.UserRole.PSYCHOLOGIST;
        };
    }

    /**
     * Obtiene todas las evaluaciones del sistema con paginación
     */
    public List<Evaluation> getAllEvaluations() {
        log.info("📊 Obteniendo todas las evaluaciones del sistema");
        List<Evaluation> evaluations = evaluationRepository.findAll();
        log.info("✅ Se encontraron {} evaluaciones", evaluations.size());
        return evaluations;
    }

    /**
     * Obtiene evaluaciones con información completa (application, student, evaluator)
     */
    public List<Evaluation> getAllEvaluationsWithDetails() {
        log.info("📊 Obteniendo evaluaciones con detalles completos");
        // Obtener todas las evaluaciones - las relaciones se cargan automáticamente por JPA
        List<Evaluation> evaluations = evaluationRepository.findAll();
        
        // Forzar la carga de las relaciones para evitar LazyInitializationException
        evaluations.forEach(evaluation -> {
            if (evaluation.getApplication() != null && evaluation.getApplication().getStudent() != null) {
                evaluation.getApplication().getStudent().getFirstName(); // Trigger lazy loading
            }
            if (evaluation.getEvaluator() != null) {
                evaluation.getEvaluator().getFirstName(); // Trigger lazy loading
            }
        });
        
        log.info("✅ Se encontraron {} evaluaciones con detalles", evaluations.size());
        return evaluations;
    }
    
    // ========== MÉTODOS PARA API UNIFICADA ==========
    
    /**
     * Obtiene evaluaciones pendientes del sistema
     */
    public List<Evaluation> getPendingEvaluations() {
        try {
            return evaluationRepository.findByStatus(Evaluation.EvaluationStatus.PENDING);
        } catch (Exception e) {
            log.error("Error getting pending evaluations", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene evaluaciones completadas hoy
     */
    public List<Evaluation> getCompletedEvaluationsToday() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            return evaluationRepository.findByStatusAndCompletionDateBetween(
                Evaluation.EvaluationStatus.COMPLETED, startOfDay, endOfDay);
        } catch (Exception e) {
            log.error("Error getting completed evaluations today", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene evaluaciones por evaluador específico
     */
    public List<Evaluation> getEvaluationsByEvaluator(Long evaluatorId) {
        try {
            User evaluator = userRepository.findById(evaluatorId).orElse(null);
            if (evaluator != null) {
                return evaluationRepository.findByEvaluator(evaluator);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting evaluations by evaluator", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtiene todas las evaluaciones con paginación
     */
    public org.springframework.data.domain.Page<Evaluation> getAllEvaluations(org.springframework.data.domain.Pageable pageable) {
        try {
            return evaluationRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error getting evaluations with pagination", e);
            return org.springframework.data.domain.Page.empty(pageable);
        }
    }
}