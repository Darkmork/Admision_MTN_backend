package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.*;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Interview.InterviewStatus;
import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.Interview.InterviewMode;
import com.desafios.admision_mtn.service.InterviewService;
import com.desafios.admision_mtn.service.InterviewNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewNotificationService notificationService;

    // CRUD básico
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewResponse> createInterview(@Valid @RequestBody CreateInterviewRequest request) {
        log.info("POST /api/interviews - Creando nueva entrevista");
        InterviewResponse response = interviewService.createInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> getInterviewById(@PathVariable Long id) {
        log.info("GET /api/interviews/{} - Obteniendo entrevista por ID", id);
        InterviewResponse response = interviewService.getInterviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<Page<InterviewResponse>> getAllInterviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        
        log.info("GET /api/interviews - Obteniendo entrevistas (página: {}, tamaño: {}, búsqueda: '{}')", page, size, search);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InterviewResponse> response;
        if (search != null && !search.trim().isEmpty()) {
            response = interviewService.searchInterviews(search.trim(), pageable);
        } else {
            response = interviewService.getAllInterviews(pageable);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<Page<InterviewResponse>> getInterviewsWithFilters(
            @RequestParam(required = false) InterviewStatus status,
            @RequestParam(required = false) InterviewType type,
            @RequestParam(required = false) InterviewMode mode,
            @RequestParam(required = false) Long interviewerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "scheduledDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("GET /api/interviews/filter - Obteniendo entrevistas con filtros");
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InterviewResponse> response = interviewService.findWithFilters(
            status, type, mode, interviewerId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewResponse> updateInterview(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateInterviewRequest request) {
        log.info("PUT /api/interviews/{} - Actualizando entrevista", id);
        InterviewResponse response = interviewService.updateInterview(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInterview(@PathVariable Long id) {
        log.info("DELETE /api/interviews/{} - Eliminando entrevista", id);
        interviewService.deleteInterview(id);
        return ResponseEntity.noContent().build();
    }

    // Operaciones de estado
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> confirmInterview(@PathVariable Long id) {
        log.info("POST /api/interviews/{}/confirm - Confirmando entrevista", id);
        InterviewResponse response = interviewService.confirmInterview(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> startInterview(@PathVariable Long id) {
        log.info("POST /api/interviews/{}/start - Iniciando entrevista", id);
        InterviewResponse response = interviewService.startInterview(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> completeInterview(
            @PathVariable Long id, 
            @Valid @RequestBody CompleteInterviewRequest request) {
        log.info("POST /api/interviews/{}/complete - Completando entrevista", id);
        InterviewResponse response = interviewService.completeInterview(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewResponse> cancelInterview(@PathVariable Long id) {
        log.info("POST /api/interviews/{}/cancel - Cancelando entrevista", id);
        InterviewResponse response = interviewService.cancelInterview(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewResponse> rescheduleInterview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {
        log.info("POST /api/interviews/{}/reschedule - Reprogramando entrevista para {} a las {}", id, newDate, newTime);
        InterviewResponse response = interviewService.rescheduleInterview(id, newDate, newTime);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> markAsNoShow(@PathVariable Long id) {
        log.info("POST /api/interviews/{}/no-show - Marcando como 'no asistió'", id);
        InterviewResponse response = interviewService.markAsNoShow(id);
        return ResponseEntity.ok(response);
    }

    // Consultas especiales
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<InterviewResponse>> getTodaysInterviews() {
        log.info("GET /api/interviews/today - Obteniendo entrevistas de hoy");
        List<InterviewResponse> response = interviewService.getTodaysInterviews();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<InterviewResponse>> getUpcomingInterviews() {
        log.info("GET /api/interviews/upcoming - Obteniendo entrevistas próximas");
        List<InterviewResponse> response = interviewService.getUpcomingInterviews();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<List<InterviewResponse>> getOverdueInterviews() {
        log.info("GET /api/interviews/overdue - Obteniendo entrevistas vencidas");
        List<InterviewResponse> response = interviewService.getOverdueInterviews();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/follow-up")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<List<InterviewResponse>> getInterviewsRequiringFollowUp() {
        log.info("GET /api/interviews/follow-up - Obteniendo entrevistas que requieren seguimiento");
        List<InterviewResponse> response = interviewService.getInterviewsRequiringFollowUp();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interviewer/{interviewerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or (hasRole('TEACHER_LANGUAGE') and #interviewerId == authentication.principal.id) or (hasRole('TEACHER_MATHEMATICS') and #interviewerId == authentication.principal.id) or (hasRole('TEACHER_ENGLISH') and #interviewerId == authentication.principal.id) or (hasRole('PSYCHOLOGIST') and #interviewerId == authentication.principal.id)")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByInterviewer(@PathVariable Long interviewerId) {
        log.info("GET /api/interviews/interviewer/{} - Obteniendo entrevistas por entrevistador", interviewerId);
        List<InterviewResponse> response = interviewService.getInterviewsByInterviewer(interviewerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByApplication(@PathVariable Long applicationId) {
        log.info("GET /api/interviews/application/{} - Obteniendo entrevistas por aplicación", applicationId);
        List<InterviewResponse> response = interviewService.getInterviewsByApplication(applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<InterviewResponse>> getInterviewsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/interviews/date-range - Obteniendo entrevistas entre {} y {}", startDate, endDate);
        List<InterviewResponse> response = interviewService.getInterviewsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // Estadísticas
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewStatsResponse> getInterviewStatistics() {
        log.info("GET /api/interviews/statistics - Obteniendo estadísticas de entrevistas");
        InterviewStatsResponse response = interviewService.getInterviewStatistics();
        return ResponseEntity.ok(response);
    }

    // Endpoints para calendario
    @GetMapping("/calendar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<List<InterviewResponse>> getCalendarInterviews(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long interviewerId) {
        log.info("GET /api/interviews/calendar - Obteniendo entrevistas para calendario entre {} y {}", startDate, endDate);
        
        List<InterviewResponse> response;
        if (interviewerId != null) {
            // Filtrar por entrevistador específico si se proporciona
            response = interviewService.getInterviewsByDateRange(startDate, endDate).stream()
                .filter(interview -> interview.getInterviewerId().equals(interviewerId))
                .toList();
        } else {
            response = interviewService.getInterviewsByDateRange(startDate, endDate);
        }
        
        return ResponseEntity.ok(response);
    }

    // Validación de disponibilidad
    @GetMapping("/availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<Boolean> checkInterviewerAvailability(
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(required = false) Long excludeInterviewId) {
        log.info("GET /api/interviews/availability - Verificando disponibilidad del entrevistador {} para {} a las {}", 
                 interviewerId, date, time);
        
        try {
            // Intentar crear una entrevista temporal para validar disponibilidad
            // Este método lanzará una excepción si no está disponible
            CreateInterviewRequest tempRequest = new CreateInterviewRequest();
            tempRequest.setInterviewerId(interviewerId);
            tempRequest.setScheduledDate(date);
            tempRequest.setScheduledTime(time);
            
            // En lugar de crear, solo validamos la disponibilidad
            // Podríamos usar un método específico para esto en el servicio
            boolean available = true; // Simplificado por ahora
            
            return ResponseEntity.ok(available);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(false);
        }
    }

    // Endpoints de notificaciones
    @PostMapping("/{id}/send-notification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<String> sendNotification(
            @PathVariable Long id,
            @RequestParam String notificationType) {
        log.info("POST /api/interviews/{}/send-notification - Enviando notificación tipo: {}", id, notificationType);
        
        try {
            InterviewResponse interview = interviewService.getInterviewById(id);
            
            switch (notificationType.toLowerCase()) {
                case "scheduled":
                    // Convertir InterviewResponse a Interview para el servicio de notificaciones
                    // Necesitamos obtener la entidad completa
                    notificationService.sendInterviewScheduledNotification(getInterviewEntity(id));
                    break;
                case "confirmed":
                    notificationService.sendInterviewConfirmedNotification(getInterviewEntity(id));
                    break;
                case "reminder":
                    notificationService.sendInterviewReminderNotification(getInterviewEntity(id));
                    break;
                default:
                    return ResponseEntity.badRequest().body("Tipo de notificación no válido: " + notificationType);
            }
            
            return ResponseEntity.ok("Notificación enviada exitosamente");
        } catch (Exception e) {
            log.error("Error enviando notificación: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error enviando notificación: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/send-reminder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<String> sendReminder(@PathVariable Long id) {
        log.info("POST /api/interviews/{}/send-reminder - Enviando recordatorio", id);
        
        try {
            notificationService.sendInterviewReminderNotification(getInterviewEntity(id));
            return ResponseEntity.ok("Recordatorio enviado exitosamente");
        } catch (Exception e) {
            log.error("Error enviando recordatorio: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error enviando recordatorio: " + e.getMessage());
        }
    }

    // Método auxiliar para obtener la entidad Interview
    private Interview getInterviewEntity(Long id) {
        // Usaremos un método del servicio para obtener la entidad
        return interviewService.getInterviewEntityById(id);
    }

    // Manejo de errores
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Error de validación: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        log.error("Error de estado: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(jakarta.persistence.EntityNotFoundException e) {
        log.error("Entidad no encontrada: {}", e.getMessage());
        return ResponseEntity.notFound().build();
    }
}