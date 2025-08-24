package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.CreateUserRequest;
import com.desafios.admision_mtn.dto.UpdateUserRequest;
import com.desafios.admision_mtn.dto.UserResponse;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.service.AdminUserService;
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
@Tag(name = "Users", description = "Gestión completa de usuarios del sistema (solo administradores)")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    
    private final AdminUserService adminUserService;
    
    @Operation(
        summary = "[ADMIN] Obtener todos los usuarios", 
        description = "Obtiene lista paginada de usuarios con filtros opcionales por búsqueda, rol y estado.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista paginada de usuarios",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "content": [
                            {
                                "id": 1,
                                "firstName": "Jorge",
                                "lastName": "Gangale",
                                "email": "jorge.gangale@mtn.cl",
                                "role": "ADMIN",
                                "active": true,
                                "emailVerified": true,
                                "createdAt": "2024-08-20T10:00:00"
                            }
                        ],
                        "totalElements": 25,
                        "totalPages": 3,
                        "size": 10,
                        "number": 0
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @Parameter(description = "Término de búsqueda (nombre, apellido, email)", example = "jorge")
        @RequestParam(required = false) String search,
        @Parameter(description = "Filtrar por rol específico", example = "TEACHER_MATHEMATICS")
        @RequestParam(required = false) User.UserRole role,
        @Parameter(description = "Filtrar por estado activo/inactivo", example = "true")
        @RequestParam(required = false) Boolean active,
        @Parameter(description = "Parámetros de paginación (page, size, sort)")
        Pageable pageable) {
        try {
            Page<UserResponse> users = adminUserService.getAllUsers(search, role, active, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(
        summary = "[ADMIN] Obtener usuario por ID", 
        description = "Obtiene los detalles completos de un usuario específico.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Detalles del usuario",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "ID del usuario", required = true, example = "123")
        @PathVariable Long id) {
        try {
            UserResponse user = adminUserService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error retrieving user with id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "[ADMIN] Crear nuevo usuario", 
        description = "Crea un nuevo usuario en el sistema con rol específico. Se genera una contraseña temporal automáticamente.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(value = """
                    {
                        "id": 25,
                        "firstName": "María",
                        "lastName": "González",
                        "email": "maria.gonzalez@mtn.cl",
                        "role": "TEACHER_LANGUAGE",
                        "active": true,
                        "emailVerified": true,
                        "createdAt": "2024-08-24T15:30:00"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos o email ya existe"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Parameter(
            description = "Datos del nuevo usuario",
            required = true,
            schema = @Schema(implementation = CreateUserRequest.class)
        )
        @Valid @RequestBody CreateUserRequest request) {
        try {
            UserResponse user = adminUserService.createUser(request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error creating user: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "[ADMIN] Actualizar usuario", 
        description = "Actualiza la información de un usuario existente.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "ID del usuario", required = true, example = "123")
        @PathVariable Long id,
        @Parameter(
            description = "Datos actualizados del usuario",
            required = true,
            schema = @Schema(implementation = UpdateUserRequest.class)
        )
        @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserResponse user = adminUserService.updateUser(id, request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error updating user with id: {}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(
        summary = "[ADMIN] Eliminar usuario", 
        description = "Elimina permanentemente un usuario del sistema. Acción irreversible.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Usuario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "No se puede eliminar el usuario (ej: único administrador)"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Usuario no encontrado"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
        @Parameter(description = "ID del usuario", required = true, example = "123")
        @PathVariable Long id) {
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
    
    @Operation(
        summary = "[ADMIN] Obtener estadísticas de usuarios", 
        description = "Obtiene estadísticas completas del sistema de usuarios: total por rol, activos/inactivos, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Estadísticas del sistema de usuarios",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "totalUsers": 25,
                        "activeUsers": 22,
                        "inactiveUsers": 3,
                        "usersByRole": {
                            "ADMIN": 2,
                            "TEACHER_MATHEMATICS": 8,
                            "TEACHER_LANGUAGE": 6,
                            "TEACHER_ENGLISH": 4,
                            "PSYCHOLOGIST": 3,
                            "CYCLE_DIRECTOR": 2
                        },
                        "recentlyCreated": 3
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
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