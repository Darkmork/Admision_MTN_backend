package com.desafios.robotcode.controller;

import com.desafios.robotcode.service.CodeExecutionEngine;
import com.desafios.robotcode.service.FlexibleCodeExecutor;
import com.desafios.robotcode.service.ProblemTestingService;
import com.desafios.robotcode.service.AdvancedCodeEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/judge")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class CodeJudgeController {

    @Autowired
    private CodeExecutionEngine executionEngine;

    @Autowired
    private ProblemTestingService testingService;

    @Autowired
    private FlexibleCodeExecutor flexibleExecutor;

    @Autowired
    private AdvancedCodeEvaluator advancedEvaluator;

    /**
     * Ejecutar código directamente (estilo Judge0)
     */
    @PostMapping("/execute")
    public ResponseEntity<?> executeCode(@RequestBody CodeExecutionEngine.ExecutionRequest request) {
        try {
            CodeExecutionEngine.ExecutionResult result = executionEngine.executeCode(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error ejecutando código: " + e.getMessage()
            ));
        }
    }

    /**
     * Validar código del usuario contra test cases de un problema específico
     */
    @PostMapping("/validate/{problemId}")
    public ResponseEntity<?> validateUserCode(
            @PathVariable Long problemId,
            @RequestBody ValidationRequest request) {
        try {
            ProblemTestingService.ValidationReport report = testingService.validateUserCode(
                problemId, request.code, request.language
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando código: " + e.getMessage()
            ));
        }
    }

    /**
     * Validar solución oficial de un problema
     */
    @PostMapping("/validate-official/{problemId}")
    public ResponseEntity<?> validateOfficialSolution(@PathVariable Long problemId) {
        try {
            ProblemTestingService.ValidationReport report = testingService.validateOfficialSolution(problemId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando solución oficial: " + e.getMessage()
            ));
        }
    }

    /**
     * Validar todas las soluciones oficiales de problemas de condicionales
     */
    @PostMapping("/validate-all-conditionals")
    public ResponseEntity<?> validateAllConditionals() {
        try {
            var problemIds = java.util.List.of(
                101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L, 110L, 111L, 112L, // Fáciles
                151L, 152L, 153L, 154L, 155L, 156L, 157L, // Intermedios  
                181L, 182L, 183L, 184L, 185L, 186L, 187L  // Difíciles
            );
            
            var reports = new java.util.ArrayList<ValidationSummary>();
            
            for (Long problemId : problemIds) {
                try {
                    ProblemTestingService.ValidationReport report = testingService.validateOfficialSolution(problemId);
                    
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = report.problemTitle;
                    summary.allTestsPassed = report.allTestsPassed;
                    summary.passedTests = report.passedTests;
                    summary.totalTests = report.totalTests;
                    summary.executionTime = report.totalExecutionTime;
                    summary.summary = report.summary;
                    
                    reports.add(summary);
                    
                } catch (Exception e) {
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = "Error";
                    summary.allTestsPassed = false;
                    summary.summary = "Error: " + e.getMessage();
                    reports.add(summary);
                }
            }
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando problemas: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/validate-all-loops")
    public ResponseEntity<?> validateAllLoops() {
        try {
            var problemIds = java.util.List.of(
                201L, 202L, 203L, 204L, 205L, 206L, 207L, 208L, 209L, 210L, 211L, 212L, // Fáciles
                251L, 252L, 253L, 254L, 255L, 256L, 257L, // Intermedios  
                281L, 282L, 283L, 284L, 285L, 286L, 287L  // Difíciles
            );
            
            var reports = new java.util.ArrayList<ValidationSummary>();
            
            for (Long problemId : problemIds) {
                try {
                    ProblemTestingService.ValidationReport report = testingService.validateOfficialSolution(problemId);
                    
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = report.problemTitle;
                    summary.allTestsPassed = report.allTestsPassed;
                    summary.passedTests = report.passedTests;
                    summary.totalTests = report.totalTests;
                    summary.executionTime = report.totalExecutionTime;
                    summary.summary = report.summary;
                    
                    reports.add(summary);
                    
                } catch (Exception e) {
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = "Error";
                    summary.allTestsPassed = false;
                    summary.summary = "Error: " + e.getMessage();
                    reports.add(summary);
                }
            }
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando problemas: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/validate-all-functions")
    public ResponseEntity<?> validateAllFunctions() {
        try {
            var problemIds = java.util.List.of(
                301L, 302L, 303L, 304L, 305L, 306L, 307L, 308L, 309L, 310L, 311L, 312L, // Fáciles
                351L, 352L, 353L, 354L, 355L, 356L, 357L, // Intermedios  
                381L, 382L, 383L, 384L, 385L, 386L, 387L  // Difíciles
            );
            
            var reports = new java.util.ArrayList<ValidationSummary>();
            
            for (Long problemId : problemIds) {
                try {
                    ProblemTestingService.ValidationReport report = testingService.validateOfficialSolution(problemId);
                    
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = report.problemTitle;
                    summary.allTestsPassed = report.allTestsPassed;
                    summary.passedTests = report.passedTests;
                    summary.totalTests = report.totalTests;
                    summary.executionTime = report.totalExecutionTime;
                    summary.summary = report.summary;
                    
                    reports.add(summary);
                    
                } catch (Exception e) {
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = "Error";
                    summary.allTestsPassed = false;
                    summary.summary = "Error: " + e.getMessage();
                    reports.add(summary);
                }
            }
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando problemas: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/validate-all-lists")
    public ResponseEntity<?> validateAllLists() {
        try {
            var problemIds = java.util.List.of(
                401L, 402L, 403L, 404L, 405L, 406L, 407L, 408L, 409L, 410L, 411L, 412L, // Fáciles
                451L, 452L, 453L, 454L, 455L, 456L, 457L, // Intermedios  
                481L, 482L, 483L, 484L, 485L, 486L, 487L  // Difíciles
            );
            
            var reports = new java.util.ArrayList<ValidationSummary>();
            
            for (Long problemId : problemIds) {
                try {
                    ProblemTestingService.ValidationReport report = testingService.validateOfficialSolution(problemId);
                    
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = report.problemTitle;
                    summary.allTestsPassed = report.allTestsPassed;
                    summary.passedTests = report.passedTests;
                    summary.totalTests = report.totalTests;
                    summary.executionTime = report.totalExecutionTime;
                    summary.summary = report.summary;
                    
                    reports.add(summary);
                    
                } catch (Exception e) {
                    ValidationSummary summary = new ValidationSummary();
                    summary.problemId = problemId;
                    summary.problemTitle = "Error";
                    summary.allTestsPassed = false;
                    summary.summary = "Error: " + e.getMessage();
                    reports.add(summary);
                }
            }
            
            return ResponseEntity.ok(reports);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando problemas: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtener información del sistema de ejecución
     */
    @GetMapping("/info")
    public ResponseEntity<?> getSystemInfo() {
        return ResponseEntity.ok(Map.of(
            "name", "RobotCode Judge Engine",
            "version", "2.0.0",
            "supportedLanguages", java.util.List.of("python3", "java", "javascript"),
            "features", java.util.List.of(
                "Code execution with timeouts",
                "Memory limits",
                "Output limits", 
                "Secure sandboxing",
                "Detailed error reporting",
                "Test case validation",
                "Flexible code execution",
                "Smart function detection",
                "Flexible output comparison",
                "Multiple execution strategies"
            ),
            "limits", Map.of(
                "timeoutSeconds", 10,
                "maxOutputSize", "10KB",
                "maxMemoryMB", 128
            )
        ));
    }

    /**
     * Verificar si Python está disponible en el sistema
     */
    @GetMapping("/health/python")
    public ResponseEntity<?> checkPythonHealth() {
        try {
            System.out.println("🩺 HEALTH CHECK: Verificando Python...");
            
            // Verificar si Python está disponible ejecutando un comando simple
            ProcessBuilder pb = new ProcessBuilder("python3", "--version");
            pb.environment().put("PATH", System.getenv("PATH"));
            Process process = pb.start();
            
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            
            if (finished && process.exitValue() == 0) {
                // Leer la versión de Python
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                String version = reader.readLine();
                
                System.out.println("✅ Python disponible: " + version);
                
                return ResponseEntity.ok(java.util.Map.of(
                    "status", "healthy",
                    "python_available", true,
                    "python_version", version != null ? version : "Unknown",
                    "path", System.getenv("PATH"),
                    "timestamp", System.currentTimeMillis()
                ));
            } else {
                System.out.println("❌ Python no disponible - Exit code: " + process.exitValue());
                return ResponseEntity.status(503).body(java.util.Map.of(
                    "status", "unhealthy",
                    "python_available", false,
                    "error", "Python no está disponible o no responde",
                    "exit_code", process.exitValue(),
                    "path", System.getenv("PATH"),
                    "timestamp", System.currentTimeMillis()
                ));
            }
        } catch (Exception e) {
            System.out.println("💥 Error verificando Python: " + e.getMessage());
            return ResponseEntity.status(503).body(java.util.Map.of(
                "status", "unhealthy",
                "python_available", false,
                "error", "Error verificando Python: " + e.getMessage(),
                "path", System.getenv("PATH"),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Ejecutar código de manera flexible (nuevo sistema mejorado)
     */
    @PostMapping("/execute-flexible")
    public ResponseEntity<?> executeCodeFlexibly(@RequestBody FlexibleExecutionRequest request) {
        try {
            if ("python3".equals(request.language) || "python".equals(request.language)) {
                FlexibleCodeExecutor.ExecutionResult result = flexibleExecutor.executePythonFlexibly(
                    request.sourceCode, request.input
                );
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Lenguaje no soportado para ejecución flexible: " + request.language
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error ejecutando código flexible: " + e.getMessage()
            ));
        }
    }

    /**
     * Validar código del usuario contra test cases usando el sistema flexible
     */
    @PostMapping("/validate-flexible/{problemId}")
    public ResponseEntity<?> validateUserCodeFlexibly(
            @PathVariable Long problemId,
            @RequestBody ValidationRequest request) {
        try {
            ProblemTestingService.ValidationReport report = testingService.validateUserCodeFlexibly(
                problemId, request.code, request.language
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error validando código flexible: " + e.getMessage()
            ));
        }
    }

    /**
     * Probar el sistema flexible con problemas reales de la base de datos
     */
    @PostMapping("/test-flexible-with-real-problems")
    public ResponseEntity<?> testFlexibleWithRealProblems() {
        try {
            var problemIds = java.util.List.of(
                101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L, 110L, 111L, 112L // Fáciles
            );
            
            var results = new java.util.ArrayList<Map<String, Object>>();
            
            for (Long problemId : problemIds) {
                try {
                    var result = testingService.testFlexibleWithProblem(problemId);
                    results.add(result);
                } catch (Exception e) {
                    Map<String, Object> errorResult = Map.of(
                        "problemId", problemId,
                        "problemTitle", "Error",
                        "status", "ERROR",
                        "error", e.getMessage()
                    );
                    results.add(errorResult);
                }
            }
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error probando problemas reales: " + e.getMessage()
            ));
        }
    }

    /**
     * Evaluar código usando el sistema avanzado con métricas de calidad
     */
    @PostMapping("/evaluate-advanced/{problemId}")
    public ResponseEntity<?> evaluateCodeAdvanced(
            @PathVariable Long problemId,
            @RequestBody ValidationRequest request) {
        try {
            // Buscar el problema en la base de datos
            var problema = testingService.findProblemaById(problemId);
            if (problema == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Usar el evaluador avanzado
            AdvancedCodeEvaluator.EvaluationResult result = advancedEvaluator.evaluateCode(
                problema, request.code, request.language
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error en evaluación avanzada: " + e.getMessage()
            ));
        }
    }

    // DTOs
    public static class ValidationRequest {
        public String code;
        public String language = "python3";
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }

    public static class FlexibleExecutionRequest {
        public String sourceCode;
        public String language = "python3";
        public String input = "";
        
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
    }

    public static class ValidationSummary {
        public Long problemId;
        public String problemTitle;
        public boolean allTestsPassed;
        public int passedTests;
        public int totalTests;
        public long executionTime;
        public String summary;
        
        // Getters y setters
        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getProblemTitle() { return problemTitle; }
        public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
        public boolean isAllTestsPassed() { return allTestsPassed; }
        public void setAllTestsPassed(boolean allTestsPassed) { this.allTestsPassed = allTestsPassed; }
        public int getPassedTests() { return passedTests; }
        public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }
}