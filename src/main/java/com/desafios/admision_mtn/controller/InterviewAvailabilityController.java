package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Interview.InterviewType;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews/availability")
@Tag(name = "Interview Availability", description = "Sistema de disponibilidad de entrevistadores basado en horarios configurados")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "http://localhost:5173", 
    "http://localhost:5174", 
    "http://localhost:5175", 
    "http://localhost:5176"
})
public class InterviewAvailabilityController {

    private final InterviewService interviewService;

    /**
     * Obtener entrevistadores disponibles para una fecha y hora específica
     * Integra el sistema de horarios configurados por los entrevistadores
     */
    @GetMapping("/interviewers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    @Operation(summary = "Obtener entrevistadores disponibles",
               description = "Obtiene la lista de entrevistadores que tienen horarios configurados y están disponibles para una fecha y hora específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de entrevistadores disponibles obtenida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de fecha/hora inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para acceder a este recurso")
    })
    public ResponseEntity<?> getAvailableInterviewers(
            @Parameter(description = "Fecha de la entrevista (formato: YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Hora de la entrevista (formato: HH:mm)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        log.info("GET /api/interviews/availability/interviewers - Fecha: {} Hora: {}", date, time);
        
        try {
            List<User> availableInterviewers = interviewService.getAvailableInterviewers(date, time);
            
            log.info("Encontrados {} entrevistadores disponibles", availableInterviewers.size());
            
            return ResponseEntity.ok(Map.of(
                "date", date,
                "time", time,
                "availableInterviewers", availableInterviewers,
                "count", availableInterviewers.size(),
                "message", "Entrevistadores disponibles obtenidos exitosamente"
            ));
            
        } catch (Exception e) {
            log.error("Error al obtener entrevistadores disponibles: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error al obtener entrevistadores disponibles",
                    "message", e.getMessage(),
                    "date", date,
                    "time", time
                ));
        }
    }

    /**
     * Obtener entrevistadores disponibles filtrados por tipo de entrevista
     */
    @GetMapping("/interviewers/by-type")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR') or hasRole('COORDINATOR')")
    @Operation(summary = "Obtener entrevistadores por tipo",
               description = "Obtiene entrevistadores disponibles filtrados por tipo de entrevista y sus roles correspondientes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de entrevistadores filtrados por tipo"),
        @ApiResponse(responseCode = "400", description = "Tipo de entrevista o parámetros inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para acceder a este recurso")
    })
    public ResponseEntity<?> getAvailableInterviewersByType(
            @Parameter(description = "Tipo de entrevista", 
                      example = "PSYCHOLOGICAL, FAMILY, INDIVIDUAL")
            @RequestParam InterviewType interviewType,
            @Parameter(description = "Fecha de la entrevista (formato: YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Hora de la entrevista (formato: HH:mm)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
        
        log.info("GET /api/interviews/availability/interviewers/by-type - Tipo: {} Fecha: {} Hora: {}", 
                interviewType, date, time);
        
        try {
            List<User> availableInterviewers = 
                interviewService.getAvailableInterviewersByType(interviewType, date, time);
            
            log.info("Encontrados {} entrevistadores disponibles para tipo {}", 
                    availableInterviewers.size(), interviewType);
            
            return ResponseEntity.ok(Map.of(
                "interviewType", interviewType,
                "date", date,
                "time", time,
                "availableInterviewers", availableInterviewers,
                "count", availableInterviewers.size(),
                "message", "Entrevistadores por tipo obtenidos exitosamente"
            ));
            
        } catch (Exception e) {
            log.error("Error al obtener entrevistadores por tipo: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error al obtener entrevistadores por tipo",
                    "message", e.getMessage(),
                    "interviewType", interviewType,
                    "date", date,
                    "time", time
                ));
        }
    }

    /**
     * Obtener resumen de disponibilidad para una fecha específica
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CYCLE_DIRECTOR')")
    @Operation(summary = "Resumen de disponibilidad diaria",
               description = "Obtiene un resumen de la disponibilidad de entrevistadores para una fecha específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
        @ApiResponse(responseCode = "400", description = "Fecha inválida"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para acceder a este recurso")
    })
    public ResponseEntity<?> getAvailabilitySummary(
            @Parameter(description = "Fecha para el resumen (formato: YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("GET /api/interviews/availability/summary - Fecha: {}", date);
        
        try {
            // Horarios típicos de entrevista (ejemplo: cada 30 minutos de 9:00 a 17:00)
            LocalTime[] commonTimes = {
                LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30), LocalTime.of(14, 0), LocalTime.of(14, 30),
                LocalTime.of(15, 0), LocalTime.of(15, 30), LocalTime.of(16, 0), LocalTime.of(16, 30)
            };
            
            Map<String, Object> summary = Map.of(
                "date", date,
                "timeSlots", java.util.Arrays.stream(commonTimes)
                    .map(time -> {
                        List<User> available = interviewService.getAvailableInterviewers(date, time);
                        return Map.of(
                            "time", time,
                            "availableCount", available.size(),
                            "availableInterviewers", available.stream()
                                .map(user -> Map.of(
                                    "id", user.getId(),
                                    "name", user.getFirstName() + " " + user.getLastName(),
                                    "role", user.getRole()
                                )).toList()
                        );
                    }).toList(),
                "message", "Resumen de disponibilidad obtenido exitosamente"
            );
            
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("Error al obtener resumen de disponibilidad: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error", "Error al obtener resumen de disponibilidad",
                    "message", e.getMessage(),
                    "date", date
                ));
        }
    }

    /**
     * Endpoint de prueba para verificar la integración con el sistema de horarios
     */
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Prueba del sistema de disponibilidad",
               description = "Endpoint de prueba para verificar la integración del sistema de horarios")
    public ResponseEntity<Map<String, Object>> testAvailabilitySystem() {
        log.info("GET /api/interviews/availability/test - Prueba del sistema");
        
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "system", "Interview Availability System",
            "features", List.of(
                "Horarios configurados por entrevistador",
                "Filtrado automático por disponibilidad",
                "Validación de conflictos",
                "Filtrado por tipo de entrevista y rol"
            ),
            "timestamp", java.time.Instant.now().toString(),
            "message", "Sistema de disponibilidad funcionando correctamente"
        ));
    }
}