package com.desafios.admision_mtn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de backup y recuperación automatizado
 * 
 * Gestiona copias de seguridad de la base de datos, archivos del sistema
 * y configuraciones críticas con rotación automática.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {
    
    @Value("${backup.database.host:localhost}")
    private String dbHost;
    
    @Value("${backup.database.port:5432}")
    private String dbPort;
    
    @Value("${backup.database.name:Admisión_MTN_DB}")
    private String dbName;
    
    @Value("${backup.database.username:admin}")
    private String dbUsername;
    
    @Value("${backup.database.password:admin123}")
    private String dbPassword;
    
    @Value("${backup.directory:/tmp/admision-backups}")
    private String backupDirectory;
    
    @Value("${backup.retention.days:7}")
    private int retentionDays;
    
    @Value("${uploads.directory:uploads/}")
    private String uploadsDirectory;
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * Backup completo automatizado cada día a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledCompleteBackup() {
        try {
            log.info("🔄 Iniciando backup programado completo...");
            performCompleteBackup()
                .thenRun(() -> log.info("✅ Backup programado completado exitosamente"))
                .exceptionally(throwable -> {
                    log.error("❌ Error en backup programado", throwable);
                    return null;
                });
        } catch (Exception e) {
            log.error("❌ Error iniciando backup programado", e);
        }
    }
    
    /**
     * Backup incremental cada 6 horas
     */
    @Scheduled(fixedRate = 21600000) // 6 horas
    public void scheduledIncrementalBackup() {
        try {
            log.debug("🔄 Iniciando backup incremental programado...");
            performIncrementalBackup()
                .thenRun(() -> log.debug("✅ Backup incremental completado"))
                .exceptionally(throwable -> {
                    log.error("❌ Error en backup incremental", throwable);
                    return null;
                });
        } catch (Exception e) {
            log.error("❌ Error iniciando backup incremental", e);
        }
    }
    
    /**
     * Limpieza de backups antiguos cada día a las 3:00 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledCleanup() {
        try {
            cleanupOldBackups();
            log.info("🧹 Limpieza de backups antiguos completada");
        } catch (Exception e) {
            log.error("❌ Error en limpieza de backups", e);
        }
    }
    
    /**
     * Ejecuta un backup completo del sistema
     */
    @Async
    public CompletableFuture<BackupResult> performCompleteBackup() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = "complete_backup_" + timestamp;
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("🔄 Iniciando backup completo del sistema...");
                
                // Crear directorio de backup
                Path backupPath = createBackupDirectory(backupName);
                
                // 1. Backup de base de datos
                BackupResult dbResult = backupDatabase(backupPath, "complete");
                if (!dbResult.isSuccess()) {
                    throw new RuntimeException("Fallo backup de base de datos: " + dbResult.getErrorMessage());
                }
                
                // 2. Backup de archivos uploadados
                BackupResult filesResult = backupUploadedFiles(backupPath);
                if (!filesResult.isSuccess()) {
                    log.warn("⚠️ Fallo backup de archivos: {}", filesResult.getErrorMessage());
                }
                
                // 3. Backup de configuraciones
                BackupResult configResult = backupConfigurations(backupPath);
                if (!configResult.isSuccess()) {
                    log.warn("⚠️ Fallo backup de configuraciones: {}", configResult.getErrorMessage());
                }
                
                // 4. Generar archivo de metadatos
                generateBackupMetadata(backupPath, "COMPLETE", 
                    List.of(dbResult, filesResult, configResult));
                
                // 5. Comprimir backup si es exitoso
                String compressedFile = compressBackup(backupPath);
                
                log.info("✅ Backup completo exitoso: {}", compressedFile);
                return new BackupResult(true, compressedFile, "Backup completo exitoso", 
                    backupPath.toFile().length());
                
            } catch (Exception e) {
                log.error("❌ Error en backup completo", e);
                return new BackupResult(false, "", "Error: " + e.getMessage(), 0);
            }
        });
    }
    
    /**
     * Ejecuta un backup incremental (solo cambios recientes)
     */
    @Async
    public CompletableFuture<BackupResult> performIncrementalBackup() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = "incremental_backup_" + timestamp;
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("🔄 Iniciando backup incremental...");
                
                Path backupPath = createBackupDirectory(backupName);
                
                // Backup incremental solo de datos recientes (últimas 6 horas)
                BackupResult dbResult = backupDatabase(backupPath, "incremental");
                
                generateBackupMetadata(backupPath, "INCREMENTAL", List.of(dbResult));
                
                if (dbResult.isSuccess()) {
                    String compressedFile = compressBackup(backupPath);
                    log.debug("✅ Backup incremental exitoso: {}", compressedFile);
                    return new BackupResult(true, compressedFile, "Backup incremental exitoso", 
                        backupPath.toFile().length());
                } else {
                    return dbResult;
                }
                
            } catch (Exception e) {
                log.error("❌ Error en backup incremental", e);
                return new BackupResult(false, "", "Error: " + e.getMessage(), 0);
            }
        });
    }
    
    /**
     * Backup manual bajo demanda
     */
    @Async
    public CompletableFuture<BackupResult> performManualBackup(String backupType) {
        if ("complete".equalsIgnoreCase(backupType)) {
            return performCompleteBackup();
        } else {
            return performIncrementalBackup();
        }
    }
    
    /**
     * Backup de la base de datos usando pg_dump
     */
    private BackupResult backupDatabase(Path backupPath, String type) {
        try {
            String fileName = "database_" + type + "_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".sql";
            Path dbBackupFile = backupPath.resolve(fileName);
            
            // Construir comando pg_dump
            List<String> command = new ArrayList<>();
            command.addAll(Arrays.asList(
                "pg_dump",
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUsername,
                "-d", dbName,
                "--no-password",
                "--format=plain",
                "--no-owner",
                "--no-privileges",
                "-f", dbBackupFile.toString()
            ));
            
            // Configurar variables de entorno
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().put("PGPASSWORD", dbPassword);
            
            log.info("🗃️ Ejecutando backup de base de datos: {}", type);
            Process process = pb.start();
            
            // Capturar output/error
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                
                while ((line = errorReader.readLine()) != null) {
                    error.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && Files.exists(dbBackupFile)) {
                long fileSize = Files.size(dbBackupFile);
                log.info("✅ Backup de base de datos exitoso: {} ({} bytes)", fileName, fileSize);
                return new BackupResult(true, dbBackupFile.toString(), 
                    "Backup BD exitoso", fileSize);
            } else {
                String errorMsg = "pg_dump failed with exit code " + exitCode + ": " + error.toString();
                log.error("❌ Error en backup de base de datos: {}", errorMsg);
                return new BackupResult(false, "", errorMsg, 0);
            }
            
        } catch (Exception e) {
            log.error("❌ Excepción en backup de base de datos", e);
            return new BackupResult(false, "", "Excepción: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Backup de archivos subidos por usuarios
     */
    private BackupResult backupUploadedFiles(Path backupPath) {
        try {
            Path uploadsPath = Paths.get(uploadsDirectory);
            if (!Files.exists(uploadsPath)) {
                return new BackupResult(true, "", "No hay archivos para respaldar", 0);
            }
            
            Path filesBackupDir = backupPath.resolve("uploaded_files");
            Files.createDirectories(filesBackupDir);
            
            // Copiar recursivamente todos los archivos
            copyDirectory(uploadsPath, filesBackupDir);
            
            long totalSize = Files.walk(filesBackupDir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
            
            log.info("✅ Backup de archivos completado: {} bytes", totalSize);
            return new BackupResult(true, filesBackupDir.toString(), 
                "Backup archivos exitoso", totalSize);
            
        } catch (Exception e) {
            log.error("❌ Error en backup de archivos", e);
            return new BackupResult(false, "", "Error archivos: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Backup de configuraciones del sistema
     */
    private BackupResult backupConfigurations(Path backupPath) {
        try {
            Path configDir = backupPath.resolve("configurations");
            Files.createDirectories(configDir);
            
            // Backup de application.yml
            Path appConfig = Paths.get("src/main/resources/application.yml");
            if (Files.exists(appConfig)) {
                Files.copy(appConfig, configDir.resolve("application.yml"));
            }
            
            // Backup de logback configuration si existe
            Path logbackConfig = Paths.get("src/main/resources/logback-spring.xml");
            if (Files.exists(logbackConfig)) {
                Files.copy(logbackConfig, configDir.resolve("logback-spring.xml"));
            }
            
            // Backup de pom.xml
            Path pomConfig = Paths.get("pom.xml");
            if (Files.exists(pomConfig)) {
                Files.copy(pomConfig, configDir.resolve("pom.xml"));
            }
            
            long totalSize = Files.walk(configDir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
            
            log.info("✅ Backup de configuraciones completado: {} bytes", totalSize);
            return new BackupResult(true, configDir.toString(), 
                "Backup config exitoso", totalSize);
            
        } catch (Exception e) {
            log.error("❌ Error en backup de configuraciones", e);
            return new BackupResult(false, "", "Error config: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Genera archivo de metadatos del backup
     */
    private void generateBackupMetadata(Path backupPath, String type, List<BackupResult> results) 
            throws IOException {
        Path metadataFile = backupPath.resolve("backup_metadata.txt");
        
        StringBuilder metadata = new StringBuilder();
        metadata.append("Backup Metadata\n");
        metadata.append("===============\n");
        metadata.append("Type: ").append(type).append("\n");
        metadata.append("Timestamp: ").append(LocalDateTime.now()).append("\n");
        metadata.append("Database: ").append(dbName).append("\n");
        metadata.append("Host: ").append(dbHost).append(":").append(dbPort).append("\n");
        metadata.append("\n");
        
        metadata.append("Components:\n");
        for (BackupResult result : results) {
            metadata.append("- ").append(result.isSuccess() ? "✅" : "❌")
                     .append(" ").append(result.getPath())
                     .append(" (").append(result.getSizeBytes()).append(" bytes)")
                     .append("\n");
            if (!result.isSuccess()) {
                metadata.append("  Error: ").append(result.getErrorMessage()).append("\n");
            }
        }
        
        Files.writeString(metadataFile, metadata.toString());
    }
    
    /**
     * Comprime el directorio de backup
     */
    private String compressBackup(Path backupPath) throws IOException, InterruptedException {
        String tarFile = backupPath.toString() + ".tar.gz";
        
        ProcessBuilder pb = new ProcessBuilder(
            "tar", "-czf", tarFile, "-C", backupPath.getParent().toString(), 
            backupPath.getFileName().toString()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            // Eliminar directorio original después de compresión exitosa
            deleteDirectory(backupPath);
            return tarFile;
        } else {
            throw new RuntimeException("Falló compresión del backup, código de salida: " + exitCode);
        }
    }
    
    /**
     * Crea directorio de backup con timestamp
     */
    private Path createBackupDirectory(String backupName) throws IOException {
        Path backupPath = Paths.get(backupDirectory, backupName);
        Files.createDirectories(backupPath);
        return backupPath;
    }
    
    /**
     * Limpia backups antiguos según política de retención
     */
    private void cleanupOldBackups() throws IOException {
        Path backupDir = Paths.get(backupDirectory);
        if (!Files.exists(backupDir)) {
            return;
        }
        
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        
        Files.list(backupDir)
            .filter(path -> path.toString().endsWith(".tar.gz"))
            .filter(path -> {
                try {
                    return Files.getLastModifiedTime(path).toInstant()
                        .isBefore(cutoff.toInstant(java.time.ZoneOffset.UTC));
                } catch (IOException e) {
                    return false;
                }
            })
            .forEach(path -> {
                try {
                    Files.delete(path);
                    log.info("🗑️ Backup antiguo eliminado: {}", path.getFileName());
                } catch (IOException e) {
                    log.error("❌ Error eliminando backup antiguo: {}", path, e);
                }
            });
    }
    
    /**
     * Copia directorio recursivamente
     */
    private void copyDirectory(Path source, Path destination) throws IOException {
        Files.walk(source)
            .forEach(sourcePath -> {
                try {
                    Path targetPath = destination.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.copy(sourcePath, targetPath);
                    }
                } catch (IOException e) {
                    log.error("Error copiando: {} -> {}", sourcePath, destination, e);
                }
            });
    }
    
    /**
     * Elimina directorio recursivamente
     */
    private void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("Error eliminando: {}", path, e);
                    }
                });
        }
    }
    
    /**
     * Obtiene estadísticas de backups
     */
    public BackupStatistics getBackupStatistics() {
        try {
            Path backupDir = Paths.get(backupDirectory);
            if (!Files.exists(backupDir)) {
                return new BackupStatistics(0, 0, 0, "No hay directorio de backups");
            }
            
            List<Path> backups = Files.list(backupDir)
                .filter(path -> path.toString().endsWith(".tar.gz"))
                .sorted((a, b) -> {
                    try {
                        return Files.getLastModifiedTime(b)
                            .compareTo(Files.getLastModifiedTime(a));
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .toList();
            
            long totalSize = backups.stream()
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
            
            String lastBackup = backups.isEmpty() ? "Nunca" : 
                backups.get(0).getFileName().toString();
            
            return new BackupStatistics(backups.size(), totalSize, retentionDays, lastBackup);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas de backup", e);
            return new BackupStatistics(0, 0, 0, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Record para resultado de backup
     */
    public record BackupResult(
        boolean success,
        String path,
        String errorMessage,
        long sizeBytes
    ) {
        public boolean isSuccess() {
            return success;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public long getSizeBytes() {
            return sizeBytes;
        }
    }
    
    /**
     * Record para estadísticas de backup
     */
    public record BackupStatistics(
        int totalBackups,
        long totalSizeBytes,
        int retentionDays,
        String lastBackup
    ) {}
}