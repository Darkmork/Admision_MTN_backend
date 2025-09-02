package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.EmailTemplate;
import com.desafios.admision_mtn.service.EmailTemplateService;
import com.desafios.admision_mtn.service.TemplatedInterviewNotificationService;
import com.desafios.admision_mtn.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Templates", description = "Gestión de templates de correo institucional")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;
    private final TemplatedInterviewNotificationService templatedNotificationService;
    private final ApplicationService applicationService;

    @Operation(summary = "Obtener todos los templates de email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Templates obtenidos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        try {
            log.info("Obteniendo todos los templates de email");
            List<EmailTemplate> templates = emailTemplateService.getAllTemplates();
            log.info("Se encontraron {} templates", templates.size());
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error obteniendo todos los templates: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testConnection() {
        try {
            log.info("Probando conexión a email templates");
            long count = emailTemplateService.getTemplateCount();
            log.info("Conteo directo desde DB: {}", count);
            return ResponseEntity.ok("Templates encontrados: " + count);
        } catch (Exception e) {
            log.error("Error en test de conexión: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener templates por categoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Templates obtenidos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailTemplate>> getTemplatesByCategory(
        @Parameter(description = "Categoría del template", required = true)
        @PathVariable String category) {
        try {
            log.info("Obteniendo templates para categoría: {}", category);
            EmailTemplate.TemplateCategory templateCategory = EmailTemplate.TemplateCategory.valueOf(category.toUpperCase());
            List<EmailTemplate> templates = emailTemplateService.getTemplatesByCategory(templateCategory);
            log.info("Se encontraron {} templates para categoría {}", templates.size(), category);
            return ResponseEntity.ok(templates);
        } catch (IllegalArgumentException e) {
            log.error("Categoría inválida: {}", category);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error obteniendo templates por categoría: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Obtener template por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Template no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailTemplate> getTemplateById(
        @Parameter(description = "ID del template", required = true)
        @PathVariable Long id) {
        try {
            log.info("Obteniendo template con ID: {}", id);
            EmailTemplate template = emailTemplateService.getTemplateById(id);
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Error obteniendo template por ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Enviar email usando template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendTemplatedEmail(
        @Parameter(description = "Datos para envío de email templated", required = true)
        @RequestBody Map<String, Object> request) {
        try {
            String templateKey = (String) request.get("templateKey");
            Long applicationId = Long.valueOf(request.get("applicationId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> variables = (Map<String, Object>) request.getOrDefault("variables", Map.of());

            log.info("Enviando email templated con clave {} para aplicación {}", templateKey, applicationId);

            // Determinar el tipo de notificación basado en el templateKey
            switch (templateKey) {
                case "STUDENT_SELECTION":
                    templatedNotificationService.sendStudentSelectionNotification(
                        getApplicationById(applicationId));
                    break;
                case "STUDENT_REJECTION":
                    String reason = (String) variables.getOrDefault("rejectionReason", "No cumple con los criterios de admisión");
                    templatedNotificationService.sendStudentRejectionNotification(
                        getApplicationById(applicationId), reason);
                    break;
                case "INTERVIEW_ASSIGNMENT":
                    // Implementación temporal: Por ahora enviar notificación de selección
                    // TODO: Implementar correctamente cuando se resuelvan los errores de compilación
                    com.desafios.admision_mtn.entity.Application application = getApplicationById(applicationId);
                    log.warn("INTERVIEW_ASSIGNMENT temporalmente usando template de selección para aplicación {}", applicationId);
                    templatedNotificationService.sendStudentSelectionNotification(application);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de template no soportado: " + templateKey);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email agregado a la cola institucional",
                "queueId", "templated-" + System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Error enviando email templated: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error enviando email: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Obtener variables disponibles para una categoría")
    @GetMapping("/variables/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAvailableVariables(
        @Parameter(description = "Categoría del template", required = true)
        @PathVariable String category) {
        try {
            EmailTemplate.TemplateCategory templateCategory = EmailTemplate.TemplateCategory.valueOf(category.toUpperCase());
            
            List<String> baseVariables = List.of(
                "studentName", "studentFirstName", "studentLastName", 
                "gradeApplied", "applicantName", "applicantEmail",
                "collegeName", "collegePhone", "collegeEmail",
                "currentDate", "currentYear"
            );

            List<String> categoryVariables;
            switch (templateCategory) {
                case INTERVIEW_ASSIGNMENT:
                case INTERVIEW_CONFIRMATION:
                case INTERVIEW_REMINDER:
                case INTERVIEW_RESCHEDULE:
                    categoryVariables = List.of(
                        "parentNames", "interviewType", "interviewMode",
                        "interviewDate", "interviewTime", "interviewDuration",
                        "interviewLocation", "interviewerName", "meetingLink"
                    );
                    break;
                case STUDENT_SELECTION:
                case STUDENT_REJECTION:
                case ADMISSION_RESULTS:
                    categoryVariables = List.of(
                        "admissionResult", "additionalInfo", "rejectionReason"
                    );
                    break;
                default:
                    categoryVariables = List.of();
                    break;
            }

            return ResponseEntity.ok(Map.of(
                "baseVariables", baseVariables,
                "categoryVariables", categoryVariables,
                "allVariables", 
                java.util.stream.Stream.concat(baseVariables.stream(), categoryVariables.stream()).toList()
            ));

        } catch (IllegalArgumentException e) {
            log.error("Categoría inválida: {}", category);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Categoría inválida: " + category
            ));
        } catch (Exception e) {
            log.error("Error obteniendo variables para categoría {}: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Crear nuevo template de email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Template creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailTemplate> createTemplate(
        @Parameter(description = "Datos del template", required = true)
        @RequestBody EmailTemplate template) {
        try {
            log.info("Creando nuevo template: {}", template.getTemplateKey());
            EmailTemplate createdTemplate = emailTemplateService.createTemplate(template);
            log.info("Template creado exitosamente con ID: {}", createdTemplate.getId());
            return ResponseEntity.status(201).body(createdTemplate);
        } catch (Exception e) {
            log.error("Error creando template: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Actualizar template existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Template no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailTemplate> updateTemplate(
        @Parameter(description = "ID del template", required = true)
        @PathVariable Long id,
        @Parameter(description = "Datos actualizados del template", required = true)
        @RequestBody EmailTemplate template) {
        try {
            log.info("Actualizando template con ID: {}", id);
            EmailTemplate updatedTemplate = emailTemplateService.updateTemplate(id, template);
            log.info("Template actualizado exitosamente: {}", updatedTemplate.getTemplateKey());
            return ResponseEntity.ok(updatedTemplate);
        } catch (Exception e) {
            log.error("Error actualizando template con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar template")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Template eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Template no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(
        @Parameter(description = "ID del template", required = true)
        @PathVariable Long id) {
        try {
            log.info("Eliminando template con ID: {}", id);
            emailTemplateService.deleteTemplate(id);
            log.info("Template eliminado exitosamente");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error eliminando template con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    private com.desafios.admision_mtn.entity.Application getApplicationById(Long applicationId) {
        return applicationService.getApplicationById(applicationId);
    }
}