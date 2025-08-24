package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.service.ApplicationService;
import com.desafios.admision_mtn.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Profile("dev")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class ApplicationDevController {

    private final ApplicationService applicationService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JdbcTemplate jdbcTemplate;

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

    @GetMapping("/public/reset-database")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        try {
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

    @GetMapping("/public/mock-applications")
    public ResponseEntity<List<Map<String, Object>>> getMockApplications() {
        try {
            List<Map<String, Object>> mockApplications = new ArrayList<>();

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

    @GetMapping("/public/debug-database")
    public ResponseEntity<Map<String, Object>> debugDatabase() {
        try {
            Map<String, Object> debug = new HashMap<>();
            debug.put("message", "Debug database connection");
            debug.put("timestamp", LocalDateTime.now());

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

    @GetMapping("/public/debug-connection")
    public ResponseEntity<Map<String, Object>> debugConnection() {
        try {
            Map<String, Object> debug = new HashMap<>();
            debug.put("message", "Debug database connection using JdbcTemplate");
            debug.put("timestamp", LocalDateTime.now());

            Integer jdbcCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Integer.class);
            debug.put("jdbcTemplateCount", jdbcCount);

            String currentDb = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            String currentUser = jdbcTemplate.queryForObject("SELECT current_user", String.class);
            debug.put("currentDatabase", currentDb);
            debug.put("currentUser", currentUser);

            long hibernateCount = applicationRepository.count();
            debug.put("hibernateRepositoryCount", hibernateCount);

            if (jdbcCount > 0) {
                List<Map<String, Object>> sampleData = jdbcTemplate.queryForList(
                    "SELECT id, status, submission_date FROM applications LIMIT 3"
                );
                debug.put("jdbcSampleData", sampleData);
            }

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in connection debug endpoint", e);
            Map<String, Object> errorDebug = new HashMap<>();
            errorDebug.put("error", e.getMessage());
            errorDebug.put("exception", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(errorDebug);
        }
    }

    @GetMapping("/public/debug-users")
    public ResponseEntity<Map<String, Object>> debugUsers() {
        try {
            Map<String, Object> debug = new HashMap<>();
            debug.put("message", "Debug endpoint funcionando");
            debug.put("timestamp", LocalDateTime.now());

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

                java.util.Optional<com.desafios.admision_mtn.entity.User> userOpt2 =
                    userService.findByEmail("jorge.gangale@mtn.cl");
                debug.put("foundJorgeUser", userOpt2.isPresent());

                long totalUsers = userRepository.count();
                debug.put("totalUsersInSpring", totalUsers);

                List<String> springEmails = userRepository.findAll()
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

