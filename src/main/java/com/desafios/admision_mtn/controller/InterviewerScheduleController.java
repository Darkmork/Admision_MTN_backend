package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.InterviewerSchedule;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.InterviewerScheduleService;
import com.desafios.admision_mtn.service.InterviewerScheduleService.RecurringScheduleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviewer-schedules")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "http://localhost:5173", 
    "http://localhost:5174", 
    "http://localhost:5175", 
    "http://localhost:5176"
})
public class InterviewerScheduleController {

    private final InterviewerScheduleService interviewerScheduleService;

    /**
     * Crear un nuevo horario para entrevistador
     * Solo ADMIN, CYCLE_DIRECTOR y el propio entrevistador pueden crear/modificar horarios
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #schedule.interviewer.id == authentication.principal.id")
    public ResponseEntity<?> createSchedule(@Valid @RequestBody InterviewerSchedule schedule) {
        try {
            log.info("Creando nuevo horario para entrevistador");
            InterviewerSchedule createdSchedule = interviewerScheduleService.createSchedule(schedule);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
        } catch (Exception e) {
            log.error("Error al crear horario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear horario", "message", e.getMessage()));
        }
    }

    /**
     * Actualizar un horario existente
     */
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or @interviewerScheduleService.findScheduleById(#scheduleId).interviewer.id == authentication.principal.id")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody InterviewerSchedule updatedSchedule) {
        try {
            log.info("Actualizando horario ID: {}", scheduleId);
            InterviewerSchedule schedule = interviewerScheduleService.updateSchedule(scheduleId, updatedSchedule);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error al actualizar horario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al actualizar horario", "message", e.getMessage()));
        }
    }

    /**
     * Desactivar un horario (soft delete)
     */
    @PutMapping("/{scheduleId}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or @interviewerScheduleService.findScheduleById(#scheduleId).interviewer.id == authentication.principal.id")
    public ResponseEntity<?> deactivateSchedule(@PathVariable Long scheduleId) {
        try {
            log.info("Desactivando horario ID: {}", scheduleId);
            interviewerScheduleService.deactivateSchedule(scheduleId);
            return ResponseEntity.ok(Map.of("message", "Horario desactivado exitosamente"));
        } catch (Exception e) {
            log.error("Error al desactivar horario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al desactivar horario", "message", e.getMessage()));
        }
    }

    /**
     * Eliminar permanentemente un horario
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            log.info("Eliminando permanentemente horario ID: {}", scheduleId);
            interviewerScheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(Map.of("message", "Horario eliminado permanentemente"));
        } catch (Exception e) {
            log.error("Error al eliminar horario: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al eliminar horario", "message", e.getMessage()));
        }
    }

    /**
     * Obtener un horario por ID
     */
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<?> getScheduleById(@PathVariable Long scheduleId) {
        try {
            InterviewerSchedule schedule = interviewerScheduleService.findScheduleById(scheduleId);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error al obtener horario: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener todos los horarios activos de un entrevistador
     */
    @GetMapping("/interviewer/{interviewerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> getInterviewerSchedules(@PathVariable Long interviewerId) {
        try {
            List<InterviewerSchedule> schedules = interviewerScheduleService.getInterviewerActiveSchedules(interviewerId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error al obtener horarios del entrevistador: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener horarios", "message", e.getMessage()));
        }
    }

    /**
     * Obtener horarios de un entrevistador para un año específico
     */
    @GetMapping("/interviewer/{interviewerId}/year/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> getInterviewerSchedulesByYear(
            @PathVariable Long interviewerId,
            @PathVariable Integer year) {
        try {
            List<InterviewerSchedule> schedules = interviewerScheduleService.getInterviewerSchedulesByYear(interviewerId, year);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            log.error("Error al obtener horarios por año: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener horarios", "message", e.getMessage()));
        }
    }

    /**
     * Buscar entrevistadores disponibles en una fecha y hora específica
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> findAvailableInterviewers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            log.info("Buscando entrevistadores disponibles para {} a las {}", date, time);
            List<User> availableInterviewers = interviewerScheduleService.findAvailableInterviewers(date, time);
            return ResponseEntity.ok(availableInterviewers);
        } catch (Exception e) {
            log.error("Error al buscar entrevistadores disponibles: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al buscar entrevistadores", "message", e.getMessage()));
        }
    }

    /**
     * Verificar si un entrevistador está disponible en una fecha y hora específica
     */
    @GetMapping("/interviewer/{interviewerId}/availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> checkInterviewerAvailability(
            @PathVariable Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        try {
            boolean isAvailable = interviewerScheduleService.isInterviewerAvailable(interviewerId, date, time);
            return ResponseEntity.ok(Map.of(
                "interviewerId", interviewerId,
                "date", date,
                "time", time,
                "available", isAvailable
            ));
        } catch (Exception e) {
            log.error("Error al verificar disponibilidad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al verificar disponibilidad", "message", e.getMessage()));
        }
    }

    /**
     * Crear múltiples horarios recurrentes para un entrevistador
     */
    @PostMapping("/interviewer/{interviewerId}/recurring/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> createRecurringSchedules(
            @PathVariable Long interviewerId,
            @PathVariable Integer year,
            @Valid @RequestBody List<RecurringScheduleRequest> scheduleRequests) {
        try {
            log.info("Creando {} horarios recurrentes para entrevistador ID: {}", scheduleRequests.size(), interviewerId);
            List<InterviewerSchedule> createdSchedules = interviewerScheduleService
                    .createRecurringSchedules(interviewerId, year, scheduleRequests);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedules);
        } catch (Exception e) {
            log.error("Error al crear horarios recurrentes: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear horarios recurrentes", "message", e.getMessage()));
        }
    }

    /**
     * Crear excepción (día no disponible) para un entrevistador
     */
    @PostMapping("/interviewer/{interviewerId}/exception")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> createException(
            @PathVariable Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String notes) {
        try {
            log.info("Creando excepción para entrevistador ID: {} en fecha: {}", interviewerId, date);
            InterviewerSchedule exception = interviewerScheduleService.createException(interviewerId, date, notes);
            return ResponseEntity.status(HttpStatus.CREATED).body(exception);
        } catch (Exception e) {
            log.error("Error al crear excepción: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear excepción", "message", e.getMessage()));
        }
    }

    /**
     * Copiar horarios de un año a otro
     */
    @PostMapping("/interviewer/{interviewerId}/copy-schedules")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or #interviewerId == authentication.principal.id")
    public ResponseEntity<?> copySchedulesToYear(
            @PathVariable Long interviewerId,
            @RequestParam Integer fromYear,
            @RequestParam Integer toYear) {
        try {
            log.info("Copiando horarios del entrevistador ID: {} del año {} al año {}", 
                    interviewerId, fromYear, toYear);
            List<InterviewerSchedule> copiedSchedules = interviewerScheduleService
                    .copySchedulesToYear(interviewerId, fromYear, toYear);
            return ResponseEntity.status(HttpStatus.CREATED).body(copiedSchedules);
        } catch (Exception e) {
            log.error("Error al copiar horarios: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al copiar horarios", "message", e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas de carga de trabajo por entrevistador
     */
    @GetMapping("/statistics/workload/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> getWorkloadStatistics(@PathVariable Integer year) {
        try {
            List<Object[]> statistics = interviewerScheduleService.getWorkloadStatistics(year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener estadísticas", "message", e.getMessage()));
        }
    }

    /**
     * Obtener entrevistadores que tienen horarios configurados
     */
    @GetMapping("/interviewers-with-schedules/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    public ResponseEntity<?> getInterviewersWithSchedules(@PathVariable Integer year) {
        try {
            List<User> interviewers = interviewerScheduleService.getInterviewersWithSchedules(year);
            return ResponseEntity.ok(interviewers);
        } catch (Exception e) {
            log.error("Error al obtener entrevistadores con horarios: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener entrevistadores", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para verificar el estado del servicio
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "InterviewerScheduleService",
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    // DTOs para requests específicos

    public static class CreateScheduleRequest {
        private Long interviewerId;
        private InterviewerSchedule.ScheduleType scheduleType;
        private java.time.DayOfWeek dayOfWeek;
        private LocalDate specificDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer year;
        private String notes;

        // Constructors, getters, setters
        public CreateScheduleRequest() {}

        // Getters and setters
        public Long getInterviewerId() { return interviewerId; }
        public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

        public InterviewerSchedule.ScheduleType getScheduleType() { return scheduleType; }
        public void setScheduleType(InterviewerSchedule.ScheduleType scheduleType) { this.scheduleType = scheduleType; }

        public java.time.DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(java.time.DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public LocalDate getSpecificDate() { return specificDate; }
        public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}