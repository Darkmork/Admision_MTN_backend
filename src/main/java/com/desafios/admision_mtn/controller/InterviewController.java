package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.*;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Interview.InterviewStatus;
import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.Interview.InterviewMode;
import com.desafios.admision_mtn.service.InterviewService;
import com.desafios.admision_mtn.service.InterviewNotificationService;
import com.desafios.admision_mtn.service.PersonalizedEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;

@RestController
@RequestMapping("/api/interviews")
@Tag(name = "Interviews", description = "Sistema completo de gestión de entrevistas del proceso de admisión")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
@RequiredArgsConstructor
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewNotificationService notificationService;
    private final PersonalizedEmailService personalizedEmailService;
    private final JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    // CRUD básico
    @Operation(
        summary = "Crear nueva entrevista", 
        description = "Crea una nueva entrevista en el sistema. Solo administradores y directores de ciclo pueden crear entrevistas.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Entrevista creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterviewResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "id": 123,
                        "applicationId": 456,
                        "interviewerId": 78,
                        "type": "DIRECTOR_INTERVIEW",
                        "mode": "IN_PERSON",
                        "status": "SCHEDULED",
                        "scheduledDate": "2024-09-15",
                        "scheduledTime": "10:30:00",
                        "location": "Oficina Director",
                        "notes": "Entrevista inicial"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrevista inválidos o conflicto de horarios"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN o CYCLE_DIRECTOR"
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<InterviewResponse> createInterview(
        @Parameter(
            description = "Datos de la nueva entrevista",
            required = true,
            schema = @Schema(implementation = CreateInterviewRequest.class)
        )
        @Valid @RequestBody CreateInterviewRequest request) {
        log.info("POST /api/interviews - Creando nueva entrevista");
        InterviewResponse response = interviewService.createInterview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // ENDPOINTS ESPECÍFICOS - DEBEN ESTAR ANTES DE /{id}
    // ============================================================
    
    @GetMapping("/interviewers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> getAvailableInterviewers() {
        log.info("GET /api/interviews/interviewers - Obteniendo lista de entrevistadores disponibles");
        
        try {
            String query = """
                SELECT 
                    u.id,
                    CONCAT(u.first_name, ' ', u.last_name) as name,
                    u.role,
                    u.subject,
                    u.educational_level,
                    COUNT(s.id) as schedule_count
                FROM users u 
                LEFT JOIN interviewer_schedules s ON u.id = s.interviewer_id AND s.is_active = true
                WHERE u.role IN ('PSYCHOLOGIST', 'CYCLE_DIRECTOR', 'TEACHER', 'COORDINATOR')
                  AND u.active = true
                GROUP BY u.id, u.first_name, u.last_name, u.role, u.subject, u.educational_level
                HAVING COUNT(s.id) > 0
                ORDER BY u.role, u.first_name
            """;
            
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(query);
            log.info("🔍 DEBUG: Raw query returned {} results", rawResults.size());
            
            List<Map<String, Object>> interviewers = new ArrayList<>();
            for (Map<String, Object> row : rawResults) {
                log.info("🔍 DEBUG: Processing row: ID={}, Name={}, Role={}", row.get("id"), row.get("name"), row.get("role"));
                Map<String, Object> interviewer = new HashMap<>();
                interviewer.put("id", row.get("id"));
                interviewer.put("name", row.get("name"));
                interviewer.put("role", row.get("role"));
                interviewer.put("subject", row.get("subject"));
                interviewer.put("educationalLevel", row.get("educational_level"));
                interviewer.put("scheduleCount", row.get("schedule_count"));
                interviewers.add(interviewer);
            }
            
            log.info("Encontrados {} entrevistadores con horarios disponibles", interviewers.size());
            return ResponseEntity.ok(interviewers);
            
        } catch (Exception e) {
            log.error("Error obteniendo lista de entrevistadores: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error obteniendo entrevistadores: " + e.getMessage());
        }
    }
    
    // ENDPOINT PÚBLICO TEMPORAL PARA TESTING - REMOVER EN PRODUCCIÓN
    @GetMapping("/public/interviewers")
    public ResponseEntity<?> getAvailableInterviewersPublic(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time) {
        
        if (date != null && time != null) {
            log.info("GET /api/interviews/public/interviewers - Verificando disponibilidad para fecha {} hora {}", date, time);
            return getAvailableInterviewersForDateTime(date, time);
        } else {
            log.info("GET /api/interviews/public/interviewers - Obteniendo lista general de entrevistadores (PÚBLICO)");
            return getAllInterviewersWithSchedules();
        }
    }
    
    private ResponseEntity<?> getAllInterviewersWithSchedules() {
        try {
            String query = """
                SELECT 
                    u.id,
                    CONCAT(u.first_name, ' ', u.last_name) as name,
                    u.role,
                    u.subject,
                    u.educational_level,
                    COUNT(s.id) as schedule_count
                FROM users u 
                LEFT JOIN interviewer_schedules s ON u.id = s.interviewer_id AND s.is_active = true
                WHERE u.role IN ('PSYCHOLOGIST', 'CYCLE_DIRECTOR', 'TEACHER', 'COORDINATOR')
                  AND u.active = true
                GROUP BY u.id, u.first_name, u.last_name, u.role, u.subject, u.educational_level
                HAVING COUNT(s.id) > 0
                ORDER BY u.role, u.first_name
            """;
            
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(query);
            log.info("🔍 DEBUG: Raw query returned {} results", rawResults.size());
            
            List<Map<String, Object>> interviewers = new ArrayList<>();
            for (Map<String, Object> row : rawResults) {
                Map<String, Object> interviewer = new HashMap<>();
                interviewer.put("id", row.get("id"));
                interviewer.put("name", row.get("name"));
                interviewer.put("role", row.get("role"));
                interviewer.put("subject", row.get("subject"));
                interviewer.put("educationalLevel", row.get("educational_level"));
                interviewer.put("scheduleCount", row.get("schedule_count"));
                interviewers.add(interviewer);
            }
            
            log.info("Encontrados {} entrevistadores con horarios disponibles", interviewers.size());
            return ResponseEntity.ok(interviewers);
            
        } catch (Exception e) {
            log.error("Error obteniendo lista de entrevistadores: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error obteniendo entrevistadores: " + e.getMessage());
        }
    }
    
    private ResponseEntity<?> getAvailableInterviewersForDateTime(String date, String time) {
        try {
            // Convertir fecha y hora
            LocalDate requestedDate = LocalDate.parse(date);
            LocalTime requestedTime = LocalTime.parse(time);
            
            // Obtener día de la semana en inglés
            String dayOfWeek = requestedDate.getDayOfWeek().toString(); // MONDAY, TUESDAY, etc.
            
            log.info("🕐 Buscando entrevistadores disponibles para {} ({}) a las {}", date, dayOfWeek, time);
            
            String query = """
                SELECT DISTINCT
                    u.id,
                    CONCAT(u.first_name, ' ', u.last_name) as name,
                    u.role,
                    u.subject,
                    u.educational_level,
                    s.start_time,
                    s.end_time,
                    s.notes,
                    u.first_name
                FROM users u 
                JOIN interviewer_schedules s ON u.id = s.interviewer_id 
                WHERE u.role IN ('PSYCHOLOGIST', 'CYCLE_DIRECTOR', 'TEACHER', 'COORDINATOR')
                  AND u.active = true
                  AND s.is_active = true
                  AND s.year = ?
                  AND s.day_of_week = ?
                  AND s.start_time <= ?::time
                  AND s.end_time > ?::time
                ORDER BY u.role, u.first_name
            """;
            
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(
                query, 
                requestedDate.getYear(),  // año
                dayOfWeek,                // día de la semana
                time,                     // hora inicio
                time                      // hora fin  
            );
            
            log.info("🔍 DEBUG: Query returned {} available interviewers for {} at {}", rawResults.size(), date, time);
            
            List<Map<String, Object>> availableInterviewers = new ArrayList<>();
            for (Map<String, Object> row : rawResults) {
                log.info("🔍 AVAILABLE: ID={}, Name={}, Role={}, Schedule: {} - {}", 
                        row.get("id"), row.get("name"), row.get("role"), 
                        row.get("start_time"), row.get("end_time"));
                        
                Map<String, Object> interviewer = new HashMap<>();
                interviewer.put("id", row.get("id"));
                interviewer.put("name", row.get("name"));
                interviewer.put("role", row.get("role"));
                interviewer.put("subject", row.get("subject"));
                interviewer.put("educationalLevel", row.get("educational_level"));
                interviewer.put("availableFrom", row.get("start_time").toString());
                interviewer.put("availableTo", row.get("end_time").toString());
                interviewer.put("scheduleNotes", row.get("notes"));
                availableInterviewers.add(interviewer);
            }
            
            log.info("✅ Encontrados {} entrevistadores disponibles para {} a las {}", availableInterviewers.size(), date, time);
            return ResponseEntity.ok(availableInterviewers);
            
        } catch (Exception e) {
            log.error("❌ Error verificando disponibilidad para {} a las {}: {}", date, time, e.getMessage(), e);
            return ResponseEntity.status(500).body("Error verificando disponibilidad: " + e.getMessage());
        }
    }
    
    @GetMapping("/interviewer-availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> getInterviewerAvailability(
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("GET /api/interviews/interviewer-availability - Obteniendo disponibilidad del entrevistador {} entre {} y {}", 
                 interviewerId, startDate, endDate);
        
        try {
            // Obtener horarios reales del entrevistador desde la BD
            String scheduleQuery = """
                SELECT 
                    s.day_of_week, 
                    s.start_time, 
                    s.end_time,
                    u.first_name || ' ' || u.last_name as interviewer_name
                FROM interviewer_schedules s
                JOIN users u ON s.interviewer_id = u.id
                WHERE s.interviewer_id = ? 
                  AND s.is_active = true
                  AND s.year = ?
                ORDER BY 
                  CASE s.day_of_week 
                    WHEN 'MONDAY' THEN 1
                    WHEN 'TUESDAY' THEN 2  
                    WHEN 'WEDNESDAY' THEN 3
                    WHEN 'THURSDAY' THEN 4
                    WHEN 'FRIDAY' THEN 5
                    WHEN 'SATURDAY' THEN 6
                    WHEN 'SUNDAY' THEN 7
                  END,
                  s.start_time
            """;
            
            List<Map<String, Object>> schedules = jdbcTemplate.queryForList(scheduleQuery, interviewerId, startDate.getYear());
            
            if (schedules.isEmpty()) {
                return ResponseEntity.ok(List.of(Map.of(
                    "message", "No hay horarios configurados para este entrevistador",
                    "interviewerId", interviewerId,
                    "availability", List.of()
                )));
            }
            
            // Organizar horarios por día de la semana
            Map<String, List<Map<String, Object>>> schedulesByDay = new HashMap<>();
            for (Map<String, Object> schedule : schedules) {
                String dayOfWeek = (String) schedule.get("day_of_week");
                schedulesByDay.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(schedule);
            }
            
            List<Map<String, Object>> availability = new ArrayList<>();
            LocalDate current = startDate;
            
            while (!current.isAfter(endDate)) {
                String dayName = current.getDayOfWeek().name(); // MONDAY, TUESDAY, etc.
                
                Map<String, Object> dayAvailability = new HashMap<>();
                dayAvailability.put("date", current.toString());
                dayAvailability.put("dayOfWeek", dayName);
                
                List<Map<String, Object>> daySchedules = schedulesByDay.get(dayName);
                if (daySchedules != null && !daySchedules.isEmpty()) {
                    dayAvailability.put("available", true);
                    
                    List<Map<String, Object>> slots = new ArrayList<>();
                    for (Map<String, Object> schedule : daySchedules) {
                        Map<String, Object> slot = new HashMap<>();
                        slot.put("startTime", schedule.get("start_time").toString().substring(0, 5)); // HH:MM format
                        slot.put("endTime", schedule.get("end_time").toString().substring(0, 5));
                        slot.put("available", true);
                        slots.add(slot);
                    }
                    dayAvailability.put("slots", slots);
                } else {
                    dayAvailability.put("available", false);
                    dayAvailability.put("slots", List.of());
                }
                
                availability.add(dayAvailability);
                current = current.plusDays(1);
            }
            
            log.info("Encontrados {} días con disponibilidad para entrevistador {}", availability.size(), interviewerId);
            return ResponseEntity.ok(availability);
            
        } catch (Exception e) {
            log.error("Error obteniendo disponibilidad del entrevistador: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error obteniendo disponibilidad: " + e.getMessage());
        }
    }
    
    @GetMapping("/available-slots")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> getAvailableTimeSlots(
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "60") int duration) {
        
        log.info("GET /api/interviews/available-slots - Obteniendo slots disponibles para entrevistador {} el {} (duración: {}min)", 
                 interviewerId, date, duration);
        
        try {
            String dayOfWeek = date.getDayOfWeek().name(); // MONDAY, TUESDAY, etc.
            
            // Obtener horarios del entrevistador para ese día específico
            String scheduleQuery = """
                SELECT 
                    s.start_time, 
                    s.end_time
                FROM interviewer_schedules s
                WHERE s.interviewer_id = ? 
                  AND s.day_of_week = ?
                  AND s.is_active = true
                  AND s.year = ?
                ORDER BY s.start_time
            """;
            
            List<Map<String, Object>> schedules = jdbcTemplate.queryForList(scheduleQuery, interviewerId, dayOfWeek, date.getYear());
            
            if (schedules.isEmpty()) {
                return ResponseEntity.ok(List.of(Map.of(
                    "message", "No hay horarios configurados para este entrevistador en " + dayOfWeek,
                    "date", date.toString(),
                    "slots", List.of()
                )));
            }
            
            // Obtener entrevistas ya programadas para ese día
            String conflictsQuery = """
                SELECT 
                    DATE_PART('hour', scheduled_date) as hour,
                    DATE_PART('minute', scheduled_date) as minute,
                    duration_minutes
                FROM interviews
                WHERE interviewer_id = ? 
                  AND DATE(scheduled_date) = ?
                  AND status NOT IN ('CANCELLED', 'COMPLETED')
            """;
            
            List<Map<String, Object>> existingInterviews = jdbcTemplate.queryForList(conflictsQuery, interviewerId, date);
            
            List<Map<String, Object>> slots = new ArrayList<>();
            
            // Para cada bloque de horario disponible del entrevistador
            for (Map<String, Object> schedule : schedules) {
                String startTimeStr = schedule.get("start_time").toString();
                String endTimeStr = schedule.get("end_time").toString();
                
                // Parse times
                String[] startParts = startTimeStr.split(":");
                String[] endParts = endTimeStr.split(":");
                
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                
                // Crear slots disponibles cada 30 minutos dentro del horario
                int currentHour = startHour;
                int currentMinute = startMinute;
                
                while (currentHour < endHour || (currentHour == endHour && currentMinute < endMinute)) {
                    String timeSlot = String.format("%02d:%02d", currentHour, currentMinute);
                    
                    // Calcular tiempo de fin
                    int endSlotMinute = currentMinute + duration;
                    int endSlotHour = currentHour;
                    if (endSlotMinute >= 60) {
                        endSlotHour++;
                        endSlotMinute -= 60;
                    }
                    
                    String endTimeSlot = String.format("%02d:%02d", endSlotHour, endSlotMinute);
                    
                    // Verificar que el slot completo esté dentro del horario disponible
                    boolean withinSchedule = (endSlotHour < endHour || 
                                            (endSlotHour == endHour && endSlotMinute <= endMinute));
                    
                    // Verificar conflictos con entrevistas existentes
                    boolean hasConflict = false;
                    for (Map<String, Object> interview : existingInterviews) {
                        int interviewHour = ((Number) interview.get("hour")).intValue();
                        int interviewMinute = ((Number) interview.get("minute")).intValue();
                        int interviewDuration = ((Number) interview.get("duration_minutes")).intValue();
                        
                        // Verificar solapamiento
                        if (currentHour == interviewHour && Math.abs(currentMinute - interviewMinute) < Math.max(duration, interviewDuration)) {
                            hasConflict = true;
                            break;
                        }
                    }
                    
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", timeSlot);
                    slot.put("endTime", endTimeSlot);
                    slot.put("available", withinSchedule && !hasConflict);
                    slot.put("reason", hasConflict ? "Ocupado" : (!withinSchedule ? "Fuera de horario" : "Disponible"));
                    
                    slots.add(slot);
                    
                    // Avanzar 30 minutos
                    currentMinute += 30;
                    if (currentMinute >= 60) {
                        currentHour++;
                        currentMinute -= 60;
                    }
                }
            }
            
            log.info("Generados {} slots para entrevistador {} el {}", slots.size(), interviewerId, date);
            return ResponseEntity.ok(slots);
            
        } catch (Exception e) {
            log.error("Error obteniendo slots disponibles: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error obteniendo slots: " + e.getMessage());
        }
    }
    
    @GetMapping("/validate-slot")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    public ResponseEntity<?> validateTimeSlot(
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String time,
            @RequestParam(defaultValue = "60") int duration) {
        
        log.info("GET /api/interviews/validate-slot - Validando slot para entrevistador {} el {} a las {} (duración: {}min)", 
                 interviewerId, date, time, duration);
        
        try {
            String dayOfWeek = date.getDayOfWeek().name();
            String[] timeParts = time.split(":");
            int requestHour = Integer.parseInt(timeParts[0]);
            int requestMinute = Integer.parseInt(timeParts[1]);
            
            // Verificar si el entrevistador tiene horarios configurados para ese día
            String scheduleQuery = """
                SELECT 
                    s.start_time, 
                    s.end_time,
                    u.first_name || ' ' || u.last_name as interviewer_name
                FROM interviewer_schedules s
                JOIN users u ON s.interviewer_id = u.id
                WHERE s.interviewer_id = ? 
                  AND s.day_of_week = ?
                  AND s.is_active = true
                  AND s.year = ?
            """;
            
            List<Map<String, Object>> schedules = jdbcTemplate.queryForList(scheduleQuery, interviewerId, dayOfWeek, date.getYear());
            
            Map<String, Object> validation = new HashMap<>();
            validation.put("interviewerId", interviewerId);
            validation.put("date", date.toString());
            validation.put("time", time);
            validation.put("duration", duration);
            validation.put("dayOfWeek", dayOfWeek);
            
            if (schedules.isEmpty()) {
                validation.put("valid", false);
                validation.put("message", "El entrevistador no tiene horarios configurados para " + dayOfWeek);
                validation.put("reason", "NO_SCHEDULE");
                return ResponseEntity.ok(validation);
            }
            
            // Verificar si el horario solicitado está dentro de alguno de los bloques disponibles
            boolean withinSchedule = false;
            String scheduleDetails = "";
            
            for (Map<String, Object> schedule : schedules) {
                String startTimeStr = schedule.get("start_time").toString();
                String endTimeStr = schedule.get("end_time").toString();
                
                String[] startParts = startTimeStr.split(":");
                String[] endParts = endTimeStr.split(":");
                
                int startHour = Integer.parseInt(startParts[0]);
                int startMinute = Integer.parseInt(startParts[1]);
                int endHour = Integer.parseInt(endParts[0]);
                int endMinute = Integer.parseInt(endParts[1]);
                
                // Calcular tiempo de fin de la entrevista
                int endInterviewMinute = requestMinute + duration;
                int endInterviewHour = requestHour;
                if (endInterviewMinute >= 60) {
                    endInterviewHour++;
                    endInterviewMinute -= 60;
                }
                
                // Verificar que tanto el inicio como el fin estén dentro del horario
                boolean startWithin = (requestHour > startHour || (requestHour == startHour && requestMinute >= startMinute));
                boolean endWithin = (endInterviewHour < endHour || (endInterviewHour == endHour && endInterviewMinute <= endMinute));
                
                if (startWithin && endWithin) {
                    withinSchedule = true;
                    scheduleDetails = String.format("%s - %s", startTimeStr.substring(0, 5), endTimeStr.substring(0, 5));
                    break;
                }
            }
            
            if (!withinSchedule) {
                validation.put("valid", false);
                validation.put("message", "El horario solicitado está fuera de los horarios disponibles del entrevistador");
                validation.put("reason", "OUTSIDE_SCHEDULE");
                
                // Agregar horarios disponibles para referencia
                List<String> availableSchedules = schedules.stream()
                    .map(s -> s.get("start_time").toString().substring(0, 5) + " - " + s.get("end_time").toString().substring(0, 5))
                    .toList();
                validation.put("availableSchedules", availableSchedules);
                return ResponseEntity.ok(validation);
            }
            
            // Verificar conflictos con entrevistas existentes
            String conflictQuery = """
                SELECT 
                    i.id,
                    i.scheduled_date,
                    i.duration_minutes,
                    s.first_name || ' ' || s.paternal_last_name as student_name
                FROM interviews i
                JOIN applications a ON i.application_id = a.id
                JOIN students s ON a.student_id = s.id
                WHERE i.interviewer_id = ? 
                  AND DATE(i.scheduled_date) = ?
                  AND i.status NOT IN ('CANCELLED', 'COMPLETED')
            """;
            
            List<Map<String, Object>> conflicts = jdbcTemplate.queryForList(conflictQuery, interviewerId, date);
            
            for (Map<String, Object> interview : conflicts) {
                java.sql.Timestamp scheduledDate = (java.sql.Timestamp) interview.get("scheduled_date");
                int interviewDuration = ((Number) interview.get("duration_minutes")).intValue();
                
                java.time.LocalDateTime interviewDateTime = scheduledDate.toLocalDateTime();
                int interviewHour = interviewDateTime.getHour();
                int interviewMinute = interviewDateTime.getMinute();
                
                // Verificar solapamiento
                int interviewEndMinute = interviewMinute + interviewDuration;
                int interviewEndHour = interviewHour;
                if (interviewEndMinute >= 60) {
                    interviewEndHour++;
                    interviewEndMinute -= 60;
                }
                
                int requestEndMinute = requestMinute + duration;
                int requestEndHour = requestHour;
                if (requestEndMinute >= 60) {
                    requestEndHour++;
                    requestEndMinute -= 60;
                }
                
                // Verificar si hay solapamiento
                boolean hasOverlap = !(requestEndHour < interviewHour || 
                                     (requestEndHour == interviewHour && requestEndMinute <= interviewMinute) ||
                                     requestHour > interviewEndHour ||
                                     (requestHour == interviewEndHour && requestMinute >= interviewEndMinute));
                
                if (hasOverlap) {
                    validation.put("valid", false);
                    validation.put("message", "El horario está ocupado con otra entrevista");
                    validation.put("reason", "CONFLICT");
                    validation.put("conflictingInterview", Map.of(
                        "studentName", interview.get("student_name"),
                        "time", String.format("%02d:%02d", interviewHour, interviewMinute),
                        "duration", interviewDuration
                    ));
                    return ResponseEntity.ok(validation);
                }
            }
            
            // Si llegamos aquí, el horario es válido
            validation.put("valid", true);
            validation.put("message", "Horario disponible");
            validation.put("reason", "AVAILABLE");
            validation.put("scheduleBlock", scheduleDetails);
            
            return ResponseEntity.ok(validation);
            
        } catch (Exception e) {
            log.error("Error validando slot de tiempo: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error validando slot: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Obtener entrevista por ID", 
        description = "Obtiene los detalles completos de una entrevista específica. Accesible para profesores y administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Detalles de la entrevista",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterviewResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Entrevista no encontrada"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<InterviewResponse> getInterviewById(
        @Parameter(description = "ID de la entrevista", required = true, example = "123")
        @PathVariable Long id) {
        log.info("GET /api/interviews/{} - Obteniendo entrevista por ID", id);
        InterviewResponse response = interviewService.getInterviewById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtener todas las entrevistas", 
        description = "Obtiene lista paginada de entrevistas con búsqueda y ordenamiento opcionales.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista paginada de entrevistas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterviewResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado"
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('TEACHER_LANGUAGE') or hasRole('TEACHER_MATHEMATICS') or hasRole('TEACHER_ENGLISH') or hasRole('PSYCHOLOGIST')")
    public ResponseEntity<Page<InterviewResponse>> getAllInterviews(
        @Parameter(description = "Número de página (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Tamaño de página", example = "20")
        @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Campo para ordenar", example = "scheduledDate")
        @RequestParam(defaultValue = "scheduledDate") String sortBy,
        @Parameter(description = "Dirección del ordenamiento", example = "desc")
        @RequestParam(defaultValue = "desc") String sortDir,
        @Parameter(description = "Término de búsqueda", example = "Juan")
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
            status, type, interviewerId, startDate, endDate, pageable);
        
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
    @Operation(
        summary = "Obtener estadísticas de entrevistas", 
        description = "Obtiene estadísticas completas del sistema de entrevistas: totales, por estado, por tipo, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estadísticas del sistema de entrevistas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InterviewStatsResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "totalInterviews": 150,
                        "scheduledInterviews": 45,
                        "completedInterviews": 80,
                        "cancelledInterviews": 15,
                        "noShowInterviews": 10,
                        "interviewsByType": {
                            "DIRECTOR_INTERVIEW": 75,
                            "PSYCHOLOGICAL_INTERVIEW": 75
                        },
                        "averageDurationMinutes": 45,
                        "upcomingThisWeek": 12
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN o CYCLE_DIRECTOR"
        )
    })
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
                    // Usar el servicio de emails personalizados
                    personalizedEmailService.sendPersonalizedInterviewNotification(getInterviewEntity(id));
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

    // ENDPOINT TEMPORAL PÚBLICO PARA DEBUG - CONEXIÓN DIRECTA A BD
    @GetMapping("/public/all")
    public ResponseEntity<?> getAllInterviewsPublic() {
        log.info("GET /api/interviews/public/all - Test conexión directa a BD");
        try {
            // Usar JdbcTemplate directamente como hace ApplicationService (que funciona)
            String databaseName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            log.info("JdbcTemplate database name: {}", databaseName);
            
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'interviews'", 
                Integer.class);
            log.info("JdbcTemplate - Table interviews exists: {}", tableExists);
            
            Integer totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM interviews", Integer.class);
            log.info("JdbcTemplate finds {} interviews in database", totalCount);
            
            // También verificar applications para comparar
            Integer appsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Integer.class);
            log.info("JdbcTemplate finds {} applications in database", appsCount);
            
            // Obtener los datos usando JdbcTemplate
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(
                "SELECT i.id, i.application_id, i.status, i.scheduled_date " +
                "FROM interviews i " +
                "ORDER BY i.id LIMIT 10");
                
            log.info("JdbcTemplate retrieved {} interview records", rawResults.size());
            
            // Crear respuesta simple a partir de los datos de JdbcTemplate
            List<Map<String, Object>> simpleResponse = new ArrayList<>();
            for (Map<String, Object> row : rawResults) {
                Map<String, Object> interview = new HashMap<>();
                interview.put("id", row.get("id"));
                interview.put("applicationId", row.get("application_id"));
                interview.put("status", row.get("status"));
                interview.put("scheduledDate", row.get("scheduled_date"));
                simpleResponse.add(interview);
            }
            
            // Respuesta con información de debug completa
            Map<String, Object> debugResponse = new HashMap<>();
            debugResponse.put("databaseName", databaseName);
            debugResponse.put("tableExists", tableExists);
            debugResponse.put("totalCount", totalCount);
            debugResponse.put("applicationsCount", appsCount);  // Para comparar
            debugResponse.put("returnedRecords", rawResults.size());
            debugResponse.put("interviews", simpleResponse);
            
            return ResponseEntity.ok(debugResponse);
        } catch (Exception e) {
            log.error("Error en consulta directa: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "message", "Error al consultar entrevistas directamente"
            ));
        }
    }

    // ENDPOINT PÚBLICO MEJORADO PARA FRONTEND - DATOS COMPLETOS DE ENTREVISTAS
    @GetMapping("/public/complete")
    public ResponseEntity<?> getAllInterviewsComplete() {
        log.info("GET /api/interviews/public/complete - Obtener entrevistas con datos completos");
        try {
            // Query que hace JOIN para obtener toda la información del estudiante
            String completeQuery = """
                SELECT 
                    i.id,
                    i.application_id,
                    i.status,
                    i.interview_type as type,
                    i.scheduled_date,
                    i.duration_minutes,
                    i.location,
                    i.notes,
                    i.evaluation_notes,
                    i.recommendation,
                    s.first_name || ' ' || s.paternal_last_name || ' ' || s.maternal_last_name as student_name,
                    s.grade_applied,
                    COALESCE(p1.full_name, 'N/A') || ' y ' || COALESCE(p2.full_name, 'N/A') as parent_names,
                    u.first_name || ' ' || u.last_name as interviewer_name,
                    u.id as interviewer_id
                FROM interviews i
                JOIN applications a ON i.application_id = a.id
                JOIN students s ON a.student_id = s.id
                LEFT JOIN parents p1 ON a.father_id = p1.id
                LEFT JOIN parents p2 ON a.mother_id = p2.id
                JOIN users u ON i.interviewer_id = u.id
                ORDER BY i.scheduled_date DESC
            """;
            
            List<Map<String, Object>> rawResults = jdbcTemplate.queryForList(completeQuery);
            log.info("Query retrieved {} complete interview records", rawResults.size());
            
            // Mapear a formato esperado por el frontend
            List<Map<String, Object>> interviews = new ArrayList<>();
            for (Map<String, Object> row : rawResults) {
                Map<String, Object> interview = new HashMap<>();
                interview.put("id", row.get("id"));
                interview.put("applicationId", row.get("application_id"));
                interview.put("studentName", row.get("student_name"));
                interview.put("parentNames", row.get("parent_names"));
                interview.put("gradeApplied", row.get("grade_applied"));
                interview.put("interviewerId", row.get("interviewer_id"));
                interview.put("interviewerName", row.get("interviewer_name"));
                interview.put("status", row.get("status"));
                interview.put("type", row.get("type") != null ? row.get("type") : "FAMILY");
                interview.put("mode", "IN_PERSON"); // Default por ahora
                
                // Formatear fecha para el frontend
                if (row.get("scheduled_date") != null) {
                    java.sql.Timestamp timestamp = (java.sql.Timestamp) row.get("scheduled_date");
                    java.time.LocalDateTime localDateTime = timestamp.toLocalDateTime();
                    interview.put("scheduledDate", localDateTime.toLocalDate().toString());
                    interview.put("scheduledTime", localDateTime.toLocalTime().toString().substring(0, 5));
                }
                
                interview.put("duration", row.get("duration_minutes") != null ? row.get("duration_minutes") : 60);
                interview.put("location", row.get("location"));
                interview.put("virtualMeetingLink", "");
                interview.put("notes", row.get("notes"));
                interview.put("preparation", "");
                interview.put("result", null);
                interview.put("score", null);
                interview.put("recommendations", row.get("recommendation"));
                interview.put("followUpRequired", false);
                interview.put("followUpNotes", "");
                java.sql.Timestamp timestamp = (java.sql.Timestamp) row.get("scheduled_date");
                interview.put("createdAt", timestamp != null ? timestamp.toString() : "");
                interview.put("updatedAt", timestamp != null ? timestamp.toString() : "");
                interview.put("completedAt", null);
                interview.put("isUpcoming", true);
                interview.put("isOverdue", false);
                interview.put("canBeCompleted", true);
                interview.put("canBeEdited", true);
                interview.put("canBeCancelled", true);
                
                interviews.add(interview);
            }
            
            // Respuesta en formato esperado por el servicio
            Map<String, Object> response = new HashMap<>();
            response.put("content", interviews);
            response.put("totalElements", interviews.size());
            response.put("totalPages", 1);
            response.put("number", 0);
            response.put("size", interviews.size());
            response.put("first", true);
            response.put("last", true);
            
            log.info("Returning {} complete interviews to frontend", interviews.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en consulta de entrevistas completas: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "message", "Error al consultar entrevistas completas",
                "content", List.of(),
                "totalElements", 0,
                "totalPages", 0
            ));
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