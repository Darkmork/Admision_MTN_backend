package com.desafios.robotcode.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

@Service
public class PistonExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(PistonExecutorService.class);
    private static final String PISTON_API_URL = "https://emkc.org/api/v2/piston/execute";
    private static final String PYTHON_VERSION = "3.10.0";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public PistonExecutorService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public static class ExecutionResult {
        public String output;
        public String stderr;
        public int exitCode;
        public boolean success;
        public long executionTime;
        public String errorMessage;
        
        public ExecutionResult(String output, String stderr, int exitCode, boolean success, long executionTime, String errorMessage) {
            this.output = output;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.success = success;
            this.executionTime = executionTime;
            this.errorMessage = errorMessage;
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
     * Ejecuta código Python usando Piston API
     */
    public ExecutionResult executeCode(String code, String input) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Preparar el payload para Piston API
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("language", "python");
            payload.put("version", PYTHON_VERSION);
            payload.put("stdin", input);
            
            // Crear array de archivos
            ArrayNode files = objectMapper.createArrayNode();
            ObjectNode file = objectMapper.createObjectNode();
            file.put("content", code);
            files.add(file);
            payload.set("files", files);
            
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Crear request
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            
            // Enviar request a Piston
            ResponseEntity<String> response = restTemplate.postForEntity(PISTON_API_URL, request, String.class);
            
            // Procesar respuesta
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode runResult = responseJson.get("run");
            
            String output = runResult.get("stdout").asText();
            String stderr = runResult.get("stderr").asText();
            int exitCode = runResult.get("code").asInt();
            boolean success = exitCode == 0;
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return new ExecutionResult(
                output.trim(), 
                stderr, 
                exitCode, 
                success, 
                executionTime, 
                success ? null : stderr
            );
            
        } catch (Exception e) {
            logger.error("Error ejecutando código con Piston API", e);
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecutionResult(
                "", 
                "Error de conexión con Piston API: " + e.getMessage(), 
                -1, 
                false, 
                executionTime, 
                e.getMessage()
            );
        }
    }
    
    /**
     * Valida código contra múltiples test cases usando Piston API
     */
    public ValidationResult validateCode(String code, String testCasesJson) {
        try {
            JsonNode testCases = objectMapper.readTree(testCasesJson);
            
            List<TestResult> testResults = new ArrayList<>();
            int correctTests = 0;
            long totalExecutionTime = 0;
            
            for (JsonNode testCase : testCases) {
                String input = testCase.get("input").asText();
                String expected = testCase.get("expectedOutput").asText();
                
                ExecutionResult result = executeCode(code, input);
                totalExecutionTime += result.executionTime;
                
                // Rate limiting: esperar 300ms entre requests para evitar 429 errors
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                boolean passed = false;
                String actual = "";
                String error = "";
                
                if (result.success) {
                    actual = result.output.trim();
                    passed = actual.equals(expected.trim());
                    if (passed) correctTests++;
                } else {
                    error = result.stderr;
                    actual = "Error: " + error;
                }
                
                testResults.add(new TestResult(input, expected, actual, passed, error, result.executionTime));
            }
            
            int totalTests = testResults.size();
            double successRate = totalTests > 0 ? (double) correctTests / totalTests : 0;
            boolean passed = successRate >= 0.8; // 80% de tests deben pasar
            
            String summary = String.format("Pasó %d de %d test cases (%.1f%%) usando Piston API", 
                                          correctTests, totalTests, successRate * 100);
            
            return new ValidationResult(passed, correctTests, totalTests, successRate, 
                                      testResults, summary, totalExecutionTime);
            
        } catch (Exception e) {
            logger.error("Error validando código con Piston API", e);
            return new ValidationResult(false, 0, 0, 0, 
                                      List.of(new TestResult("", "", "", false, 
                                                           "Error interno: " + e.getMessage(), 0)), 
                                      "Error en validación con Piston API", 0);
        }
    }
    
    /**
     * Verifica si Piston API está disponible
     */
    public boolean isPistonAvailable() {
        try {
            // Test simple para verificar conectividad
            String testCode = "print('test')";
            ExecutionResult result = executeCode(testCode, "");
            return result.success && result.output.contains("test");
        } catch (Exception e) {
            logger.warn("Piston API no disponible", e);
            return false;
        }
    }
    
    /**
     * Obtiene información sobre Piston API
     */
    public String getPistonInfo() {
        return String.format("Piston API - URL: %s, Python Version: %s", PISTON_API_URL, PYTHON_VERSION);
    }
}