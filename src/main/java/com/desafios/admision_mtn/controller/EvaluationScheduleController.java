package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.EvaluationSchedule;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.EvaluationScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class EvaluationScheduleController {

    private final EvaluationScheduleService scheduleService;

    /**
     * Crear programación genérica (solo administradores)
     */
    @PostMapping("/generic")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EvaluationSchedule> createGenericSchedule(@RequestBody CreateGenericScheduleRequest request) {
        EvaluationSchedule schedule = scheduleService.createGenericSchedule(
            request.getEvaluationType(),
            request.getGradeLevel(),
            request.getSubject(),
            request.getEvaluatorId(),
            request.getScheduledDate(),
            request.getDurationMinutes(),
            request.getLocation(),
            request.getInstructions()
        );
        return ResponseEntity.ok(schedule);
    }

    /**
     * Crear programación individual (administradores y evaluadores)
     */
    @PostMapping("/individual")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('PSYCHOLOGIST') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<EvaluationSchedule> createIndividualSchedule(@RequestBody CreateIndividualScheduleRequest request) {
        EvaluationSchedule schedule = scheduleService.createIndividualSchedule(
            request.getApplicationId(),
            request.getEvaluationType(),
            request.getEvaluatorId(),
            request.getScheduledDate(),
            request.getDurationMinutes(),
            request.getLocation(),
            request.getInstructions(),
            request.isRequiresConfirmation(),
            request.getAttendeesRequired()
        );
        return ResponseEntity.ok(schedule);
    }

    /**
     * Obtener próximas citas para una familia
     */
    @GetMapping("/family/{applicationId}")
    @PreAuthorize("hasRole('APODERADO') or hasRole('ADMIN')")
    public ResponseEntity<List<EvaluationSchedule>> getFamilySchedules(@PathVariable Long applicationId) {
        List<EvaluationSchedule> schedules = scheduleService.getUpcomingSchedulesForFamily(applicationId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Obtener calendario del evaluador
     */
    @GetMapping("/evaluator/{evaluatorId}")
    @PreAuthorize("hasRole('ADMIN') or @evaluationScheduleController.isCurrentUser(#evaluatorId)")
    public ResponseEntity<List<EvaluationSchedule>> getEvaluatorSchedule(
            @PathVariable Long evaluatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<EvaluationSchedule> schedules = scheduleService.getEvaluatorSchedule(evaluatorId, startDate, endDate);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Confirmar cita (familias)
     */
    @PutMapping("/{scheduleId}/confirm")
    @PreAuthorize("hasRole('APODERADO') or hasRole('ADMIN')")
    public ResponseEntity<EvaluationSchedule> confirmSchedule(
            @PathVariable Long scheduleId,
            @RequestParam Long userId) {
        EvaluationSchedule schedule = scheduleService.confirmSchedule(scheduleId, userId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Reprogramar cita
     */
    @PutMapping("/{scheduleId}/reschedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('PSYCHOLOGIST') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<EvaluationSchedule> rescheduleAppointment(
            @PathVariable Long scheduleId,
            @RequestBody RescheduleRequest request) {
        EvaluationSchedule schedule = scheduleService.rescheduleAppointment(
            scheduleId, 
            request.getNewDate(), 
            request.getReason()
        );
        return ResponseEntity.ok(schedule);
    }

    /**
     * Obtener citas pendientes de confirmación
     */
    @GetMapping("/pending-confirmations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EvaluationSchedule>> getPendingConfirmations() {
        List<EvaluationSchedule> schedules = scheduleService.getPendingConfirmations();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Marcar cita como completada
     */
    @PutMapping("/{scheduleId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('PSYCHOLOGIST') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<EvaluationSchedule> markAsCompleted(@PathVariable Long scheduleId) {
        EvaluationSchedule schedule = scheduleService.markAsCompleted(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Endpoint público para demostración - Obtener citas mock
     */
    @GetMapping("/public/mock-schedules/{applicationId}")
    public ResponseEntity<List<MockEvaluationSchedule>> getMockFamilySchedules(@PathVariable Long applicationId) {
        List<MockEvaluationSchedule> mockSchedules = generateMockSchedules(applicationId);
        return ResponseEntity.ok(mockSchedules);
    }

    /**
     * Verificar si el usuario actual es el evaluador
     */
    public boolean isCurrentUser(Long evaluatorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getId().equals(evaluatorId);
    }

    private List<MockEvaluationSchedule> generateMockSchedules(Long applicationId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime futureDate1 = now.plusDays(7);
        java.time.LocalDateTime futureDate2 = now.plusDays(14);
        java.time.LocalDateTime pastDate = now.minusDays(7);

        return java.util.Arrays.asList(
            new MockEvaluationSchedule(
                1L, "MATHEMATICS_EXAM", "4° Básico", null,
                new MockApplication(applicationId, new MockStudent("Juan", "Pérez", "4° Básico")),
                new MockEvaluator(1L, "María", "González", "maria.gonzalez@mtn.cl"),
                futureDate1, 90, "Sala de Matemáticas 201", null,
                "Traer calculadora científica y útiles de escritura.",
                "GENERIC", "SCHEDULED", true, futureDate1.minusDays(1), null,
                "Solo el estudiante", "Calculadora científica, lápices, goma de borrar",
                now, now
            ),
            new MockEvaluationSchedule(
                2L, "PSYCHOLOGICAL_INTERVIEW", "4° Básico", null,
                new MockApplication(applicationId, new MockStudent("Juan", "Pérez", "4° Básico")),
                new MockEvaluator(2L, "Carlos", "López", "carlos.lopez@mtn.cl"),
                futureDate2, 60, "Oficina de Psicología", null,
                "Entrevista individual con el estudiante. Los padres deben esperar en el hall.",
                "INDIVIDUAL", "CONFIRMED", true, null, now,
                "Estudiante y al menos un apoderado", "Ninguno específico",
                now, now
            ),
            new MockEvaluationSchedule(
                3L, "LANGUAGE_EXAM", "4° Básico", null,
                new MockApplication(applicationId, new MockStudent("Juan", "Pérez", "4° Básico")),
                new MockEvaluator(3L, "Ana", "Silva", "ana.silva@mtn.cl"),
                pastDate, 90, "Sala de Lenguaje 102", null,
                "Evaluación de comprensión lectora y redacción.",
                "GENERIC", "COMPLETED", false, null, null,
                null, null,
                pastDate, pastDate
            )
        );
    }

    // DTOs para datos mock
    public static class MockEvaluationSchedule {
        public Long id;
        public String evaluationType;
        public String gradeLevel;
        public String subject;
        public MockApplication application;
        public MockEvaluator evaluator;
        public java.time.LocalDateTime scheduledDate;
        public Integer durationMinutes;
        public String location;
        public String meetingLink;
        public String instructions;
        public String scheduleType;
        public String status;
        public Boolean requiresConfirmation;
        public java.time.LocalDateTime confirmationDeadline;
        public java.time.LocalDateTime confirmedAt;
        public String attendeesRequired;
        public String preparationMaterials;
        public java.time.LocalDateTime createdAt;
        public java.time.LocalDateTime updatedAt;

        public MockEvaluationSchedule(Long id, String evaluationType, String gradeLevel, String subject,
                MockApplication application, MockEvaluator evaluator, java.time.LocalDateTime scheduledDate,
                Integer durationMinutes, String location, String meetingLink, String instructions,
                String scheduleType, String status, Boolean requiresConfirmation,
                java.time.LocalDateTime confirmationDeadline, java.time.LocalDateTime confirmedAt,
                String attendeesRequired, String preparationMaterials,
                java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
            this.id = id;
            this.evaluationType = evaluationType;
            this.gradeLevel = gradeLevel;
            this.subject = subject;
            this.application = application;
            this.evaluator = evaluator;
            this.scheduledDate = scheduledDate;
            this.durationMinutes = durationMinutes;
            this.location = location;
            this.meetingLink = meetingLink;
            this.instructions = instructions;
            this.scheduleType = scheduleType;
            this.status = status;
            this.requiresConfirmation = requiresConfirmation;
            this.confirmationDeadline = confirmationDeadline;
            this.confirmedAt = confirmedAt;
            this.attendeesRequired = attendeesRequired;
            this.preparationMaterials = preparationMaterials;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }

    public static class MockApplication {
        public Long id;
        public MockStudent student;

        public MockApplication(Long id, MockStudent student) {
            this.id = id;
            this.student = student;
        }
    }

    public static class MockStudent {
        public String firstName;
        public String lastName;
        public String gradeApplied;

        public MockStudent(String firstName, String lastName, String gradeApplied) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.gradeApplied = gradeApplied;
        }
    }

    public static class MockEvaluator {
        public Long id;
        public String firstName;
        public String lastName;
        public String email;

        public MockEvaluator(Long id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
    }

    // DTOs for request bodies
    public static class CreateGenericScheduleRequest {
        private Evaluation.EvaluationType evaluationType;
        private String gradeLevel;
        private String subject;
        private Long evaluatorId;
        private LocalDateTime scheduledDate;
        private Integer durationMinutes;
        private String location;
        private String instructions;

        // Getters y setters
        public Evaluation.EvaluationType getEvaluationType() { return evaluationType; }
        public void setEvaluationType(Evaluation.EvaluationType evaluationType) { this.evaluationType = evaluationType; }
        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public Long getEvaluatorId() { return evaluatorId; }
        public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }
        public LocalDateTime getScheduledDate() { return scheduledDate; }
        public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
    }

    public static class CreateIndividualScheduleRequest {
        private Long applicationId;
        private Evaluation.EvaluationType evaluationType;
        private Long evaluatorId;
        private LocalDateTime scheduledDate;
        private Integer durationMinutes;
        private String location;
        private String instructions;
        private boolean requiresConfirmation;
        private String attendeesRequired;

        // Getters y setters
        public Long getApplicationId() { return applicationId; }
        public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
        public Evaluation.EvaluationType getEvaluationType() { return evaluationType; }
        public void setEvaluationType(Evaluation.EvaluationType evaluationType) { this.evaluationType = evaluationType; }
        public Long getEvaluatorId() { return evaluatorId; }
        public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }
        public LocalDateTime getScheduledDate() { return scheduledDate; }
        public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        public boolean isRequiresConfirmation() { return requiresConfirmation; }
        public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }
        public String getAttendeesRequired() { return attendeesRequired; }
        public void setAttendeesRequired(String attendeesRequired) { this.attendeesRequired = attendeesRequired; }
    }

    public static class RescheduleRequest {
        private LocalDateTime newDate;
        private String reason;

        // Getters y setters
        public LocalDateTime getNewDate() { return newDate; }
        public void setNewDate(LocalDateTime newDate) { this.newDate = newDate; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}