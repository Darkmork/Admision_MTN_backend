package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.InterviewRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para validar transiciones de estado de aplicaciones
 * 
 * Garantiza que las aplicaciones solo puedan avanzar a estados válidos
 * según las reglas de negocio del proceso de admisión:
 * 
 * FLUJO PRINCIPAL:
 * PENDING → UNDER_REVIEW → INTERVIEW_SCHEDULED → EXAM_SCHEDULED → APPROVED/REJECTED/WAITLIST
 * 
 * VALIDACIONES ESPECÍFICAS:
 * - Documentos requeridos completados para cada transición
 * - Entrevistas programadas y completadas
 * - Evaluaciones académicas finalizadas
 * - Criterios de tiempo y fechas límite
 * - Reglas de negocio específicas por estado
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StateTransitionValidationService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final EvaluationRepository evaluationRepository;

    /**
     * Valida si una aplicación puede transicionar a un nuevo estado
     */
    public ValidationResult validateTransition(Long applicationId, 
                                             Application.ApplicationStatus fromStatus,
                                             Application.ApplicationStatus toStatus) {
        log.debug("🔍 Validando transición para aplicación {} de {} a {}", 
                applicationId, fromStatus, toStatus);
        
        try {
            // Validaciones básicas
            ValidationResult basicValidation = validateBasicTransition(applicationId, fromStatus, toStatus);
            if (!basicValidation.isValid()) {
                return basicValidation;
            }
            
            // Validaciones específicas por estado destino
            ValidationResult specificValidation = validateSpecificTransition(applicationId, fromStatus, toStatus);
            if (!specificValidation.isValid()) {
                return specificValidation;
            }
            
            // Validaciones de reglas de negocio
            ValidationResult businessValidation = validateBusinessRules(applicationId, fromStatus, toStatus);
            if (!businessValidation.isValid()) {
                return businessValidation;
            }
            
            log.info("✅ Transición validada correctamente: aplicación {} puede avanzar de {} a {}", 
                    applicationId, fromStatus, toStatus);
            
            return ValidationResult.valid("Transición permitida");
            
        } catch (Exception e) {
            log.error("❌ Error validando transición para aplicación {}", applicationId, e);
            return ValidationResult.invalid("Error interno validando transición: " + e.getMessage());
        }
    }

    /**
     * Validaciones básicas de la transición
     */
    private ValidationResult validateBasicTransition(Long applicationId, 
                                                   Application.ApplicationStatus fromStatus,
                                                   Application.ApplicationStatus toStatus) {
        
        // Verificar que la aplicación existe
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ValidationResult.invalid("Aplicación no encontrada: " + applicationId);
        }
        
        Application application = applicationOpt.get();
        
        // Verificar que el estado actual coincida
        if (application.getStatus() != fromStatus) {
            return ValidationResult.invalid(
                String.format("Estado actual (%s) no coincide con el estado origen (%s)", 
                            application.getStatus(), fromStatus));
        }
        
        // No permitir transiciones circulares
        if (fromStatus == toStatus) {
            return ValidationResult.invalid("No se permite transicionar al mismo estado");
        }
        
        // Validar que la transición esté permitida según el flujo
        if (!isTransitionAllowed(fromStatus, toStatus)) {
            return ValidationResult.invalid(
                String.format("Transición no permitida de %s a %s", fromStatus, toStatus));
        }
        
        return ValidationResult.valid("Validaciones básicas correctas");
    }

    /**
     * Validaciones específicas por estado destino
     */
    private ValidationResult validateSpecificTransition(Long applicationId,
                                                      Application.ApplicationStatus fromStatus,
                                                      Application.ApplicationStatus toStatus) {
        
        switch (toStatus) {
            case UNDER_REVIEW:
                return validateTransitionToUnderReview(applicationId);
                
            case INTERVIEW_SCHEDULED:
                return validateTransitionToInterviewScheduled(applicationId);
                
            case EXAM_SCHEDULED:
                return validateTransitionToExamScheduled(applicationId);
                
            case DOCUMENTS_REQUESTED:
                return validateTransitionToDocumentsRequested(applicationId);
                
            case APPROVED:
                return validateTransitionToApproved(applicationId);
                
            case REJECTED:
                return validateTransitionToRejected(applicationId);
                
            case WAITLIST:
                return validateTransitionToWaitlist(applicationId);
                
            default:
                return ValidationResult.valid("Sin validaciones específicas para " + toStatus);
        }
    }

    /**
     * Validación para transición a UNDER_REVIEW
     */
    private ValidationResult validateTransitionToUnderReview(Long applicationId) {
        // Verificar que la aplicación tenga los datos mínimos requeridos
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ValidationResult.invalid("Aplicación no encontrada");
        }
        
        Application application = applicationOpt.get();
        
        // Validar que tenga estudiante asociado
        if (application.getStudent() == null) {
            return ValidationResult.invalid("Aplicación debe tener estudiante asociado");
        }
        
        // Validar que tenga al menos un apoderado
        if (application.getApplicantUser() == null) {
            return ValidationResult.invalid("Aplicación debe tener usuario solicitante");
        }
        
        return ValidationResult.valid("Aplicación lista para revisión");
    }

    /**
     * Validación para transición a INTERVIEW_SCHEDULED
     */
    private ValidationResult validateTransitionToInterviewScheduled(Long applicationId) {
        // Verificar que la revisión inicial esté completa
        // En este punto podríamos validar que haya pasado tiempo mínimo en UNDER_REVIEW
        
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ValidationResult.invalid("Aplicación no encontrada");
        }
        
        Application application = applicationOpt.get();
        
        // Verificar que haya pasado tiempo mínimo en revisión (ejemplo: 1 día)
        if (application.getUpdatedAt() != null) {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            if (application.getUpdatedAt().isAfter(oneDayAgo)) {
                return ValidationResult.invalid("Aplicación debe estar en revisión por al menos 24 horas");
            }
        }
        
        return ValidationResult.valid("Aplicación lista para programar entrevista");
    }

    /**
     * Validación para transición a EXAM_SCHEDULED
     */
    private ValidationResult validateTransitionToExamScheduled(Long applicationId) {
        // Verificar que las entrevistas estén completadas
        List<Interview> interviews = interviewRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        if (interviews.isEmpty()) {
            return ValidationResult.invalid("No se pueden programar exámenes sin entrevistas completadas");
        }
        
        // Verificar que al menos una entrevista esté completada con resultado positivo
        boolean hasCompletedPositiveInterview = interviews.stream()
                .anyMatch(interview -> 
                    interview.getStatus() == Interview.InterviewStatus.COMPLETED &&
                    interview.getResult() == Interview.InterviewResult.POSITIVE
                );
        
        if (!hasCompletedPositiveInterview) {
            return ValidationResult.invalid("Debe tener al menos una entrevista completada con resultado positivo");
        }
        
        return ValidationResult.valid("Aplicación lista para programar exámenes");
    }

    /**
     * Validación para transición a DOCUMENTS_REQUESTED
     */
    private ValidationResult validateTransitionToDocumentsRequested(Long applicationId) {
        // Esta transición puede ocurrir desde varios estados cuando faltan documentos
        return ValidationResult.valid("Transición válida para solicitar documentos");
    }

    /**
     * Validación para transición a APPROVED
     */
    private ValidationResult validateTransitionToApproved(Long applicationId) {
        // Verificar que las evaluaciones estén completadas
        List<Evaluation> evaluations = evaluationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        if (evaluations.isEmpty()) {
            return ValidationResult.invalid("No se puede aprobar sin evaluaciones completadas");
        }
        
        // Verificar que todas las evaluaciones estén completadas
        boolean allEvaluationsCompleted = evaluations.stream()
                .allMatch(evaluation -> evaluation.getStatus() == Evaluation.EvaluationStatus.COMPLETED);
        
        if (!allEvaluationsCompleted) {
            return ValidationResult.invalid("Todas las evaluaciones deben estar completadas para aprobar");
        }
        
        // Verificar que las evaluaciones tengan resultados positivos
        boolean hasPositiveEvaluations = evaluations.stream()
                .anyMatch(evaluation -> 
                    evaluation.getFinalRecommendation() != null && 
                    evaluation.getFinalRecommendation()
                );
        
        if (!hasPositiveEvaluations) {
            return ValidationResult.invalid("Debe tener al menos una evaluación con recomendación positiva");
        }
        
        return ValidationResult.valid("Aplicación cumple criterios para aprobación");
    }

    /**
     * Validación para transición a REJECTED
     */
    private ValidationResult validateTransitionToRejected(Long applicationId) {
        // Las aplicaciones pueden ser rechazadas en cualquier momento si hay motivo válido
        // Aquí podríamos agregar validaciones específicas si hay reglas de negocio particulares
        return ValidationResult.valid("Transición válida para rechazo");
    }

    /**
     * Validación para transición a WAITLIST
     */
    private ValidationResult validateTransitionToWaitlist(Long applicationId) {
        // Similar a aprobación pero con criterios de lista de espera
        List<Evaluation> evaluations = evaluationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        if (evaluations.isEmpty()) {
            return ValidationResult.invalid("No se puede poner en lista de espera sin evaluaciones");
        }
        
        boolean hasCompletedEvaluations = evaluations.stream()
                .anyMatch(evaluation -> evaluation.getStatus() == Evaluation.EvaluationStatus.COMPLETED);
        
        if (!hasCompletedEvaluations) {
            return ValidationResult.invalid("Debe tener al menos una evaluación completada para lista de espera");
        }
        
        return ValidationResult.valid("Aplicación cumple criterios para lista de espera");
    }

    /**
     * Validaciones de reglas de negocio específicas
     */
    private ValidationResult validateBusinessRules(Long applicationId,
                                                 Application.ApplicationStatus fromStatus,
                                                 Application.ApplicationStatus toStatus) {
        
        Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
        if (applicationOpt.isEmpty()) {
            return ValidationResult.invalid("Aplicación no encontrada");
        }
        
        Application application = applicationOpt.get();
        
        // Regla: No permitir cambios después de 30 días desde creación (ejemplo)
        if (application.getCreatedAt() != null) {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            if (application.getCreatedAt().isBefore(thirtyDaysAgo)) {
                return ValidationResult.invalid("No se pueden modificar aplicaciones de más de 30 días");
            }
        }
        
        // Regla: No permitir retrocesos excepto a DOCUMENTS_REQUESTED
        if (isBackwardTransition(fromStatus, toStatus) && toStatus != Application.ApplicationStatus.DOCUMENTS_REQUESTED) {
            return ValidationResult.invalid("No se permiten transiciones hacia atrás excepto para solicitar documentos");
        }
        
        // Regla: Estados finales no pueden cambiar
        if (isFinalStatus(fromStatus)) {
            return ValidationResult.invalid("No se puede modificar una aplicación en estado final: " + fromStatus);
        }
        
        return ValidationResult.valid("Reglas de negocio validadas correctamente");
    }

    /**
     * Verifica si una transición está permitida según el flujo del proceso
     */
    private boolean isTransitionAllowed(Application.ApplicationStatus from, Application.ApplicationStatus to) {
        // Definir las transiciones permitidas
        Map<Application.ApplicationStatus, Set<Application.ApplicationStatus>> allowedTransitions = Map.of(
            Application.ApplicationStatus.PENDING, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.UNDER_REVIEW, Set.of(
                Application.ApplicationStatus.INTERVIEW_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.INTERVIEW_SCHEDULED, Set.of(
                Application.ApplicationStatus.EXAM_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED,
                Application.ApplicationStatus.WAITLIST
            ),
            Application.ApplicationStatus.EXAM_SCHEDULED, Set.of(
                Application.ApplicationStatus.APPROVED,
                Application.ApplicationStatus.REJECTED,
                Application.ApplicationStatus.WAITLIST,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED
            ),
            Application.ApplicationStatus.DOCUMENTS_REQUESTED, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.PENDING,
                Application.ApplicationStatus.REJECTED
            )
        );
        
        Set<Application.ApplicationStatus> allowedTargets = allowedTransitions.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }

    /**
     * Verifica si una transición es hacia atrás en el proceso
     */
    private boolean isBackwardTransition(Application.ApplicationStatus from, Application.ApplicationStatus to) {
        // Definir el orden del proceso (números más altos = más avanzado)
        Map<Application.ApplicationStatus, Integer> statusOrder = Map.of(
            Application.ApplicationStatus.PENDING, 1,
            Application.ApplicationStatus.UNDER_REVIEW, 2,
            Application.ApplicationStatus.INTERVIEW_SCHEDULED, 3,
            Application.ApplicationStatus.EXAM_SCHEDULED, 4,
            Application.ApplicationStatus.APPROVED, 5,
            Application.ApplicationStatus.REJECTED, 5,
            Application.ApplicationStatus.WAITLIST, 5,
            Application.ApplicationStatus.DOCUMENTS_REQUESTED, 0 // Especial: puede ocurrir en cualquier momento
        );
        
        Integer fromOrder = statusOrder.get(from);
        Integer toOrder = statusOrder.get(to);
        
        if (fromOrder == null || toOrder == null) {
            return false;
        }
        
        // DOCUMENTS_REQUESTED es especial, no se considera retroceso
        if (to == Application.ApplicationStatus.DOCUMENTS_REQUESTED) {
            return false;
        }
        
        return fromOrder > toOrder;
    }

    /**
     * Verifica si un estado es final (no se puede cambiar)
     */
    private boolean isFinalStatus(Application.ApplicationStatus status) {
        return status == Application.ApplicationStatus.APPROVED ||
               status == Application.ApplicationStatus.REJECTED ||
               status == Application.ApplicationStatus.WAITLIST;
    }

    /**
     * Obtiene todas las transiciones válidas desde un estado dado
     */
    public Set<Application.ApplicationStatus> getValidTransitions(Application.ApplicationStatus fromStatus) {
        Map<Application.ApplicationStatus, Set<Application.ApplicationStatus>> allowedTransitions = Map.of(
            Application.ApplicationStatus.PENDING, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.UNDER_REVIEW, Set.of(
                Application.ApplicationStatus.INTERVIEW_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.INTERVIEW_SCHEDULED, Set.of(
                Application.ApplicationStatus.EXAM_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED,
                Application.ApplicationStatus.WAITLIST
            ),
            Application.ApplicationStatus.EXAM_SCHEDULED, Set.of(
                Application.ApplicationStatus.APPROVED,
                Application.ApplicationStatus.REJECTED,
                Application.ApplicationStatus.WAITLIST,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED
            ),
            Application.ApplicationStatus.DOCUMENTS_REQUESTED, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.PENDING,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.APPROVED, Set.of(), // Estado final
            Application.ApplicationStatus.REJECTED, Set.of(), // Estado final
            Application.ApplicationStatus.WAITLIST, Set.of()  // Estado final
        );
        
        return allowedTransitions.getOrDefault(fromStatus, Set.of());
    }

    /**
     * Clase para el resultado de validación
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> errors;
        private final Map<String, Object> metadata;
        
        private ValidationResult(boolean valid, String message, List<String> errors, Map<String, Object> metadata) {
            this.valid = valid;
            this.message = message;
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        }
        
        public static ValidationResult valid(String message) {
            return new ValidationResult(true, message, null, null);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message, List.of(message), null);
        }
        
        public static ValidationResult invalid(String message, List<String> errors) {
            return new ValidationResult(false, message, errors, null);
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
        
        public ValidationResult withMetadata(String key, Object value) {
            Map<String, Object> newMetadata = new HashMap<>(this.metadata);
            newMetadata.put(key, value);
            return new ValidationResult(this.valid, this.message, this.errors, newMetadata);
        }
    }
}