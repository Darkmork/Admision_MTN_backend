package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.InterviewerSchedule;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.repository.InterviewerScheduleRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InterviewerScheduleService {

    private final InterviewerScheduleRepository interviewerScheduleRepository;
    private final UserRepository userRepository;

    /**
     * Crear un nuevo horario para un entrevistador
     */
    public InterviewerSchedule createSchedule(InterviewerSchedule schedule) {
        log.info("Creando horario para entrevistador ID: {}", schedule.getInterviewer().getId());
        
        // Validar que el usuario existe y tiene rol de entrevistador
        validateInterviewer(schedule.getInterviewer());
        
        // Validar que no hay conflictos de horario
        validateNoScheduleConflicts(schedule);
        
        // Validar lógica de negocio del horario
        validateScheduleLogic(schedule);
        
        InterviewerSchedule savedSchedule = interviewerScheduleRepository.save(schedule);
        log.info("Horario creado exitosamente con ID: {}", savedSchedule.getId());
        
        return savedSchedule;
    }

    /**
     * Actualizar un horario existente
     */
    public InterviewerSchedule updateSchedule(Long scheduleId, InterviewerSchedule updatedSchedule) {
        log.info("Actualizando horario ID: {}", scheduleId);
        
        InterviewerSchedule existingSchedule = findScheduleById(scheduleId);
        
        // Validar que no hay conflictos (excluyendo el horario actual)
        validateNoScheduleConflicts(updatedSchedule, scheduleId);
        
        // Actualizar campos
        existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
        existingSchedule.setStartTime(updatedSchedule.getStartTime());
        existingSchedule.setEndTime(updatedSchedule.getEndTime());
        existingSchedule.setSpecificDate(updatedSchedule.getSpecificDate());
        existingSchedule.setScheduleType(updatedSchedule.getScheduleType());
        existingSchedule.setNotes(updatedSchedule.getNotes());
        existingSchedule.setIsActive(updatedSchedule.getIsActive());
        
        // Validar lógica de negocio
        validateScheduleLogic(existingSchedule);
        
        InterviewerSchedule savedSchedule = interviewerScheduleRepository.save(existingSchedule);
        log.info("Horario actualizado exitosamente ID: {}", savedSchedule.getId());
        
        return savedSchedule;
    }

    /**
     * Desactivar un horario (soft delete)
     */
    public void deactivateSchedule(Long scheduleId) {
        log.info("Desactivando horario ID: {}", scheduleId);
        
        InterviewerSchedule schedule = findScheduleById(scheduleId);
        schedule.setIsActive(false);
        interviewerScheduleRepository.save(schedule);
        
        log.info("Horario desactivado exitosamente ID: {}", scheduleId);
    }

    /**
     * Eliminar permanentemente un horario
     */
    public void deleteSchedule(Long scheduleId) {
        log.info("Eliminando permanentemente horario ID: {}", scheduleId);
        
        if (!interviewerScheduleRepository.existsById(scheduleId)) {
            throw new RuntimeException("Horario no encontrado con ID: " + scheduleId);
        }
        
        interviewerScheduleRepository.deleteById(scheduleId);
        log.info("Horario eliminado permanentemente ID: {}", scheduleId);
    }

    /**
     * Obtener horario por ID
     */
    @Transactional(readOnly = true)
    public InterviewerSchedule findScheduleById(Long scheduleId) {
        return interviewerScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + scheduleId));
    }

    /**
     * Obtener todos los horarios activos de un entrevistador
     */
    @Transactional(readOnly = true)
    public List<InterviewerSchedule> getInterviewerActiveSchedules(Long interviewerId) {
        User interviewer = findInterviewerById(interviewerId);
        return interviewerScheduleRepository.findByInterviewerAndIsActiveTrue(interviewer);
    }

    /**
     * Obtener horarios de un entrevistador para un año específico
     */
    @Transactional(readOnly = true)
    public List<InterviewerSchedule> getInterviewerSchedulesByYear(Long interviewerId, Integer year) {
        User interviewer = findInterviewerById(interviewerId);
        return interviewerScheduleRepository.findByInterviewerAndYearAndIsActiveTrue(interviewer, year);
    }

    /**
     * Buscar entrevistadores disponibles en una fecha y hora específica
     */
    @Transactional(readOnly = true)
    public List<User> findAvailableInterviewers(LocalDate date, LocalTime time) {
        log.info("Buscando entrevistadores disponibles para fecha: {} y hora: {}", date, time);
        
        Integer year = date.getYear();
        List<User> availableInterviewers = interviewerScheduleRepository
                .findAvailableInterviewers(date, time, year);
        
        log.info("Encontrados {} entrevistadores disponibles", availableInterviewers.size());
        return availableInterviewers;
    }

    /**
     * Verificar si un entrevistador está disponible en una fecha y hora específica
     */
    @Transactional(readOnly = true)
    public boolean isInterviewerAvailable(Long interviewerId, LocalDate date, LocalTime time) {
        User interviewer = findInterviewerById(interviewerId);
        
        // Obtener horarios recurrentes para el día de la semana
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<InterviewerSchedule> recurringSchedules = interviewerScheduleRepository
                .findRecurringSchedules(interviewer, dayOfWeek, date.getYear());
        
        // Obtener horarios específicos para la fecha
        List<InterviewerSchedule> specificSchedules = interviewerScheduleRepository
                .findSpecificDateSchedules(interviewer, date);
        
        // Verificar excepciones (días no disponibles)
        List<InterviewerSchedule> exceptions = interviewerScheduleRepository
                .findExceptions(interviewer, date);
        
        if (!exceptions.isEmpty()) {
            log.debug("Entrevistador {} tiene excepciones en la fecha {}", interviewerId, date);
            return false;
        }
        
        // Verificar disponibilidad en horarios recurrentes
        boolean availableInRecurring = recurringSchedules.stream()
                .anyMatch(schedule -> schedule.isAvailableAt(date, time));
        
        // Verificar disponibilidad en horarios específicos
        boolean availableInSpecific = specificSchedules.stream()
                .anyMatch(schedule -> schedule.isAvailableAt(date, time));
        
        return availableInRecurring || availableInSpecific;
    }

    /**
     * Obtener estadísticas de carga de trabajo por entrevistador
     */
    @Transactional(readOnly = true)
    public List<Object[]> getWorkloadStatistics(Integer year) {
        return interviewerScheduleRepository.getWorkloadStatistics(year);
    }

    /**
     * Obtener entrevistadores que tienen horarios configurados
     */
    @Transactional(readOnly = true)
    public List<User> getInterviewersWithSchedules(Integer year) {
        return interviewerScheduleRepository.findInterviewersWithSchedules(year);
    }

    /**
     * Crear múltiples horarios recurrentes para un entrevistador
     */
    public List<InterviewerSchedule> createRecurringSchedules(Long interviewerId, Integer year,
            List<RecurringScheduleRequest> scheduleRequests) {
        log.info("Creando {} horarios recurrentes para entrevistador ID: {}", 
                scheduleRequests.size(), interviewerId);
        
        User interviewer = findInterviewerById(interviewerId);
        
        return scheduleRequests.stream()
                .map(request -> {
                    InterviewerSchedule schedule = InterviewerSchedule.builder()
                            .interviewer(interviewer)
                            .dayOfWeek(request.getDayOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .year(year)
                            .scheduleType(InterviewerSchedule.ScheduleType.RECURRING)
                            .notes(request.getNotes())
                            .build();
                    
                    return createSchedule(schedule);
                })
                .toList();
    }

    /**
     * Crear excepción (día no disponible) para un entrevistador
     */
    public InterviewerSchedule createException(Long interviewerId, LocalDate date, String notes) {
        log.info("Creando excepción para entrevistador ID: {} en fecha: {}", interviewerId, date);
        
        User interviewer = findInterviewerById(interviewerId);
        
        InterviewerSchedule exception = InterviewerSchedule.builder()
                .interviewer(interviewer)
                .specificDate(date)
                .year(date.getYear())
                .scheduleType(InterviewerSchedule.ScheduleType.EXCEPTION)
                .startTime(LocalTime.of(0, 0)) // Dummy values for exception
                .endTime(LocalTime.of(23, 59))
                .notes(notes)
                .build();
        
        return createSchedule(exception);
    }

    /**
     * Copiar horarios de un año a otro
     */
    public List<InterviewerSchedule> copySchedulesToYear(Long interviewerId, Integer fromYear, Integer toYear) {
        log.info("Copiando horarios del entrevistador ID: {} del año {} al año {}", 
                interviewerId, fromYear, toYear);
        
        User interviewer = findInterviewerById(interviewerId);
        List<InterviewerSchedule> sourceSchedules = interviewerScheduleRepository
                .findByInterviewerAndYearAndIsActiveTrue(interviewer, fromYear);
        
        return sourceSchedules.stream()
                .filter(schedule -> schedule.getScheduleType() == InterviewerSchedule.ScheduleType.RECURRING)
                .map(schedule -> {
                    InterviewerSchedule newSchedule = InterviewerSchedule.builder()
                            .interviewer(schedule.getInterviewer())
                            .dayOfWeek(schedule.getDayOfWeek())
                            .startTime(schedule.getStartTime())
                            .endTime(schedule.getEndTime())
                            .year(toYear)
                            .scheduleType(schedule.getScheduleType())
                            .notes("Copiado desde " + fromYear)
                            .build();
                    
                    return createSchedule(newSchedule);
                })
                .toList();
    }

    // Métodos de validación privados
    
    private void validateInterviewer(User interviewer) {
        if (interviewer == null || interviewer.getId() == null) {
            throw new IllegalArgumentException("Entrevistador no puede ser nulo");
        }
        
        User existingUser = findInterviewerById(interviewer.getId());
        
        if (!isValidInterviewerRole(existingUser)) {
            throw new IllegalArgumentException("El usuario no tiene un rol válido para realizar entrevistas");
        }
    }
    
    private boolean isValidInterviewerRole(User user) {
        return user.getRole() != null && (
                user.getRole().name().equals("CYCLE_DIRECTOR") ||
                user.getRole().name().equals("PSYCHOLOGIST") ||
                user.getRole().name().equals("COORDINATOR")
        );
    }
    
    private User findInterviewerById(Long interviewerId) {
        return userRepository.findById(interviewerId)
                .orElseThrow(() -> new RuntimeException("Entrevistador no encontrado con ID: " + interviewerId));
    }
    
    private void validateNoScheduleConflicts(InterviewerSchedule schedule) {
        validateNoScheduleConflicts(schedule, null);
    }
    
    private void validateNoScheduleConflicts(InterviewerSchedule schedule, Long excludeId) {
        List<InterviewerSchedule> conflicts = interviewerScheduleRepository.findConflictingSchedules(
                schedule.getInterviewer(),
                schedule.getDayOfWeek(),
                schedule.getSpecificDate(),
                schedule.getYear(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                excludeId != null ? excludeId : -1L
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("El horario se superpone con horarios existentes del entrevistador");
        }
    }
    
    private void validateScheduleLogic(InterviewerSchedule schedule) {
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new IllegalArgumentException("Hora de inicio y fin son obligatorias");
        }
        
        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }
        
        if (schedule.getYear() == null || schedule.getYear() < Year.now().getValue()) {
            throw new IllegalArgumentException("El año debe ser actual o futuro");
        }
        
        if (schedule.getScheduleType() == null) {
            throw new IllegalArgumentException("Tipo de horario es obligatorio");
        }
        
        // Validaciones específicas por tipo
        switch (schedule.getScheduleType()) {
            case RECURRING:
                if (schedule.getDayOfWeek() == null) {
                    throw new IllegalArgumentException("Día de la semana es obligatorio para horarios recurrentes");
                }
                if (schedule.getSpecificDate() != null) {
                    throw new IllegalArgumentException("Fecha específica no debe estar presente en horarios recurrentes");
                }
                break;
                
            case SPECIFIC_DATE:
                if (schedule.getSpecificDate() == null) {
                    throw new IllegalArgumentException("Fecha específica es obligatoria para este tipo de horario");
                }
                if (schedule.getSpecificDate().isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("La fecha específica no puede ser en el pasado");
                }
                break;
                
            case EXCEPTION:
                if (schedule.getSpecificDate() == null) {
                    throw new IllegalArgumentException("Fecha específica es obligatoria para excepciones");
                }
                break;
        }
    }
    
    // ========== MÉTODOS PARA API UNIFICADA ==========
    
    /**
     * Obtiene resumen de disponibilidad para una fecha específica
     */
    public Map<String, Object> getAvailabilitySummary(String date) {
        Map<String, Object> summary = new HashMap<>();
        try {
            LocalDate targetDate = LocalDate.parse(date);
            
            // Obtener conteo básico de horarios activos
            List<InterviewerSchedule> allSchedules = interviewerScheduleRepository.findAll();
            int activeSchedules = (int) allSchedules.stream()
                .filter(schedule -> schedule.getYear() >= Year.now().getValue())
                .count();
                
            summary.put("date", date);
            summary.put("activeSchedules", activeSchedules);
            summary.put("status", "available");
            
        } catch (Exception e) {
            log.error("Error getting availability summary", e);
            summary.put("error", "Error al obtener resumen de disponibilidad");
        }
        
        return summary;
    }

    // DTO para crear horarios recurrentes
    public static class RecurringScheduleRequest {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private String notes;

        // Constructors, getters, setters
        public RecurringScheduleRequest() {}

        public RecurringScheduleRequest(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, String notes) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.notes = notes;
        }

        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}