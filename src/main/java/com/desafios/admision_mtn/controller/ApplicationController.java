package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.ApplicationResponse;
import com.desafios.admision_mtn.dto.CreateApplicationRequest;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.service.ApplicationService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications", description = "Sistema de gestión de postulaciones de admisión escolar")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
        summary = "Crear nueva postulación", 
        description = "Crea una nueva postulación de admisión para un estudiante. Requiere autenticación de apoderado.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Postulación creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApplicationResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "message": "Postulación creada exitosamente",
                        "applicationId": 123,
                        "status": "DRAFT"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de postulación inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "message": "Error en los datos de postulación"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Usuario no autenticado"
        )
    })
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
        @Parameter(
            description = "Datos de la nueva postulación",
            required = true,
            schema = @Schema(implementation = CreateApplicationRequest.class)
        )
        @Valid @RequestBody CreateApplicationRequest request) {
        try {
            // Obtener el email del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getName() == null) {
                throw new RuntimeException("Usuario no autenticado");
            }
            String userEmail = authentication.getName();
            
            log.info("Creating application for user: {}", userEmail);
            log.info("Student name: {} {} {}", request.getFirstName(), request.getLastName(), request.getMaternalLastName());
            
            ApplicationResponse response = applicationService.createApplication(request, userEmail);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creating application", e);
            return ResponseEntity.badRequest().body(
                ApplicationResponse.error(e.getMessage())
            );
        }
    }

    @Operation(
        summary = "Obtener mis postulaciones", 
        description = "Obtiene todas las postulaciones del usuario autenticado (apoderado).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de postulaciones del usuario",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Application.class)
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Usuario no autenticado"
        )
    })
    @GetMapping("/my-applications")
    public ResponseEntity<List<Application>> getMyApplications() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            List<Application> applications = applicationService.getApplicationsByUser(userEmail);
            return ResponseEntity.ok(applications);
            
        } catch (Exception e) {
            log.error("Error fetching user applications", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Obtener postulación por ID", 
        description = "Obtiene los detalles completos de una postulación específica.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Detalles de la postulación",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Application.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Postulación no encontrada"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Usuario no autenticado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(
        @Parameter(description = "ID de la postulación", required = true, example = "123")
        @PathVariable Long id) {
        try {
            Application application = applicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
            
        } catch (Exception e) {
            log.error("Error fetching application by ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints para administradores
    @Operation(
        summary = "[ADMIN] Obtener todas las postulaciones", 
        description = "Obtiene todas las postulaciones del sistema. Solo para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista completa de postulaciones",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Application.class)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @GetMapping("/admin/all")
    public ResponseEntity<List<Application>> getAllApplications() {
        try {
            List<Application> applications = applicationService.getAllApplications();
            return ResponseEntity.ok(applications);
            
        } catch (Exception e) {
            log.error("Error fetching all applications", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "[ADMIN] Actualizar estado de postulación", 
        description = "Actualiza el estado de una postulación específica. Estados válidos: DRAFT, SUBMITTED, UNDER_REVIEW, INTERVIEW_SCHEDULED, EXAM_SCHEDULED, APPROVED, REJECTED, WAITLIST.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estado actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Application.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Estado inválido"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Postulación no encontrada"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @PutMapping("/admin/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
        @Parameter(description = "ID de la postulación", required = true, example = "123")
        @PathVariable Long id,
        @Parameter(description = "Nuevo estado de la postulación", required = true, example = "APPROVED")
        @RequestParam String status) {
        try {
            Application.ApplicationStatus applicationStatus = 
                Application.ApplicationStatus.valueOf(status.toUpperCase());
            
            Application updatedApplication = applicationService.updateApplicationStatus(id, applicationStatus);
            return ResponseEntity.ok(updatedApplication);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating application status", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint de prueba para verificar que el backend esté funcionando
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Backend funcionando correctamente");
    }
    
    
    // Endpoints para gestión de documentos de aplicaciones
    @GetMapping("/{id}/document-status")
    public ResponseEntity<Map<String, Object>> getApplicationDocumentStatus(@PathVariable Long id) {
        try {
            Map<String, Object> status = applicationService.getApplicationDocumentStatus(id);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting document status for application {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/missing-documents")
    public ResponseEntity<List<String>> getMissingDocuments(@PathVariable Long id) {
        try {
            List<String> missingDocuments = applicationService.getMissingDocuments(id)
                    .stream()
                    .map(Enum::name)
                    .toList();
            return ResponseEntity.ok(missingDocuments);
        } catch (Exception e) {
            log.error("Error getting missing documents for application {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/update-status-by-documents")
    public ResponseEntity<Application> updateApplicationStatusByDocuments(@PathVariable Long id) {
        try {
            Application updatedApplication = applicationService.updateApplicationStatusBasedOnDocuments(id);
            return ResponseEntity.ok(updatedApplication);
        } catch (Exception e) {
            log.error("Error updating application status by documents for application {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
}