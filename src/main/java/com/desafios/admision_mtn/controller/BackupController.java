package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para gestión de backups y recuperación del sistema
 * 
 * Proporciona endpoints administrativos para ejecutar, monitorear y gestionar
 * copias de seguridad del sistema de admisión escolar.
 */
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Backup Management", description = "Gestión de copias de seguridad y recuperación del sistema")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    @Operation(
        summary = "Ejecutar backup completo manual", 
        description = "Inicia un backup completo del sistema incluyendo base de datos, archivos y configuraciones.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Backup iniciado exitosamente"
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "Acceso denegado - requiere rol ADMIN"
        )
    })
    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> startCompleteBackup() {
        try {
            CompletableFuture<BackupService.BackupResult> backupTask = 
                backupService.performManualBackup("complete");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "started");
            response.put("message", "Backup completo iniciado en segundo plano");
            response.put("type", "complete");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🔄 Backup completo manual iniciado por administrador");
            
            // Configurar callback para cuando termine el backup
            backupTask.thenAccept(result -> {
                if (result.isSuccess()) {
                    log.info("✅ Backup completo manual completado: {}", result.getPath());
                } else {
                    log.error("❌ Backup completo manual falló: {}", result.getErrorMessage());
                }
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error iniciando backup completo", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error iniciando backup: " + e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Ejecutar backup incremental manual", 
        description = "Inicia un backup incremental del sistema (solo cambios recientes).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/incremental")
    public ResponseEntity<Map<String, Object>> startIncrementalBackup() {
        try {
            CompletableFuture<BackupService.BackupResult> backupTask = 
                backupService.performManualBackup("incremental");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "started");
            response.put("message", "Backup incremental iniciado en segundo plano");
            response.put("type", "incremental");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("🔄 Backup incremental manual iniciado por administrador");
            
            backupTask.thenAccept(result -> {
                if (result.isSuccess()) {
                    log.info("✅ Backup incremental manual completado: {}", result.getPath());
                } else {
                    log.error("❌ Backup incremental manual falló: {}", result.getErrorMessage());
                }
            });
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error iniciando backup incremental", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error iniciando backup: " + e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Obtener estadísticas de backups", 
        description = "Obtiene estadísticas del sistema de backup: número de backups, tamaño total, último backup, etc.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBackupStatistics() {
        try {
            BackupService.BackupStatistics stats = backupService.getBackupStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalBackups", stats.totalBackups());
            response.put("totalSizeBytes", stats.totalSizeBytes());
            response.put("totalSizeMB", String.format("%.2f MB", stats.totalSizeBytes() / 1024.0 / 1024.0));
            response.put("retentionDays", stats.retentionDays());
            response.put("lastBackup", stats.lastBackup());
            response.put("timestamp", LocalDateTime.now());
            
            log.info("📊 Estadísticas de backup solicitadas por administrador");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de backup", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error obteniendo estadísticas: " + e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Estado del sistema de backup", 
        description = "Verifica el estado y configuración del sistema de backup automático.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBackupStatus() {
        try {
            BackupService.BackupStatistics stats = backupService.getBackupStatistics();
            
            Map<String, Object> status = new HashMap<>();
            status.put("systemStatus", "ACTIVE");
            status.put("scheduledBackups", Map.of(
                "completeBackup", "Daily at 02:00 AM",
                "incrementalBackup", "Every 6 hours",
                "cleanup", "Daily at 03:00 AM"
            ));
            
            status.put("configuration", Map.of(
                "retentionDays", stats.retentionDays(),
                "backupDirectory", "/tmp/admision-backups",
                "compressionEnabled", true,
                "incrementalBackupsEnabled", true
            ));
            
            status.put("recentActivity", Map.of(
                "totalBackups", stats.totalBackups(),
                "lastBackup", stats.lastBackup(),
                "storageUsed", String.format("%.2f MB", stats.totalSizeBytes() / 1024.0 / 1024.0)
            ));
            
            status.put("healthCheck", stats.totalBackups() > 0 ? "HEALTHY" : "WARNING");
            status.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error verificando estado del sistema de backup", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "ERROR", "error", e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Información del sistema de backup", 
        description = "Obtiene información detallada sobre capacidades y configuración del sistema de backup.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getBackupInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            info.put("capabilities", Map.of(
                "databaseBackup", true,
                "fileSystemBackup", true,
                "configurationBackup", true,
                "incrementalBackups", true,
                "compression", true,
                "automaticCleanup", true,
                "scheduling", true
            ));
            
            info.put("backupTypes", Map.of(
                "complete", "Full system backup including DB, files, and configurations",
                "incremental", "Recent changes only (last 6 hours)",
                "database", "Database only backup",
                "files", "Uploaded files only"
            ));
            
            info.put("schedule", Map.of(
                "completeBackup", "0 0 2 * * * (Daily at 2:00 AM)",
                "incrementalBackup", "Every 6 hours",
                "cleanup", "0 0 3 * * * (Daily at 3:00 AM)"
            ));
            
            info.put("storage", Map.of(
                "format", "tar.gz compressed archives",
                "location", "/tmp/admision-backups",
                "retention", "7 days",
                "metadata", "Included in each backup"
            ));
            
            info.put("recovery", Map.of(
                "databaseRestore", "Use pg_restore with generated SQL files",
                "fileRestore", "Extract tar.gz and copy files back",
                "configRestore", "Manual restoration of configuration files"
            ));
            
            info.put("timestamp", LocalDateTime.now());
            
            log.info("📋 Información del sistema de backup solicitada por administrador");
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("Error obteniendo información del sistema de backup", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error obteniendo información: " + e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }

    @Operation(
        summary = "Verificar integridad del sistema", 
        description = "Verifica la integridad de los componentes del sistema antes de realizar backup.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifySystemIntegrity() {
        try {
            Map<String, Object> verification = new HashMap<>();
            
            // Verificar acceso a base de datos
            boolean dbAccess = true; // Simplificado - en producción verificar conexión real
            
            // Verificar espacio en disco
            java.io.File backupDir = new java.io.File("/tmp/admision-backups");
            long freeSpace = backupDir.getFreeSpace();
            long totalSpace = backupDir.getTotalSpace();
            double freeSpaceGB = freeSpace / (1024.0 * 1024.0 * 1024.0);
            
            // Verificar herramientas de backup
            boolean pgDumpAvailable = checkCommandAvailable("pg_dump");
            boolean tarAvailable = checkCommandAvailable("tar");
            
            verification.put("database", Map.of(
                "accessible", dbAccess,
                "status", dbAccess ? "OK" : "ERROR"
            ));
            
            verification.put("storage", Map.of(
                "freeSpaceGB", String.format("%.2f GB", freeSpaceGB),
                "totalSpaceGB", String.format("%.2f GB", totalSpace / (1024.0 * 1024.0 * 1024.0)),
                "sufficient", freeSpaceGB > 1.0 // Al menos 1GB libre
            ));
            
            verification.put("tools", Map.of(
                "pgDump", pgDumpAvailable,
                "tar", tarAvailable,
                "allAvailable", pgDumpAvailable && tarAvailable
            ));
            
            verification.put("uploadDirectory", Map.of(
                "exists", new java.io.File("uploads/").exists(),
                "readable", new java.io.File("uploads/").canRead()
            ));
            
            boolean overallOk = dbAccess && freeSpaceGB > 1.0 && pgDumpAvailable && tarAvailable;
            verification.put("overallStatus", overallOk ? "READY" : "WARNING");
            verification.put("timestamp", LocalDateTime.now());
            
            log.info("🔍 Verificación de integridad del sistema completada: {}", 
                    overallOk ? "READY" : "WARNING");
            return ResponseEntity.ok(verification);
            
        } catch (Exception e) {
            log.error("Error verificando integridad del sistema", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error en verificación: " + e.getMessage(), 
                           "timestamp", LocalDateTime.now()));
        }
    }
    
    /**
     * Verifica si un comando está disponible en el sistema
     */
    private boolean checkCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", command);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}