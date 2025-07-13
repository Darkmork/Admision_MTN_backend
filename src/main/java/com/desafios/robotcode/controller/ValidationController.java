package com.desafios.robotcode.controller;

import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.repository.ProblemaRepository;
import com.desafios.robotcode.service.TestCaseValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class ValidationController {

    @Autowired
    private TestCaseValidationService validationService;

    @Autowired
    private ProblemaRepository problemaRepository;

    @PostMapping("/problem/{problemId}")
    public ResponseEntity<?> validateProblemTestCases(@PathVariable Long problemId) {
        try {
            Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
            if (problemaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Problema problema = problemaOpt.get();
            
            if (problema.getSolucionCorrecta() == null) {
                return ResponseEntity.badRequest().body("No hay solución de referencia para validar");
            }

            // Extract function name from solution code
            String functionName = extractFunctionName(problema.getSolucionCorrecta());
            if (functionName == null) {
                return ResponseEntity.badRequest().body("No se pudo extraer el nombre de la función");
            }

            TestCaseValidationService.ValidationResult result = validationService.validateTestCases(
                problema.getSolucionCorrecta(),
                problema.getTestCasesJson(),
                functionName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en validación: " + e.getMessage());
        }
    }

    @PostMapping("/all-conditionals")
    public ResponseEntity<?> validateAllConditionalProblems() {
        try {
            // Get all conditional problems that have solution code
            var problems = problemaRepository.findAll().stream()
                .filter(p -> p.getTema() != null && "Condicionales".equals(p.getTema().getNombre()))
                .filter(p -> p.getSolucionCorrecta() != null)
                .toList();

            var results = new java.util.ArrayList<ValidationSummary>();

            for (Problema problema : problems) {
                String functionName = extractFunctionName(problema.getSolucionCorrecta());
                if (functionName != null) {
                    TestCaseValidationService.ValidationResult result = validationService.validateTestCases(
                        problema.getSolucionCorrecta(),
                        problema.getTestCasesJson(),
                        functionName
                    );
                    
                    results.add(new ValidationSummary(
                        problema.getId(),
                        problema.getTitulo(),
                        result.isSuccess(),
                        result.getMessage(),
                        result.getTestResults() != null ? result.getTestResults().size() : 0,
                        result.getTestResults() != null ? 
                            (int) result.getTestResults().stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum() : 0
                    ));
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en validación masiva: " + e.getMessage());
        }
    }

    @PostMapping("/all-loops")
    public ResponseEntity<?> validateAllLoopProblems() {
        try {
            // Get all loop problems that have solution code
            var problems = problemaRepository.findAll().stream()
                .filter(p -> p.getTema() != null && "Bucles".equals(p.getTema().getNombre()))
                .filter(p -> p.getSolucionCorrecta() != null)
                .toList();

            var results = new java.util.ArrayList<ValidationSummary>();

            for (Problema problema : problems) {
                String functionName = extractFunctionName(problema.getSolucionCorrecta());
                if (functionName != null) {
                    TestCaseValidationService.ValidationResult result = validationService.validateTestCases(
                        problema.getSolucionCorrecta(),
                        problema.getTestCasesJson(),
                        functionName
                    );
                    
                    results.add(new ValidationSummary(
                        problema.getId(),
                        problema.getTitulo(),
                        result.isSuccess(),
                        result.getMessage(),
                        result.getTestResults() != null ? result.getTestResults().size() : 0,
                        result.getTestResults() != null ? 
                            (int) result.getTestResults().stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum() : 0
                    ));
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en validación masiva: " + e.getMessage());
        }
    }

    @PostMapping("/all-functions")
    public ResponseEntity<?> validateAllFunctionProblems() {
        try {
            // Get all function problems that have solution code
            var problems = problemaRepository.findAll().stream()
                .filter(p -> p.getTema() != null && "Funciones".equals(p.getTema().getNombre()))
                .filter(p -> p.getSolucionCorrecta() != null)
                .toList();

            var results = new java.util.ArrayList<ValidationSummary>();

            for (Problema problema : problems) {
                String functionName = extractFunctionName(problema.getSolucionCorrecta());
                if (functionName != null) {
                    TestCaseValidationService.ValidationResult result = validationService.validateTestCases(
                        problema.getSolucionCorrecta(),
                        problema.getTestCasesJson(),
                        functionName
                    );
                    
                    results.add(new ValidationSummary(
                        problema.getId(),
                        problema.getTitulo(),
                        result.isSuccess(),
                        result.getMessage(),
                        result.getTestResults() != null ? result.getTestResults().size() : 0,
                        result.getTestResults() != null ? 
                            (int) result.getTestResults().stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum() : 0
                    ));
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en validación masiva: " + e.getMessage());
        }
    }

    @PostMapping("/all-lists")
    public ResponseEntity<?> validateAllListProblems() {
        try {
            // Get all list problems that have solution code
            var problems = problemaRepository.findAll().stream()
                .filter(p -> p.getTema() != null && "Listas y Arrays".equals(p.getTema().getNombre()))
                .filter(p -> p.getSolucionCorrecta() != null)
                .toList();

            var results = new java.util.ArrayList<ValidationSummary>();

            for (Problema problema : problems) {
                String functionName = extractFunctionName(problema.getSolucionCorrecta());
                if (functionName != null) {
                    TestCaseValidationService.ValidationResult result = validationService.validateTestCases(
                        problema.getSolucionCorrecta(),
                        problema.getTestCasesJson(),
                        functionName
                    );
                    
                    results.add(new ValidationSummary(
                        problema.getId(),
                        problema.getTitulo(),
                        result.isSuccess(),
                        result.getMessage(),
                        result.getTestResults() != null ? result.getTestResults().size() : 0,
                        result.getTestResults() != null ? 
                            (int) result.getTestResults().stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum() : 0
                    ));
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en validación masiva: " + e.getMessage());
        }
    }

    private String extractFunctionName(String code) {
        // Extract function name from "def function_name("
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("def ")) {
                int start = line.indexOf("def ") + 4;
                int end = line.indexOf("(");
                if (end > start) {
                    return line.substring(start, end).trim();
                }
            }
        }
        return null;
    }

    public static class ValidationSummary {
        public Long problemId;
        public String title;
        public boolean allTestsPassed;
        public String message;
        public int totalTests;
        public int passedTests;

        public ValidationSummary(Long problemId, String title, boolean allTestsPassed, 
                               String message, int totalTests, int passedTests) {
            this.problemId = problemId;
            this.title = title;
            this.allTestsPassed = allTestsPassed;
            this.message = message;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
        }

        // Getters
        public Long getProblemId() { return problemId; }
        public String getTitle() { return title; }
        public boolean isAllTestsPassed() { return allTestsPassed; }
        public String getMessage() { return message; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
    }
}