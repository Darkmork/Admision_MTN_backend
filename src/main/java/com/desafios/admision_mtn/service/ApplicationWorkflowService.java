package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.*;
import com.desafios.admision_mtn.service.ApplicationService;
import com.desafios.admision_mtn.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio para manejar las transiciones automáticas de estados de aplicaciones
 * 
 * FLUJO DE ESTADOS AUTOMÁTICO:
 * 1. PENDING → UNDER_REVIEW (cuando documentos están completos)
 * 2. UNDER_REVIEW → INTERVIEW_SCHEDULED (cuando evaluaciones están asignadas)
 * 3. INTERVIEW_SCHEDULED → EXAM_SCHEDULED (cuando entrevista está completada)
 * 4. EXAM_SCHEDULED → APPROVED/REJECTED/WAITLIST (cuando evaluaciones están completas)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationWorkflowService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final EvaluationRepository evaluationRepository;
    private final InterviewRepository interviewRepository;
    private final NotificationService notificationService;
    private final StateTransitionValidationService stateValidationService;
    
    /**
     * Evalúa si una aplicación puede avanzar automáticamente al siguiente estado
     */
    public boolean evaluateAndAdvanceApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));
                
        Application.ApplicationStatus currentStatus = application.getStatus();
        Application.ApplicationStatus nextStatus = determineNextStatus(application);
        
        if (nextStatus != currentStatus && canTransitionTo(currentStatus, nextStatus)) {
            // Validar la transición con el servicio de validaciones
            StateTransitionValidationService.ValidationResult validationResult = 
                    stateValidationService.validateTransition(applicationId, currentStatus, nextStatus);
            
            if (!validationResult.isValid()) {
                log.warn("⚠️ Auto-transición bloqueada por validación: aplicación {} de {} → {}. Razón: {}", 
                        applicationId, currentStatus, nextStatus, validationResult.getMessage());
                return false;
            }
            
            log.info("🔄 Auto-transición: Aplicación {} de {} → {}", 
                    applicationId, currentStatus, nextStatus);
            
            application.setStatus(nextStatus);
            application.setUpdatedAt(LocalDateTime.now());
            applicationRepository.save(application);
            
            // Log del cambio para auditoría
            logStatusTransition(application, currentStatus, nextStatus, "AUTO_TRANSITION");
            
            // 📧 NOTIFICACIÓN AUTOMÁTICA: Enviar email al apoderado
            try {
                notificationService.notifyApplicationStatusChange(application, currentStatus, nextStatus);
                log.info("📧 Notificación automática enviada para transición {} → {}", 
                        currentStatus, nextStatus);
            } catch (Exception e) {
                log.error("❌ Error enviando notificación automática para aplicación {}", 
                        applicationId, e);
                // No fallar la transición por error de email
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Determina el próximo estado basado en las condiciones actuales
     */
    private Application.ApplicationStatus determineNextStatus(Application application) {
        Application.ApplicationStatus currentStatus = application.getStatus();
        
        return switch (currentStatus) {
            case PENDING -> {
                if (areRequiredDocumentsComplete(application.getId())) {
                    yield Application.ApplicationStatus.UNDER_REVIEW;
                }
                yield currentStatus;
            }
            
            case UNDER_REVIEW -> {
                if (areEvaluationsAssigned(application.getId())) {
                    yield Application.ApplicationStatus.INTERVIEW_SCHEDULED;
                }
                if (areMissingDocuments(application.getId())) {
                    // 📧 NOTIFICAR DOCUMENTOS FALTANTES
                    try {
                        List<String> missingDocs = applicationService.getMissingDocuments(application.getId())
                                .stream()
                                .map(Enum::name)
                                .toList();
                        notificationService.notifyMissingDocuments(application, missingDocs);
                        log.info("📄 Notificación de documentos faltantes enviada para aplicación {}", 
                                application.getId());
                    } catch (Exception e) {
                        log.error("❌ Error enviando notificación de documentos faltantes", e);
                    }
                    yield Application.ApplicationStatus.DOCUMENTS_REQUESTED;
                }
                yield currentStatus;
            }
            
            case DOCUMENTS_REQUESTED -> {
                if (areRequiredDocumentsComplete(application.getId())) {
                    yield Application.ApplicationStatus.UNDER_REVIEW;
                }
                yield currentStatus;
            }
            
            case INTERVIEW_SCHEDULED -> {
                if (isInterviewCompleted(application.getId())) {
                    yield Application.ApplicationStatus.EXAM_SCHEDULED;
                }
                yield currentStatus;
            }
            
            case EXAM_SCHEDULED -> {
                if (areAllEvaluationsComplete(application.getId())) {
                    yield determineAdmissionDecision(application.getId());
                }
                yield currentStatus;
            }
            
            default -> currentStatus;
        };
    }
    
    /**
     * Verifica si los documentos requeridos están completos
     */
    private boolean areRequiredDocumentsComplete(Long applicationId) {
        try {
            List<String> missingDocs = applicationService.getMissingDocuments(applicationId)
                    .stream()
                    .map(Enum::name)
                    .toList();
                    
            // Documentos críticos que DEBEN estar presentes
            Set<String> criticalDocuments = Set.of(
                "BIRTH_CERTIFICATE",
                "STUDENT_PHOTO"
            );
            
            boolean criticalComplete = criticalDocuments.stream()
                    .noneMatch(missingDocs::contains);
                    
            log.debug("📄 Aplicación {}: Docs críticos completos: {}, Faltantes: {}", 
                    applicationId, criticalComplete, missingDocs);
                    
            return criticalComplete;
        } catch (Exception e) {
            log.error("Error verificando documentos para aplicación {}", applicationId, e);
            return false;
        }
    }
    
    /**
     * Verifica si hay documentos faltantes
     */
    private boolean areMissingDocuments(Long applicationId) {
        try {
            List<Document.DocumentType> missing = applicationService.getMissingDocuments(applicationId);
            return !missing.isEmpty();
        } catch (Exception e) {
            log.error("Error verificando documentos faltantes para aplicación {}", applicationId, e);
            return true; // Asumir que faltan documentos en caso de error
        }
    }
    
    /**
     * Verifica si las evaluaciones están asignadas
     */
    private boolean areEvaluationsAssigned(Long applicationId) {
        List<Evaluation> evaluations = evaluationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        // Verificar que existan evaluaciones mínimas requeridas
        Set<Evaluation.EvaluationType> requiredTypes = Set.of(
            Evaluation.EvaluationType.LANGUAGE_EXAM,
            Evaluation.EvaluationType.MATHEMATICS_EXAM,
            Evaluation.EvaluationType.PSYCHOLOGICAL_INTERVIEW
        );
        
        Set<Evaluation.EvaluationType> assignedTypes = evaluations.stream()
                .filter(e -> e.getEvaluator() != null)
                .map(Evaluation::getEvaluationType)
                .collect(java.util.stream.Collectors.toSet());
                
        boolean hasRequiredEvaluations = assignedTypes.containsAll(requiredTypes);
        
        log.debug("📝 Aplicación {}: Evaluaciones asignadas: {}/{}, Requeridas: {}", 
                applicationId, assignedTypes.size(), evaluations.size(), requiredTypes);
                
        return hasRequiredEvaluations;
    }
    
    /**
     * Verifica si la entrevista está completada
     */
    private boolean isInterviewCompleted(Long applicationId) {
        Optional<Interview> interview = interviewRepository.findFirstByApplicationIdOrderByScheduledDateTimeAsc(applicationId);
        
        if (interview.isEmpty()) {
            log.debug("📅 Aplicación {}: No hay entrevista programada", applicationId);
            return false;
        }
        
        boolean completed = interview.get().getStatus() == Interview.InterviewStatus.COMPLETED;
        log.debug("📅 Aplicación {}: Entrevista completada: {}", applicationId, completed);
        
        return completed;
    }
    
    /**
     * Verifica si todas las evaluaciones están completas
     */
    private boolean areAllEvaluationsComplete(Long applicationId) {
        List<Evaluation> evaluations = evaluationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        if (evaluations.isEmpty()) {
            return false;
        }
        
        long completedEvaluations = evaluations.stream()
                .mapToLong(e -> e.getStatus() == Evaluation.EvaluationStatus.COMPLETED ? 1 : 0)
                .sum();
                
        boolean allComplete = completedEvaluations == evaluations.size() && evaluations.size() >= 3;
        
        log.debug("🎯 Aplicación {}: Evaluaciones completas: {}/{}", 
                applicationId, completedEvaluations, evaluations.size());
                
        return allComplete;
    }
    
    /**
     * Determina la decisión de admisión basada en las evaluaciones
     */
    private Application.ApplicationStatus determineAdmissionDecision(Long applicationId) {
        List<Evaluation> evaluations = evaluationRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
        
        if (evaluations.isEmpty()) {
            return Application.ApplicationStatus.EXAM_SCHEDULED;
        }
        
        // Algoritmo simple: promedio de calificaciones
        OptionalDouble averageScore = evaluations.stream()
                .filter(e -> e.getScore() != null && e.getScore() > 0)
                .mapToDouble(Evaluation::getScore)
                .average();
                
        if (averageScore.isEmpty()) {
            log.warn("⚠️ Aplicación {}: No hay calificaciones válidas", applicationId);
            return Application.ApplicationStatus.EXAM_SCHEDULED;
        }
        
        double avg = averageScore.getAsDouble();
        log.info("🎯 Aplicación {}: Promedio de evaluaciones: {:.2f}", applicationId, avg);
        
        if (avg >= 7.0) {
            return Application.ApplicationStatus.APPROVED;
        } else if (avg >= 5.5) {
            return Application.ApplicationStatus.WAITLIST;
        } else {
            return Application.ApplicationStatus.REJECTED;
        }
    }
    
    /**
     * Verifica si es válida la transición entre estados
     */
    private boolean canTransitionTo(Application.ApplicationStatus from, Application.ApplicationStatus to) {
        Map<Application.ApplicationStatus, Set<Application.ApplicationStatus>> validTransitions = Map.of(
            Application.ApplicationStatus.PENDING, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED
            ),
            Application.ApplicationStatus.DOCUMENTS_REQUESTED, Set.of(
                Application.ApplicationStatus.UNDER_REVIEW
            ),
            Application.ApplicationStatus.UNDER_REVIEW, Set.of(
                Application.ApplicationStatus.INTERVIEW_SCHEDULED,
                Application.ApplicationStatus.DOCUMENTS_REQUESTED,
                Application.ApplicationStatus.REJECTED
            ),
            Application.ApplicationStatus.INTERVIEW_SCHEDULED, Set.of(
                Application.ApplicationStatus.EXAM_SCHEDULED
            ),
            Application.ApplicationStatus.EXAM_SCHEDULED, Set.of(
                Application.ApplicationStatus.APPROVED,
                Application.ApplicationStatus.REJECTED,
                Application.ApplicationStatus.WAITLIST
            )
        );
        
        return validTransitions.getOrDefault(from, Set.of()).contains(to);
    }
    
    /**
     * Evalúa todas las aplicaciones pendientes para transiciones automáticas
     */
    public void evaluateAllApplicationsForTransition() {
        log.info("🔄 Iniciando evaluación masiva de transiciones automáticas");
        
        List<Application.ApplicationStatus> activeStatuses = List.of(
            Application.ApplicationStatus.PENDING,
            Application.ApplicationStatus.DOCUMENTS_REQUESTED,
            Application.ApplicationStatus.UNDER_REVIEW,
            Application.ApplicationStatus.INTERVIEW_SCHEDULED,
            Application.ApplicationStatus.EXAM_SCHEDULED
        );
        
        List<Application> applicationsToEvaluate = applicationRepository.findByStatusIn(activeStatuses);
        
        int transitioned = 0;
        for (Application application : applicationsToEvaluate) {
            try {
                boolean advanced = evaluateAndAdvanceApplication(application.getId());
                if (advanced) {
                    transitioned++;
                }
            } catch (Exception e) {
                log.error("Error evaluando aplicación {}", application.getId(), e);
            }
        }
        
        log.info("✅ Evaluación masiva completada: {}/{} aplicaciones avanzaron", 
                transitioned, applicationsToEvaluate.size());
    }
    
    /**
     * Log de transiciones para auditoría
     */
    private void logStatusTransition(Application application, Application.ApplicationStatus from, 
                                   Application.ApplicationStatus to, String reason) {
        log.info("📋 AUDIT: Aplicación {} | {} → {} | Razón: {} | Estudiante: {} {} | Fecha: {}", 
                application.getId(), from, to, reason,
                application.getStudent() != null ? application.getStudent().getFirstName() : "N/A",
                application.getStudent() != null ? application.getStudent().getLastName() : "N/A",
                LocalDateTime.now());
    }
}