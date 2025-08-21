package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.EvaluationSchedule;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.EvaluationScheduleRepository;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationScheduleService {

    private final EvaluationScheduleRepository scheduleRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    /**
     * Crear programación genérica para un tipo de evaluación y nivel
     */
    public EvaluationSchedule createGenericSchedule(
            Evaluation.EvaluationType evaluationType,
            String gradeLevel,
            String subject,
            Long evaluatorId,
            LocalDateTime scheduledDate,
            Integer durationMinutes,
            String location,
            String instructions) {

        User evaluator = userRepository.findById(evaluatorId)
            .orElseThrow(() -> new RuntimeException("Evaluador no encontrado"));

        // Verificar conflictos de horario
        if (hasScheduleConflict(evaluatorId, scheduledDate, durationMinutes, null)) {
            throw new RuntimeException("El evaluador ya tiene una cita programada en ese horario");
        }

        EvaluationSchedule schedule = new EvaluationSchedule();
        schedule.setEvaluationType(evaluationType);
        schedule.setGradeLevel(gradeLevel);
        schedule.setSubject(subject);
        schedule.setEvaluator(evaluator);
        schedule.setScheduledDate(scheduledDate);
        schedule.setDurationMinutes(durationMinutes);
        schedule.setLocation(location);
        schedule.setInstructions(instructions);
        schedule.setScheduleType(EvaluationSchedule.ScheduleType.GENERIC);
        schedule.setStatus(EvaluationSchedule.ScheduleStatus.SCHEDULED);

        return scheduleRepository.save(schedule);
    }

    /**
     * Crear programación individual para un estudiante específico
     */
    public EvaluationSchedule createIndividualSchedule(
            Long applicationId,
            Evaluation.EvaluationType evaluationType,
            Long evaluatorId,
            LocalDateTime scheduledDate,
            Integer durationMinutes,
            String location,
            String instructions,
            boolean requiresConfirmation,
            String attendeesRequired) {

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        User evaluator = userRepository.findById(evaluatorId)
            .orElseThrow(() -> new RuntimeException("Evaluador no encontrado"));

        // Verificar conflictos de horario
        if (hasScheduleConflict(evaluatorId, scheduledDate, durationMinutes, null)) {
            throw new RuntimeException("El evaluador ya tiene una cita programada en ese horario");
        }

        EvaluationSchedule schedule = new EvaluationSchedule();
        schedule.setApplication(application);
        schedule.setEvaluationType(evaluationType);
        schedule.setGradeLevel(application.getStudent().getGradeApplied());
        schedule.setEvaluator(evaluator);
        schedule.setScheduledDate(scheduledDate);
        schedule.setDurationMinutes(durationMinutes);
        schedule.setLocation(location);
        schedule.setInstructions(instructions);
        schedule.setScheduleType(EvaluationSchedule.ScheduleType.INDIVIDUAL);
        schedule.setStatus(EvaluationSchedule.ScheduleStatus.SCHEDULED);
        schedule.setRequiresConfirmation(requiresConfirmation);
        schedule.setAttendeesRequired(attendeesRequired);

        if (requiresConfirmation) {
            // Deadline de confirmación: 24 horas antes de la cita
            schedule.setConfirmationDeadline(scheduledDate.minus(24, ChronoUnit.HOURS));
        }

        return scheduleRepository.save(schedule);
    }

    /**
     * Obtener próximas citas para una familia
     */
    @Transactional(readOnly = true)
    public List<EvaluationSchedule> getUpcomingSchedulesForFamily(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        String gradeLevel = application.getStudent().getGradeApplied();
        
        // Tipos de evaluación típicos para este nivel
        List<Evaluation.EvaluationType> relevantTypes = List.of(
            Evaluation.EvaluationType.MATHEMATICS_EXAM,
            Evaluation.EvaluationType.LANGUAGE_EXAM,
            Evaluation.EvaluationType.ENGLISH_EXAM,
            Evaluation.EvaluationType.PSYCHOLOGICAL_INTERVIEW,
            Evaluation.EvaluationType.CYCLE_DIRECTOR_INTERVIEW
        );

        return scheduleRepository.findUpcomingForFamily(
            applicationId, 
            gradeLevel, 
            relevantTypes, 
            LocalDateTime.now()
        );
    }

    /**
     * Obtener calendario del evaluador
     */
    @Transactional(readOnly = true)
    public List<EvaluationSchedule> getEvaluatorSchedule(Long evaluatorId, LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleRepository.findByEvaluatorAndDateRange(evaluatorId, startDate, endDate);
    }

    /**
     * Confirmar cita (por parte de la familia)
     */
    public EvaluationSchedule confirmSchedule(Long scheduleId, Long userId) {
        EvaluationSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!schedule.getRequiresConfirmation()) {
            throw new RuntimeException("Esta cita no requiere confirmación");
        }

        if (schedule.getConfirmedAt() != null) {
            throw new RuntimeException("Esta cita ya ha sido confirmada");
        }

        if (LocalDateTime.now().isAfter(schedule.getConfirmationDeadline())) {
            throw new RuntimeException("El plazo de confirmación ha expirado");
        }

        schedule.setConfirmedAt(LocalDateTime.now());
        schedule.setConfirmedBy(user);
        schedule.setStatus(EvaluationSchedule.ScheduleStatus.CONFIRMED);

        return scheduleRepository.save(schedule);
    }

    /**
     * Reprogramar cita
     */
    public EvaluationSchedule rescheduleAppointment(
            Long scheduleId, 
            LocalDateTime newDate, 
            String reason) {

        EvaluationSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        // Verificar conflictos con la nueva fecha
        if (hasScheduleConflict(schedule.getEvaluator().getId(), newDate, schedule.getDurationMinutes(), scheduleId)) {
            throw new RuntimeException("El evaluador ya tiene una cita en el nuevo horario");
        }

        // Marcar la cita actual como reprogramada
        schedule.setStatus(EvaluationSchedule.ScheduleStatus.RESCHEDULED);
        scheduleRepository.save(schedule);

        // Crear nueva programación
        EvaluationSchedule newSchedule = new EvaluationSchedule();
        newSchedule.setApplication(schedule.getApplication());
        newSchedule.setEvaluationType(schedule.getEvaluationType());
        newSchedule.setGradeLevel(schedule.getGradeLevel());
        newSchedule.setSubject(schedule.getSubject());
        newSchedule.setEvaluator(schedule.getEvaluator());
        newSchedule.setScheduledDate(newDate);
        newSchedule.setDurationMinutes(schedule.getDurationMinutes());
        newSchedule.setLocation(schedule.getLocation());
        newSchedule.setInstructions(schedule.getInstructions() + "\n\nReprogramada por: " + reason);
        newSchedule.setScheduleType(schedule.getScheduleType());
        newSchedule.setStatus(EvaluationSchedule.ScheduleStatus.SCHEDULED);
        newSchedule.setRequiresConfirmation(schedule.getRequiresConfirmation());
        newSchedule.setAttendeesRequired(schedule.getAttendeesRequired());
        newSchedule.setPreparationMaterials(schedule.getPreparationMaterials());

        if (schedule.getRequiresConfirmation()) {
            newSchedule.setConfirmationDeadline(newDate.minus(24, ChronoUnit.HOURS));
        }

        return scheduleRepository.save(newSchedule);
    }

    /**
     * Verificar conflictos de horario
     */
    private boolean hasScheduleConflict(Long evaluatorId, LocalDateTime startTime, Integer durationMinutes, Long excludeScheduleId) {
        LocalDateTime endTime = startTime.plus(durationMinutes, ChronoUnit.MINUTES);
        Long excludeId = excludeScheduleId != null ? excludeScheduleId : -1L;
        
        List<EvaluationSchedule> conflicts = scheduleRepository.findScheduleConflicts(
            evaluatorId, startTime, endTime, excludeId);
        
        return !conflicts.isEmpty();
    }

    /**
     * Asignar automáticamente estudiantes a programaciones genéricas
     */
    @Transactional
    public void assignStudentsToGenericSchedules(String gradeLevel, Evaluation.EvaluationType evaluationType) {
        // Encontrar programaciones genéricas para este nivel y tipo
        List<EvaluationSchedule> genericSchedules = scheduleRepository
            .findByEvaluationTypeAndGradeLevelAndApplicationIsNull(evaluationType, gradeLevel);

        // Encontrar estudiantes del nivel que necesitan esta evaluación
        // TODO: Implementar lógica para encontrar estudiantes que necesiten la evaluación
        
        log.info("Asignando estudiantes a {} programaciones genéricas de {} para {}", 
                genericSchedules.size(), evaluationType, gradeLevel);
    }

    /**
     * Obtener citas pendientes de confirmación
     */
    @Transactional(readOnly = true)
    public List<EvaluationSchedule> getPendingConfirmations() {
        return scheduleRepository.findPendingConfirmations(LocalDateTime.now());
    }

    /**
     * Marcar cita como completada
     */
    public EvaluationSchedule markAsCompleted(Long scheduleId) {
        EvaluationSchedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("Programación no encontrada"));

        schedule.setStatus(EvaluationSchedule.ScheduleStatus.COMPLETED);
        return scheduleRepository.save(schedule);
    }
}