package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.CreateUserRequest;
import com.desafios.admision_mtn.dto.UpdateUserRequest;
import com.desafios.admision_mtn.dto.UserResponse;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    private final AdminUserService adminUserService;
    
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {
        try {
            Page<UserResponse> users = adminUserService.getAllUsers(search, role, active, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = adminUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error retrieving user with id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserResponse user = adminUserService.createUser(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error creating user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserResponse user = adminUserService.updateUser(id, request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminUserService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting user with id: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        try {
            UserResponse user = adminUserService.activateUser(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error activating user with id: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            adminUserService.deactivateUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error deactivating user with id: {}", id, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deactivating user with id: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error interno del servidor"));
        }
    }
    
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetUserPassword(@PathVariable Long id) {
        try {
            adminUserService.resetUserPassword(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error resetting password for user with id: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/roles")
    public ResponseEntity<List<User.UserRole>> getAllRoles() {
        return ResponseEntity.ok(List.of(User.UserRole.values()));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Object> getUserStats() {
        try {
            var stats = adminUserService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving user statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}