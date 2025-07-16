package com.desafios.robotcode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Service
public class DockerExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DockerExecutorService.class);
    private static final String DOCKER_IMAGE = "robotcode-python:latest";
    private static final int TIMEOUT_SECONDS = 10;
    private static final String MEMORY_LIMIT = "128m";
    private static final String CPU_LIMIT = "0.5";
    
    public static class ExecutionResult {
        public String output;
        public String error;
        public int exitCode;
        public long executionTime;
        public boolean timedOut;
        
        public ExecutionResult(String output, String error, int exitCode, long executionTime, boolean timedOut) {
            this.output = output;
            this.error = error;
            this.exitCode = exitCode;
            this.executionTime = executionTime;
            this.timedOut = timedOut;
        }
    }
    
    public static class TestResult {
        public String input;
        public String expected;
        public String actual;
        public boolean passed;
        public String error;
        public long executionTime;
        
        public TestResult(String input, String expected, String actual, boolean passed, String error, long executionTime) {
            this.input = input;
            this.expected = expected;
            this.actual = actual;
            this.passed = passed;
            this.error = error;
            this.executionTime = executionTime;
        }
    }
    
    public static class ValidationResult {
        public boolean passed;
        public int correctTests;
        public int totalTests;
        public double successRate;
        public List<TestResult> testResults;
        public String summary;
        public long totalExecutionTime;
        
        public ValidationResult(boolean passed, int correctTests, int totalTests, 
                              double successRate, List<TestResult> testResults, 
                              String summary, long totalExecutionTime) {
            this.passed = passed;
            this.correctTests = correctTests;
            this.totalTests = totalTests;
            this.successRate = successRate;
            this.testResults = testResults;
            this.summary = summary;
            this.totalExecutionTime = totalExecutionTime;
        }
    }
    
    /**
     * Ejecuta código Python en un contenedor Docker seguro
     */
    public ExecutionResult executeCode(String code, String input) {
        String sessionId = UUID.randomUUID().toString();
        Path tempDir = null;
        
        try {
            // Crear directorio temporal
            tempDir = Files.createTempDirectory("robotcode_" + sessionId);
            Path codeFile = tempDir.resolve("solution.py");
            
            // Escribir código a archivo
            Files.write(codeFile, code.getBytes());
            
            // Comando Docker con limitaciones de recursos
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "run",
                "--rm",                                    // Eliminar contenedor después de ejecución
                "--memory=" + MEMORY_LIMIT,                // Límite de memoria
                "--cpus=" + CPU_LIMIT,                     // Límite de CPU
                "--network=none",                          // Sin acceso a red
                "--user=nobody",                           // Usuario sin privilegios
                "--read-only",                             // Sistema de archivos de solo lectura
                "--tmpfs=/tmp:rw,noexec,nosuid,size=10m", // Temporal limitado
                "-v", tempDir.toString() + ":/code:ro",    // Montar código como solo lectura
                "-w", "/code",                             // Directorio de trabajo
                DOCKER_IMAGE,
                "python3", "solution.py"
            );
            
            // Configurar entrada estándar
            pb.redirectErrorStream(false);
            
            long startTime = System.currentTimeMillis();
            Process process = pb.start();
            
            // Enviar input si existe
            if (input != null && !input.trim().isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.println(input);
                    writer.flush();
                }
            }
            
            // Esperar con timeout
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (!finished) {
                process.destroyForcibly();
                return new ExecutionResult("", "Timeout: La ejecución tardó más de " + TIMEOUT_SECONDS + " segundos", 
                                         -1, executionTime, true);
            }
            
            // Leer output y error
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            
            return new ExecutionResult(output, error, process.exitValue(), executionTime, false);
            
        } catch (Exception e) {
            logger.error("Error ejecutando código en Docker", e);
            return new ExecutionResult("", "Error interno: " + e.getMessage(), -1, 0, false);
        } finally {
            // Limpiar archivos temporales
            if (tempDir != null) {
                try {
                    Files.deleteIfExists(tempDir.resolve("solution.py"));
                    Files.deleteIfExists(tempDir);
                } catch (IOException e) {
                    logger.warn("Error limpiando archivos temporales", e);
                }
            }
        }
    }
    
    /**
     * Valida código contra múltiples test cases
     */
    public ValidationResult validateCode(String code, String testCasesJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode testCases = mapper.readTree(testCasesJson);
            
            List<TestResult> testResults = new java.util.ArrayList<>();
            int correctTests = 0;
            long totalExecutionTime = 0;
            
            for (JsonNode testCase : testCases) {
                String input = testCase.get("input").asText();
                String expected = testCase.get("expectedOutput").asText();
                
                ExecutionResult result = executeCode(code, input);
                totalExecutionTime += result.executionTime;
                
                boolean passed = false;
                String actual = "";
                String error = "";
                
                if (result.exitCode == 0 && !result.timedOut) {
                    actual = result.output.trim();
                    passed = actual.equals(expected.trim());
                    if (passed) correctTests++;
                } else {
                    error = result.error;
                }
                
                testResults.add(new TestResult(input, expected, actual, passed, error, result.executionTime));
            }
            
            int totalTests = testResults.size();
            double successRate = totalTests > 0 ? (double) correctTests / totalTests : 0;
            boolean passed = successRate >= 0.8; // 80% de tests deben pasar
            
            String summary = String.format("Pasó %d de %d test cases (%.1f%%)", 
                                          correctTests, totalTests, successRate * 100);
            
            return new ValidationResult(passed, correctTests, totalTests, successRate, 
                                      testResults, summary, totalExecutionTime);
            
        } catch (Exception e) {
            logger.error("Error validando código", e);
            return new ValidationResult(false, 0, 0, 0, 
                                      List.of(new TestResult("", "", "", false, 
                                                           "Error interno: " + e.getMessage(), 0)), 
                                      "Error en validación", 0);
        }
    }
    
    /**
     * Verifica si Docker está disponible
     */
    public boolean isDockerAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "--version");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Construye la imagen Docker si no existe
     */
    public boolean buildDockerImage() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "build", "-t", DOCKER_IMAGE, ".");
            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            logger.error("Error construyendo imagen Docker", e);
            return false;
        }
    }
    
    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }
}