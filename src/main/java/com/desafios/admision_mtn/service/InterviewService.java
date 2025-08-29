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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InterviewNotificationService notificationService;
    private final PersonalizedEmailService personalizedEmailService;
    private final TemplatedInterviewNotificationService templatedNotificationService;
    private final InterviewerScheduleService interviewerScheduleService;
    private final EntityManager entityManager;

    // CRUD básico
    public InterviewResponse createInterview(CreateInterviewRequest request) {
        log.info("Creando nueva entrevista para aplicación ID: {}", request.getApplicationId());
        
        // Validar que la aplicación existe usando SQL directo
        Long applicationCount = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM applications WHERE id = ?")
            .setParameter(1, request.getApplicationId())
            .getSingleResult();
            
        if (applicationCount == 0) {
            throw new EntityNotFoundException("Aplicación no encontrada con ID: " + request.getApplicationId());
        }
        
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
        
        // Crear la entrevista usando SQL directo para evitar problemas con entidades complejas
        String insertSql = "INSERT INTO interviews (application_id, interviewer_id, interview_type, scheduled_date, duration_minutes, location, status, notes, created_at) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW()) RETURNING id";
        
        // Combinar fecha y hora en un LocalDateTime
        LocalDateTime scheduledDateTime = LocalDateTime.of(request.getScheduledDate(), request.getScheduledTime());
        
        // Usar jdbcTemplate para ejecutar la inserción
        Long interviewId = ((Number) entityManager.createNativeQuery(insertSql)
            .setParameter(1, request.getApplicationId())
            .setParameter(2, request.getInterviewerId())
            .setParameter(3, request.getType().toString())
            .setParameter(4, Timestamp.valueOf(scheduledDateTime))
            .setParameter(5, request.getDuration())
            .setParameter(6, request.getLocation())
            .setParameter(7, "SCHEDULED")
            .setParameter(8, request.getNotes())
            .getSingleResult()).longValue();
        
        log.info("Entrevista creada exitosamente con ID: {}", interviewId);
        
        // Obtener la entrevista recién creada
        Interview savedInterview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new RuntimeException("Error al recuperar la entrevista creada"));
        
        // Enviar notificación por email a la familia usando templates
        templatedNotificationService.sendInterviewAssignmentNotification(savedInterview);
        
        // Verificar si esta aplicación tiene las 3 entrevistas programadas
        List<Interview> allInterviews = interviewRepository.findByApplicationId(request.getApplicationId());
        if (hasAllRequiredInterviews(allInterviews)) {
            log.info("La aplicación {} tiene las 3 entrevistas requeridas, enviando notificación de set completo", request.getApplicationId());
            templatedNotificationService.sendCompleteInterviewSetNotification(request.getApplicationId(), allInterviews);
        }
        
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
        log.info("=== DEBUGGING getAllInterviews START ===");
        
        try {
            // Intentar obtener entrevistas con SQL nativo para bypass JPA mapping issues
            @SuppressWarnings("unchecked")
            List<Object[]> nativeResults = entityManager.createNativeQuery(
                "SELECT i.id, i.application_id, i.interviewer_id, i.interview_type, i.scheduled_date, " +
                "i.duration_minutes, i.location, i.status, i.notes, i.created_at, " +
                "s.first_name || ' ' || s.paternal_last_name as student_name, " +
                "u.first_name || ' ' || u.last_name as interviewer_name " +
                "FROM interviews i " +
                "LEFT JOIN applications a ON i.application_id = a.id " +
                "LEFT JOIN students s ON a.student_id = s.id " +
                "LEFT JOIN users u ON i.interviewer_id = u.id " +
                "ORDER BY i.id LIMIT ?")
                .setParameter(1, pageable.getPageSize())
                .getResultList();
                
            log.info("=== Native query returned {} results ===", nativeResults.size());
            
            // Crear responses manualmente desde los resultados nativos
            List<InterviewResponse> responses = nativeResults.stream()
                .map(this::mapNativeResultToResponse)
                .collect(Collectors.toList());
            
            // Obtener conteo total
            Long totalCount = ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM interviews")
                .getSingleResult()).longValue();
                
            log.info("=== Total count: {}, Returning {} responses ===", totalCount, responses.size());
            
            // Crear Page manualmente
            return new org.springframework.data.domain.PageImpl<>(responses, pageable, totalCount);
            
        } catch (Exception e) {
            log.error("Error in getAllInterviews: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving interviews", e);
        }
    }
    
    private InterviewResponse mapNativeResultToResponse(Object[] row) {
        InterviewResponse response = new InterviewResponse();
        response.setId(((Number) row[0]).longValue());
        response.setApplicationId(((Number) row[1]).longValue());
        response.setInterviewerId(((Number) row[2]).longValue());
        response.setType(InterviewType.valueOf((String) row[3]));
        response.setStatus(InterviewStatus.valueOf((String) row[7]));
        response.setStudentName((String) row[10]);
        response.setInterviewerName((String) row[11]);
        response.setLocation((String) row[6]);
        response.setNotes((String) row[8]);
        
        // Parsear la fecha/hora
        if (row[4] != null) {
            java.sql.Timestamp timestamp = (java.sql.Timestamp) row[4];
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            response.setScheduledDate(dateTime.toLocalDate());
            response.setScheduledTime(dateTime.toLocalTime());
        }
        
        if (row[5] != null) {
            response.setDuration((Integer) row[5]);
        }
        
        // Valores por defecto
        response.setMode(InterviewMode.IN_PERSON);
        response.setParentNames(""); // Temporalmente vacío
        response.setGradeApplied(""); // Temporalmente vacío
        response.setFollowUpRequired(false);
        
        return response;
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
            Long interviewerId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        
        return interviewRepository.findWithFilters(
                status, type, interviewerId, startDate, endDate, pageable)
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
        
        // Verificar si todas las entrevistas requeridas están completadas
        checkAndUpdateApplicationStatusIfAllInterviewsCompleted(completedInterview.getApplication().getId());
        
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
        // followUpRequired es transient, retornar lista vacía
        return new ArrayList<>();
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

    /**
     * Obtener entrevistadores disponibles para una fecha y hora específica
     * Este método integra el sistema de horarios configurados por los entrevistadores
     */
    @Transactional(readOnly = true)
    public List<User> getAvailableInterviewers(LocalDate date, LocalTime time) {
        log.info("Obteniendo entrevistadores disponibles para fecha: {} hora: {}", date, time);
        
        // Usar el servicio de horarios para obtener entrevistadores con horarios configurados
        List<User> availableInterviewers = interviewerScheduleService.findAvailableInterviewers(date, time);
        
        // Filtrar entrevistadores que ya tienen entrevistas programadas en esa fecha y hora
        List<User> finalAvailableList = availableInterviewers.stream()
            .filter(interviewer -> {
                long conflicts = interviewRepository.countConflictingInterviews(
                    interviewer.getId(), LocalDateTime.of(date, time));
                return conflicts == 0;
            })
            .collect(Collectors.toList());
        
        log.info("Entrevistadores disponibles encontrados: {} (de {} con horarios configurados)", 
                finalAvailableList.size(), availableInterviewers.size());
        
        return finalAvailableList;
    }

    /**
     * Obtener entrevistadores por tipo de entrevista y disponibilidad
     * Filtra automáticamente según los horarios configurados
     */
    @Transactional(readOnly = true) 
    public List<User> getAvailableInterviewersByType(InterviewType interviewType, LocalDate date, LocalTime time) {
        log.info("Obteniendo entrevistadores disponibles para tipo: {} fecha: {} hora: {}", 
                interviewType, date, time);
        
        List<User> availableInterviewers = getAvailableInterviewers(date, time);
        
        // Filtrar por tipo de entrevista según roles
        List<User> filteredByType = availableInterviewers.stream()
            .filter(interviewer -> isInterviewerValidForType(interviewer, interviewType))
            .collect(Collectors.toList());
        
        log.info("Entrevistadores válidos para tipo {}: {}", interviewType, filteredByType.size());
        
        return filteredByType;
    }

    /**
     * Verificar si un entrevistador puede realizar un tipo específico de entrevista
     */
    private boolean isInterviewerValidForType(User interviewer, InterviewType interviewType) {
        if (interviewer.getRole() == null) {
            return false;
        }
        
        String roleName = interviewer.getRole().name();
        
        switch (interviewType) {
            case PSYCHOLOGICAL:
                return "PSYCHOLOGIST".equals(roleName) || "ADMIN".equals(roleName);
            case FAMILY:
            case INDIVIDUAL:
                return "CYCLE_DIRECTOR".equals(roleName) || 
                       "COORDINATOR".equals(roleName) || 
                       "ADMIN".equals(roleName);
            default:
                return false;
        }
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
        
        // Métricas de resultados - result es transient, no hay datos en DB
        long positiveResults = 0L;
        long neutralResults = 0L;
        long negativeResults = 0L;
        long pendingReviewResults = 0L;
        long requiresFollowUpResults = 0L;
        
        // Promedios y tasas
        double averageScore = 0.0; // score es transient, no hay datos en DB
        double completionRate = totalInterviews > 0 ? (double) completedInterviews / totalInterviews * 100 : 0;
        double cancellationRate = totalInterviews > 0 ? (double) (cancelledInterviews + noShowInterviews) / totalInterviews * 100 : 0;
        double successRate = completedInterviews > 0 ? (double) positiveResults / completedInterviews * 100 : 0;
        
        // Distribuciones
        Map<String, Long> statusDistribution = convertToMap(interviewRepository.findStatusDistribution());
        Map<String, Long> typeDistribution = convertToMap(interviewRepository.findTypeDistribution());
        // Mode distribution - mode es transient y siempre IN_PERSON
        Map<String, Long> modeDistribution = new HashMap<>();
        modeDistribution.put("IN_PERSON", totalInterviews);
        modeDistribution.put("VIRTUAL", 0L);
        modeDistribution.put("HYBRID", 0L);
        
        // Resultado distribución calculada manualmente
        Map<String, Long> resultDistribution = new HashMap<>();
        resultDistribution.put("POSITIVE", positiveResults);
        resultDistribution.put("NEUTRAL", neutralResults);
        resultDistribution.put("NEGATIVE", negativeResults);
        resultDistribution.put("PENDING_REVIEW", pendingReviewResults);
        resultDistribution.put("REQUIRES_FOLLOW_UP", requiresFollowUpResults);
        
        // Tendencias mensuales
        Map<String, Long> monthlyTrends = convertToMap(interviewRepository.findMonthlyStatistics());
        
        // Métricas adicionales - followUpRequired es transient
        long followUpRequired = 0L;
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
        // Primero verificar si el entrevistador tiene horarios configurados y está disponible según el sistema de horarios
        boolean hasScheduleAvailability = interviewerScheduleService.isInterviewerAvailable(interviewerId, date, time);
        
        if (!hasScheduleAvailability) {
            log.warn("Entrevistador ID: {} no tiene disponibilidad configurada para fecha: {} hora: {}", 
                    interviewerId, date, time);
            throw new IllegalArgumentException("El entrevistador no tiene horarios disponibles configurados para esa fecha y hora");
        }
        
        // Luego verificar conflictos con entrevistas ya programadas
        long conflicts = interviewRepository.countConflictingInterviews(interviewerId, LocalDateTime.of(date, time));
        
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
        
        log.debug("Validación de disponibilidad exitosa para entrevistador ID: {} en fecha: {} hora: {}", 
                interviewerId, date, time);
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        return results.stream()
            .collect(Collectors.toMap(
                result -> result[0].toString(),
                result -> ((Number) result[1]).longValue()
            ));
    }

    /**
     * Verifica si todas las entrevistas requeridas (PSYCHOLOGICAL, FAMILY, INDIVIDUAL) están completadas
     * y actualiza el estado de la aplicación a APPROVED si es así
     */
    private void checkAndUpdateApplicationStatusIfAllInterviewsCompleted(Long applicationId) {
        log.info("Verificando estado de entrevistas para aplicación ID: {}", applicationId);
        
        try {
            // Obtener todas las entrevistas de la aplicación
            List<Interview> interviews = interviewRepository.findByApplicationId(applicationId);
            
            // Verificar que existan las 3 entrevistas requeridas y que estén completadas
            boolean hasPsychological = interviews.stream()
                .anyMatch(i -> i.getType() == Interview.InterviewType.PSYCHOLOGICAL && 
                             i.getStatus() == Interview.InterviewStatus.COMPLETED);
                             
            boolean hasFamily = interviews.stream()
                .anyMatch(i -> i.getType() == Interview.InterviewType.FAMILY && 
                             i.getStatus() == Interview.InterviewStatus.COMPLETED);
                             
            boolean hasIndividual = interviews.stream()
                .anyMatch(i -> i.getType() == Interview.InterviewType.INDIVIDUAL && 
                             i.getStatus() == Interview.InterviewStatus.COMPLETED);
            
            if (hasPsychological && hasFamily && hasIndividual) {
                // Todas las entrevistas están completadas, actualizar el estado de la aplicación
                log.info("Todas las entrevistas están completadas para aplicación ID: {}. Actualizando estado a APPROVED", applicationId);
                
                // Aquí necesitaríamos inyectar el ApplicationService para actualizar el estado
                // Por ahora lo dejaremos como log para evitar dependencia circular
                log.info("ACCIÓN REQUERIDA: Actualizar estado de aplicación {} a APPROVED - todas las entrevistas completadas", applicationId);
                
                // TODO: Implementar actualización de estado de aplicación cuando esté disponible ApplicationService
            } else {
                log.debug("Entrevistas pendientes para aplicación ID: {} - Psicológica: {}, Familia: {}, Individual: {}", 
                    applicationId, hasPsychological, hasFamily, hasIndividual);
            }
            
        } catch (Exception e) {
            log.error("Error al verificar estado de entrevistas para aplicación ID: {}: {}", applicationId, e.getMessage());
        }
    }

    /**
     * Verificar si una aplicación tiene las 3 entrevistas requeridas
     */
    private boolean hasAllRequiredInterviews(List<Interview> interviews) {
        boolean hasPsychological = interviews.stream().anyMatch(i -> i.getType() == Interview.InterviewType.PSYCHOLOGICAL);
        boolean hasFamily = interviews.stream().anyMatch(i -> i.getType() == Interview.InterviewType.FAMILY);
        boolean hasIndividual = interviews.stream().anyMatch(i -> i.getType() == Interview.InterviewType.INDIVIDUAL);
        
        return hasPsychological && hasFamily && hasIndividual;
    }
}