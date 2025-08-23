package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.ApplicationResponse;
import com.desafios.admision_mtn.dto.CreateApplicationRequest;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class ApplicationController {

    private final ApplicationService applicationService;
    private final com.desafios.admision_mtn.service.UserService userService;
    private final com.desafios.admision_mtn.repository.UserRepository userRepository;
    private final com.desafios.admision_mtn.repository.ApplicationRepository applicationRepository;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
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

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplicationById(@PathVariable Long id) {
        try {
            Application application = applicationService.getApplicationById(id);
            return ResponseEntity.ok(application);
            
        } catch (Exception e) {
            log.error("Error fetching application by ID: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints para administradores
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

    @PutMapping("/admin/{id}/status")
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable Long id,
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
    
    // Endpoint público para obtener todas las aplicaciones (solo para desarrollo)
    @GetMapping("/public/all")
    public ResponseEntity<List<Map<String, Object>>> getAllApplicationsPublic() {
        try {
            List<Application> applications = applicationService.getAllApplications();
            List<Map<String, Object>> response = applications.stream()
                    .map(this::createApplicationSummaryResponse)
                    .toList();
            log.info("Returning {} applications for development", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all applications", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private Map<String, Object> createApplicationSummaryResponse(Application application) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", application.getId());
        response.put("status", application.getStatus());
        response.put("submissionDate", application.getSubmissionDate());
        response.put("createdAt", application.getCreatedAt());
        response.put("updatedAt", application.getUpdatedAt());
        
        if (application.getStudent() != null) {
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("id", application.getStudent().getId());
            studentInfo.put("firstName", application.getStudent().getFirstName());
            studentInfo.put("lastName", application.getStudent().getLastName());
            studentInfo.put("maternalLastName", application.getStudent().getMaternalLastName());
            studentInfo.put("fullName", application.getStudent().getFirstName() + " " + 
                           application.getStudent().getLastName() + " " + 
                           application.getStudent().getMaternalLastName());
            studentInfo.put("rut", application.getStudent().getRut());
            studentInfo.put("gradeApplied", application.getStudent().getGradeApplied());
            studentInfo.put("birthDate", application.getStudent().getBirthDate());
            studentInfo.put("currentSchool", application.getStudent().getCurrentSchool());
            response.put("student", studentInfo);
        }
        
        if (application.getFather() != null) {
            Map<String, Object> fatherInfo = new HashMap<>();
            fatherInfo.put("id", application.getFather().getId());
            fatherInfo.put("fullName", application.getFather().getFullName());
            fatherInfo.put("rut", application.getFather().getRut());
            fatherInfo.put("email", application.getFather().getEmail());
            fatherInfo.put("phone", application.getFather().getPhone());
            response.put("father", fatherInfo);
        }
        
        if (application.getMother() != null) {
            Map<String, Object> motherInfo = new HashMap<>();
            motherInfo.put("id", application.getMother().getId());
            motherInfo.put("fullName", application.getMother().getFullName());
            motherInfo.put("rut", application.getMother().getRut());
            motherInfo.put("email", application.getMother().getEmail());
            motherInfo.put("phone", application.getMother().getPhone());
            response.put("mother", motherInfo);
        }
        
        return response;
    }
    
    // Endpoint público para obtener datos de prueba (solo para desarrollo)
    @GetMapping("/public/test-data")
    public ResponseEntity<Map<String, Object>> getTestData() {
        try {
            Map<String, Object> testData = new HashMap<>();
            testData.put("message", "Datos de prueba cargados correctamente");
            testData.put("timestamp", LocalDateTime.now());
            testData.put("status", "success");
            return ResponseEntity.ok(testData);
        } catch (Exception e) {
            log.error("Error getting test data", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Endpoint para limpiar la base de datos (solo para desarrollo)
    @GetMapping("/public/reset-database")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        try {
            // Limpiar todas las tablas en orden correcto para evitar problemas de FK
            applicationService.deleteAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Base de datos limpiada exitosamente");
            response.put("timestamp", LocalDateTime.now());
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error limpiando la base de datos", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error limpiando la base de datos: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", "error");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Endpoint público para obtener aplicaciones de prueba (solo para desarrollo)
    @GetMapping("/public/mock-applications")
    public ResponseEntity<List<Map<String, Object>>> getMockApplications() {
        try {
            List<Map<String, Object>> mockApplications = new ArrayList<>();
            
            // Aplicación 1
            Map<String, Object> app1 = new HashMap<>();
            app1.put("id", "APP-001");
            app1.put("status", "SUBMITTED");
            app1.put("submissionDate", "2024-08-15T10:30:00");
            app1.put("student", Map.of(
                "firstName", "Juan Carlos",
                "lastName", "Gangale González",
                "rut", "12345678-9",
                "birthDate", "2015-03-15",
                "gradeApplied", "3° Básico",
                "address", "Av. Providencia 123, Santiago",
                "currentSchool", "Colegio San Ignacio"
            ));
            app1.put("applicantUser", Map.of(
                "firstName", "Jorge",
                "lastName", "Gangale",
                "email", "jorge.gangale@mtn.cl"
            ));
            mockApplications.add(app1);
            
            // Aplicación 2
            Map<String, Object> app2 = new HashMap<>();
            app2.put("id", "APP-002");
            app2.put("status", "INTERVIEW_SCHEDULED");
            app2.put("submissionDate", "2024-08-16T09:15:00");
            app2.put("student", Map.of(
                "firstName", "Ana Sofía",
                "lastName", "González López",
                "rut", "87654321-0",
                "birthDate", "2014-07-22",
                "gradeApplied", "4° Básico",
                "address", "Av. Las Condes 456, Santiago",
                "currentSchool", "Colegio San Agustín"
            ));
            app2.put("applicantUser", Map.of(
                "firstName", "María",
                "lastName", "González",
                "email", "maria.gonzalez@mtn.cl"
            ));
            mockApplications.add(app2);
            
            return ResponseEntity.ok(mockApplications);
        } catch (Exception e) {
            log.error("Error getting mock applications", e);
            return ResponseEntity.badRequest().build();
        }
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
    
    // Endpoint para debugging base de datos
    @GetMapping("/public/debug-database")
    public ResponseEntity<Map<String, Object>> debugDatabase() {
        try {
            Map<String, Object> debug = new HashMap<>();
            debug.put("message", "Debug database connection");
            debug.put("timestamp", LocalDateTime.now());
            
            // Contar aplicaciones directamente con repository
            long applicationCount = applicationRepository.count();
            debug.put("applicationCount", applicationCount);
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in debug database endpoint", e);
            Map<String, Object> errorDebug = new HashMap<>();
            errorDebug.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorDebug);
        }
    }

    // Endpoint temporal para debugging autenticación
    @GetMapping("/public/debug-users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        try {
            Map<String, Object> debug = new HashMap<>();
            debug.put("message", "Debug endpoint funcionando");
            debug.put("timestamp", LocalDateTime.now());
            
            // Intentar buscar usuario directamente
            try {
                java.util.Optional<com.desafios.admision_mtn.entity.User> userOpt = 
                    userService.findByEmail("admin@test.cl");
                
                if (userOpt.isPresent()) {
                    com.desafios.admision_mtn.entity.User user = userOpt.get();
                    debug.put("userFound", true);
                    debug.put("userEmail", user.getEmail());
                    debug.put("userActive", user.getActive());
                    debug.put("userEmailVerified", user.getEmailVerified());
                    debug.put("userEnabled", user.isEnabled());
                    debug.put("userRole", user.getRole().name());
                } else {
                    debug.put("userFound", false);
                }
                
                // Probar con otros emails para ver si encuentra alguno
                java.util.Optional<com.desafios.admision_mtn.entity.User> userOpt2 = 
                    userService.findByEmail("jorge.gangale@mtn.cl");
                debug.put("foundJorgeUser", userOpt2.isPresent());
                
                // Contar total de usuarios usando repository
                long totalUsers = userRepository.count();
                debug.put("totalUsersInSpring", totalUsers);
                
                // Listar los emails de los usuarios que Spring SÍ ve
                java.util.List<String> springEmails = userRepository.findAll()
                    .stream()
                    .map(u -> u.getEmail())
                    .collect(java.util.stream.Collectors.toList());
                debug.put("springUserEmails", springEmails);
                
            } catch (Exception e) {
                debug.put("userServiceError", e.getMessage());
            }
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in debug endpoint", e);
            Map<String, Object> errorDebug = new HashMap<>();
            errorDebug.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorDebug);
        }
    }
}