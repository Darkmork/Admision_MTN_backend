package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.CreateSchoolUserDto;
import com.desafios.admision_mtn.dto.SchoolUserResponseDto;
import com.desafios.admision_mtn.model.RolUsuario;
import com.desafios.admision_mtn.service.SchoolUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/school-users")
@CrossOrigin(
    origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "https://admision-mtn.vercel.app"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = {"*"},
    allowCredentials = "true",
    maxAge = 3600
)
public class SchoolUserController {

    @Autowired
    private SchoolUserService schoolUserService;

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<?> createSchoolUser(@Valid @RequestBody CreateSchoolUserDto dto) {
        try {
            SchoolUserResponseDto user = schoolUserService.createSchoolUser(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getAllSchoolUsers() {
        List<SchoolUserResponseDto> users = schoolUserService.getAllSchoolUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getActiveSchoolUsers() {
        List<SchoolUserResponseDto> users = schoolUserService.getActiveSchoolUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/by-role/{role}")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getUsersByRole(@PathVariable RolUsuario role) {
        List<SchoolUserResponseDto> users = schoolUserService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<SchoolUserResponseDto> user = schoolUserService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody CreateSchoolUserDto dto) {
        try {
            SchoolUserResponseDto user = schoolUserService.updateUser(id, dto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            schoolUserService.deactivateUser(id);
            return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/reactivate")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<?> reactivateUser(@PathVariable Long id) {
        try {
            schoolUserService.reactivateUser(id);
            return ResponseEntity.ok(Map.of("message", "Usuario reactivado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        schoolUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-subjects-mapping")
    public ResponseEntity<Map<String, Object>> updateSubjectsMapping() {
        try {
            schoolUserService.updateSubjectsMapping();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Materias actualizadas correctamente");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error actualizando materias: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoints específicos para cada tipo de personal
    @GetMapping("/professors")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getProfessors() {
        return ResponseEntity.ok(schoolUserService.getUsersByRole(RolUsuario.PROFESSOR));
    }

    @GetMapping("/kinder-teachers")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getKinderTeachers() {
        return ResponseEntity.ok(schoolUserService.getUsersByRole(RolUsuario.KINDER_TEACHER));
    }

    @GetMapping("/psychologists")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getPsychologists() {
        return ResponseEntity.ok(schoolUserService.getUsersByRole(RolUsuario.PSYCHOLOGIST));
    }

    @GetMapping("/support-staff")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<List<SchoolUserResponseDto>> getSupportStaff() {
        return ResponseEntity.ok(schoolUserService.getUsersByRole(RolUsuario.SUPPORT_STAFF));
    }

    // Endpoint para estadísticas
    @GetMapping("/stats")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para testing
    public ResponseEntity<Map<String, Object>> getSchoolUserStats() {
        List<SchoolUserResponseDto> allUsers = schoolUserService.getAllSchoolUsers();
        List<SchoolUserResponseDto> activeUsers = schoolUserService.getActiveSchoolUsers();
        
        long professorsCount = allUsers.stream()
                .filter(u -> u.getRole() == RolUsuario.PROFESSOR)
                .count();
        
        long kinderTeachersCount = allUsers.stream()
                .filter(u -> u.getRole() == RolUsuario.KINDER_TEACHER)
                .count();
        
        long psychologistsCount = allUsers.stream()
                .filter(u -> u.getRole() == RolUsuario.PSYCHOLOGIST)
                .count();
        
        long supportStaffCount = allUsers.stream()
                .filter(u -> u.getRole() == RolUsuario.SUPPORT_STAFF)
                .count();

        Map<String, Object> stats = Map.of(
            "totalUsers", allUsers.size(),
            "activeUsers", activeUsers.size(),
            "inactiveUsers", allUsers.size() - activeUsers.size(),
            "professors", professorsCount,
            "kinderTeachers", kinderTeachersCount,
            "psychologists", psychologistsCount,
            "supportStaff", supportStaffCount
        );

        return ResponseEntity.ok(stats);
    }
}