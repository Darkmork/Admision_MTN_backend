package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.*;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Interview.*;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.InterviewRepository;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InterviewNotificationService notificationService;

    // CRUD básico
    public InterviewResponse createInterview(CreateInterviewRequest request) {
        log.info("Creando nueva entrevista para aplicación ID: {}", request.getApplicationId());
        
        // Validar que la aplicación existe
        Application application = applicationRepository.findById(request.getApplicationId())
            .orElseThrow(() -> new EntityNotFoundException("Aplicación no encontrada con ID: " + request.getApplicationId()));
        
        // Validar que el entrevistador existe
        User interviewer = userRepository.findById(request.getInterviewerId())
            .orElseThrow(() -> new EntityNotFoundException("Entrevistador no encontrado con ID: " + request.getInterviewerId()));
        
        // Validar disponibilidad del entrevistador
        validateInterviewerAvailability(request.getInterviewerId(), request.getScheduledDate(), request.getScheduledTime());
        
        // Validar enlace virtual si es necesario
        if (request.getMode() == InterviewMode.VIRTUAL && 
            (request.getVirtualMeetingLink() == null || request.getVirtualMeetingLink().trim().isEmpty())) {
            throw new IllegalArgumentException("El enlace de reunión virtual es obligatorio para entrevistas virtuales");
        }
        
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewer(interviewer);
        interview.setType(request.getType());
        interview.setMode(request.getMode());
        interview.setScheduledDate(request.getScheduledDate());
        interview.setScheduledTime(request.getScheduledTime());
        interview.setDuration(request.getDuration());
        interview.setLocation(request.getLocation());
        interview.setVirtualMeetingLink(request.getVirtualMeetingLink());
        interview.setNotes(request.getNotes());
        interview.setPreparation(request.getPreparation());
        interview.setStatus(InterviewStatus.SCHEDULED);
        
        Interview savedInterview = interviewRepository.save(interview);
        log.info("Entrevista creada exitosamente con ID: {}", savedInterview.getId());
        
        // Enviar notificación por email a la familia
        notificationService.sendInterviewScheduledNotification(savedInterview);
        
        return InterviewResponse.from(savedInterview);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long id) {
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        return InterviewResponse.from(interview);
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> getAllInterviews(Pageable pageable) {
        return interviewRepository.findAll(pageable)
            .map(InterviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> searchInterviews(String searchTerm, Pageable pageable) {
        return interviewRepository.findBySearchTerm(searchTerm, pageable)
            .map(InterviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InterviewResponse> findWithFilters(
            InterviewStatus status,
            InterviewType type,
            InterviewMode mode,
            Long interviewerId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        return interviewRepository.findWithFilters(
                status, type, mode, interviewerId, startDate, endDate, pageable)
            .map(InterviewResponse::from);
    }

    public InterviewResponse updateInterview(Long id, UpdateInterviewRequest request) {
        log.info("Actualizando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        if (!interview.canBeEdited()) {
            throw new IllegalStateException("La entrevista no puede ser editada en su estado actual: " + interview.getStatus());
        }
        
        // Actualizar campos si están presentes
        if (request.getInterviewerId() != null) {
            User interviewer = userRepository.findById(request.getInterviewerId())
                .orElseThrow(() -> new EntityNotFoundException("Entrevistador no encontrado con ID: " + request.getInterviewerId()));
            interview.setInterviewer(interviewer);
        }
        
        if (request.getType() != null) {
            interview.setType(request.getType());
        }
        
        if (request.getMode() != null) {
            interview.setMode(request.getMode());
        }
        
        if (request.getScheduledDate() != null && request.getScheduledTime() != null) {
            // Validar nueva disponibilidad si cambió el horario
            Long interviewerId = request.getInterviewerId() != null ? 
                request.getInterviewerId() : interview.getInterviewer().getId();
            validateInterviewerAvailability(interviewerId, request.getScheduledDate(), request.getScheduledTime(), id);
            
            interview.setScheduledDate(request.getScheduledDate());
            interview.setScheduledTime(request.getScheduledTime());
        }
        
        if (request.getDuration() != null) {
            interview.setDuration(request.getDuration());
        }
        
        if (request.getLocation() != null) {
            interview.setLocation(request.getLocation());
        }
        
        if (request.getVirtualMeetingLink() != null) {
            interview.setVirtualMeetingLink(request.getVirtualMeetingLink());
        }
        
        if (request.getNotes() != null) {
            interview.setNotes(request.getNotes());
        }
        
        if (request.getPreparation() != null) {
            interview.setPreparation(request.getPreparation());
        }
        
        // Validar enlace virtual si es necesario
        if (interview.getMode() == InterviewMode.VIRTUAL && 
            (interview.getVirtualMeetingLink() == null || interview.getVirtualMeetingLink().trim().isEmpty())) {
            throw new IllegalArgumentException("El enlace de reunión virtual es obligatorio para entrevistas virtuales");
        }
        
        Interview updatedInterview = interviewRepository.save(interview);
        log.info("Entrevista actualizada exitosamente con ID: {}", id);
        
        return InterviewResponse.from(updatedInterview);
    }

    public void deleteInterview(Long id) {
        log.info("Eliminando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new IllegalStateException("No se puede eliminar una entrevista completada");
        }
        
        interviewRepository.delete(interview);
        log.info("Entrevista eliminada exitosamente con ID: {}", id);
    }

    // Operaciones de estado
    public InterviewResponse confirmInterview(Long id) {
        log.info("Confirmando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        interview.confirm();
        Interview confirmedInterview = interviewRepository.save(interview);
        
        // Enviar notificación de confirmación a la familia
        notificationService.sendInterviewConfirmedNotification(confirmedInterview);
        
        log.info("Entrevista confirmada exitosamente con ID: {}", id);
        return InterviewResponse.from(confirmedInterview);
    }

    public InterviewResponse startInterview(Long id) {
        log.info("Iniciando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        interview.start();
        Interview startedInterview = interviewRepository.save(interview);
        
        log.info("Entrevista iniciada exitosamente con ID: {}", id);
        return InterviewResponse.from(startedInterview);
    }

    public InterviewResponse completeInterview(Long id, CompleteInterviewRequest request) {
        log.info("Completando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        interview.complete(
            request.getResult(),
            request.getScore(),
            request.getRecommendations(),
            request.getFollowUpRequired(),
            request.getFollowUpNotes()
        );
        
        Interview completedInterview = interviewRepository.save(interview);
        
        log.info("Entrevista completada exitosamente con ID: {}", id);
        return InterviewResponse.from(completedInterview);
    }

    public InterviewResponse cancelInterview(Long id) {
        log.info("Cancelando entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        interview.cancel();
        Interview cancelledInterview = interviewRepository.save(interview);
        
        // Enviar notificación de cancelación a la familia
        notificationService.sendInterviewCancelledNotification(cancelledInterview, null);
        
        log.info("Entrevista cancelada exitosamente con ID: {}", id);
        return InterviewResponse.from(cancelledInterview);
    }

    public InterviewResponse rescheduleInterview(Long id, LocalDate newDate, LocalTime newTime) {
        log.info("Reprogramando entrevista con ID: {} para {} a las {}", id, newDate, newTime);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        // Validar nueva disponibilidad
        validateInterviewerAvailability(interview.getInterviewer().getId(), newDate, newTime, id);
        
        interview.reschedule(newDate, newTime);
        Interview rescheduledInterview = interviewRepository.save(interview);
        
        // Enviar notificación de reprogramación a la familia
        notificationService.sendInterviewRescheduledNotification(rescheduledInterview);
        
        log.info("Entrevista reprogramada exitosamente con ID: {}", id);
        return InterviewResponse.from(rescheduledInterview);
    }

    public InterviewResponse markAsNoShow(Long id) {
        log.info("Marcando como 'no asistió' entrevista con ID: {}", id);
        
        Interview interview = interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
        
        interview.markAsNoShow();
        Interview noShowInterview = interviewRepository.save(interview);
        
        log.info("Entrevista marcada como 'no asistió' exitosamente con ID: {}", id);
        return InterviewResponse.from(noShowInterview);
    }

    // Consultas especiales
    @Transactional(readOnly = true)
    public List<InterviewResponse> getTodaysInterviews() {
        return interviewRepository.findTodaysInterviews().stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getUpcomingInterviews() {
        return interviewRepository.findUpcomingInterviews().stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getOverdueInterviews() {
        return interviewRepository.findOverdueInterviews().stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsRequiringFollowUp() {
        return interviewRepository.findRequiringFollowUp().stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByInterviewer(Long interviewerId) {
        return interviewRepository.findByInterviewerId(interviewerId).stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsByDateRange(LocalDate startDate, LocalDate endDate) {
        return interviewRepository.findByScheduledDateBetween(startDate, endDate).stream()
            .map(InterviewResponse::from)
            .collect(Collectors.toList());
    }

    // Estadísticas
    @Transactional(readOnly = true)
    public InterviewStatsResponse getInterviewStatistics() {
        log.info("Generando estadísticas de entrevistas");
        
        // Métricas principales
        long totalInterviews = interviewRepository.count();
        long scheduledInterviews = interviewRepository.countByStatus(InterviewStatus.SCHEDULED);
        long completedInterviews = interviewRepository.countByStatus(InterviewStatus.COMPLETED);
        long cancelledInterviews = interviewRepository.countByStatus(InterviewStatus.CANCELLED);
        long noShowInterviews = interviewRepository.countByStatus(InterviewStatus.NO_SHOW);
        long pendingInterviews = scheduledInterviews + 
            interviewRepository.countByStatus(InterviewStatus.CONFIRMED) +
            interviewRepository.countByStatus(InterviewStatus.IN_PROGRESS);
        
        // Métricas de resultados
        long positiveResults = interviewRepository.countByResult(InterviewResult.POSITIVE);
        long neutralResults = interviewRepository.countByResult(InterviewResult.NEUTRAL);
        long negativeResults = interviewRepository.countByResult(InterviewResult.NEGATIVE);
        long pendingReviewResults = interviewRepository.countByResult(InterviewResult.PENDING_REVIEW);
        long requiresFollowUpResults = interviewRepository.countByResult(InterviewResult.REQUIRES_FOLLOW_UP);
        
        // Promedios y tasas
        double averageScore = interviewRepository.findAverageScore().orElse(0.0);
        double completionRate = totalInterviews > 0 ? (double) completedInterviews / totalInterviews * 100 : 0;
        double cancellationRate = totalInterviews > 0 ? (double) (cancelledInterviews + noShowInterviews) / totalInterviews * 100 : 0;
        double successRate = completedInterviews > 0 ? (double) positiveResults / completedInterviews * 100 : 0;
        
        // Distribuciones
        Map<String, Long> statusDistribution = convertToMap(interviewRepository.findStatusDistribution());
        Map<String, Long> typeDistribution = convertToMap(interviewRepository.findTypeDistribution());
        Map<String, Long> modeDistribution = convertToMap(interviewRepository.findModeDistribution());
        
        // Resultado distribución calculada manualmente
        Map<String, Long> resultDistribution = new HashMap<>();
        resultDistribution.put("POSITIVE", positiveResults);
        resultDistribution.put("NEUTRAL", neutralResults);
        resultDistribution.put("NEGATIVE", negativeResults);
        resultDistribution.put("PENDING_REVIEW", pendingReviewResults);
        resultDistribution.put("REQUIRES_FOLLOW_UP", requiresFollowUpResults);
        
        // Tendencias mensuales
        Map<String, Long> monthlyTrends = convertToMap(interviewRepository.findMonthlyStatistics());
        
        // Métricas adicionales
        long followUpRequired = interviewRepository.findRequiringFollowUp().size();
        long upcomingInterviews = interviewRepository.findUpcomingInterviews().size();
        long overdueInterviews = interviewRepository.findOverdueInterviews().size();
        
        return InterviewStatsResponse.builder()
            .totalInterviews(totalInterviews)
            .scheduledInterviews(scheduledInterviews)
            .completedInterviews(completedInterviews)
            .cancelledInterviews(cancelledInterviews)
            .noShowInterviews(noShowInterviews)
            .pendingInterviews(pendingInterviews)
            .positiveResults(positiveResults)
            .neutralResults(neutralResults)
            .negativeResults(negativeResults)
            .pendingReviewResults(pendingReviewResults)
            .requiresFollowUpResults(requiresFollowUpResults)
            .averageScore(averageScore)
            .completionRate(completionRate)
            .cancellationRate(cancellationRate)
            .successRate(successRate)
            .statusDistribution(statusDistribution)
            .typeDistribution(typeDistribution)
            .modeDistribution(modeDistribution)
            .resultDistribution(resultDistribution)
            .monthlyTrends(monthlyTrends)
            .followUpRequired(followUpRequired)
            .upcomingInterviews(upcomingInterviews)
            .overdueInterviews(overdueInterviews)
            .build();
    }

    // Método para obtener entidad completa (para notificaciones)
    @Transactional(readOnly = true)
    public Interview getInterviewEntityById(Long id) {
        return interviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Entrevista no encontrada con ID: " + id));
    }

    // Métodos de validación
    private void validateInterviewerAvailability(Long interviewerId, LocalDate date, LocalTime time) {
        validateInterviewerAvailability(interviewerId, date, time, null);
    }

    private void validateInterviewerAvailability(Long interviewerId, LocalDate date, LocalTime time, Long excludeInterviewId) {
        long conflicts = interviewRepository.countConflictingInterviews(interviewerId, date, time);
        
        // Si estamos actualizando una entrevista existente, restar 1 si hay conflicto con la misma entrevista
        if (excludeInterviewId != null && conflicts > 0) {
            Interview existingInterview = interviewRepository.findById(excludeInterviewId).orElse(null);
            if (existingInterview != null && 
                existingInterview.getScheduledDate().equals(date) && 
                existingInterview.getScheduledTime().equals(time)) {
                conflicts--;
            }
        }
        
        if (conflicts > 0) {
            throw new IllegalArgumentException("El entrevistador ya tiene una entrevista programada en esa fecha y hora");
        }
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
            .collect(Collectors.toMap(
                result -> result[0].toString(),
                result -> ((Number) result[1]).longValue()
            ));
    }
}