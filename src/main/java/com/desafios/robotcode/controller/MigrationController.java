package com.desafios.robotcode.controller;

import com.desafios.robotcode.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/migration")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    @PostMapping("/conditionals")
    public ResponseEntity<String> migrateConditionalProblems() {
        try {
            migrationService.migrateConditionalProblems();
            return ResponseEntity.ok("Migración de problemas de condicionales completada exitosamente!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en la migración: " + e.getMessage());
        }
    }

    @PostMapping("/loops")
    public ResponseEntity<String> migrateLoopProblems() {
        try {
            migrationService.migrateLoopProblems();
            return ResponseEntity.ok("Migración de problemas de loops completada exitosamente!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en la migración: " + e.getMessage());
        }
    }

    @PostMapping("/functions")
    public ResponseEntity<String> migrateFunctionProblems() {
        try {
            migrationService.migrateFunctionProblems();
            return ResponseEntity.ok("Migración de problemas de funciones completada exitosamente!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en la migración: " + e.getMessage());
        }
    }

    @PostMapping("/lists")
    public ResponseEntity<String> migrateListProblems() {
        try {
            migrationService.migrateListProblems();
            return ResponseEntity.ok("Migración de problemas de listas completada exitosamente!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en la migración: " + e.getMessage());
        }
    }
    
    @PostMapping("/update-solutions")
    public ResponseEntity<String> updateMissingSolutions() {
        try {
            migrationService.updateMissingSolutions();
            return ResponseEntity.ok("Actualización de soluciones completada exitosamente!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error actualizando soluciones: " + e.getMessage());
        }
    }
}