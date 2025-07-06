package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.model.TestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class AdvancedCodeEvaluator {

    @Autowired
    private FlexibleCodeExecutor flexibleExecutor;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class EvaluationResult {
        public boolean allTestsPassed;
        public int totalTests;
        public int passedTests;
        public List<TestCaseResult> testResults;
        public String summary;
        public long totalExecutionTime;
        public CodeQualityMetrics qualityMetrics;
        
        public EvaluationResult() {
            this.testResults = new ArrayList<>();
            this.qualityMetrics = new CodeQualityMetrics();
        }
    }

    public static class TestCaseResult {
        public String input;
        public String expectedOutput;
        public String actualOutput;
        public boolean passed;
        public String errorMessage;
        public long executionTime;
        public String status;
        
        public TestCaseResult() {}
    }

    public static class CodeQualityMetrics {
        public int linesOfCode;
        public int cyclomaticComplexity;
        public boolean hasComments;
        public boolean hasDocstrings;
        public boolean followsNamingConventions;
        public String complexityLevel; // "LOW", "MEDIUM", "HIGH"
        
        public CodeQualityMetrics() {}
    }

    /**
     * Evalúa código usando múltiples estrategias
     */
    public EvaluationResult evaluateCode(Problema problema, String userCode, String language) {
        EvaluationResult result = new EvaluationResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // Parsear test cases del problema
            List<TestCase> testCases = parseTestCases(problema);
            result.totalTests = testCases.size();
            
            // Evaluar calidad del código
            result.qualityMetrics = analyzeCodeQuality(userCode);
            
            // Ejecutar cada test case
            for (TestCase testCase : testCases) {
                TestCaseResult testResult = executeTestCase(userCode, testCase, language);
                result.testResults.add(testResult);
                
                if (testResult.passed) {
                    result.passedTests++;
                }
            }
            
            result.allTestsPassed = result.passedTests == result.totalTests;
            result.totalExecutionTime = System.currentTimeMillis() - startTime;
            
            // Generar resumen
            result.summary = generateSummary(result);
            
        } catch (Exception e) {
            result.allTestsPassed = false;
            result.summary = "Error en evaluación: " + e.getMessage();
            result.totalExecutionTime = System.currentTimeMillis() - startTime;
        }
        
        return result;
    }

    /**
     * Ejecuta un test case individual
     */
    private TestCaseResult executeTestCase(String userCode, TestCase testCase, String language) {
        TestCaseResult result = new TestCaseResult();
        result.input = testCase.getInput();
        result.expectedOutput = testCase.getExpectedOutput();
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Usar el flexible executor mejorado
            FlexibleCodeExecutor.ExecutionResult execution = 
                flexibleExecutor.executePythonFlexibly(userCode, testCase.getInput());
            
            result.executionTime = System.currentTimeMillis() - startTime;
            
            if (execution.status == FlexibleCodeExecutor.ExecutionStatus.SUCCESS) {
                result.actualOutput = execution.stdout;
                result.passed = flexibleExecutor.compareOutputsFlexibly(
                    testCase.getExpectedOutput(), execution.stdout);
                result.status = result.passed ? "PASSED" : "FAILED";
                
                if (!result.passed) {
                    result.errorMessage = generateComparisonError(
                        testCase.getExpectedOutput(), execution.stdout);
                }
            } else {
                result.passed = false;
                result.status = "ERROR";
                result.errorMessage = execution.errorMessage;
                result.actualOutput = "";
            }
            
        } catch (Exception e) {
            result.passed = false;
            result.status = "ERROR";
            result.errorMessage = "Error de ejecución: " + e.getMessage();
            result.actualOutput = "";
            result.executionTime = System.currentTimeMillis() - startTime;
        }
        
        return result;
    }

    /**
     * Analiza la calidad del código
     */
    private CodeQualityMetrics analyzeCodeQuality(String code) {
        CodeQualityMetrics metrics = new CodeQualityMetrics();
        
        String[] lines = code.split("\n");
        metrics.linesOfCode = lines.length;
        
        // Contar comentarios
        int commentLines = 0;
        boolean hasDocstrings = false;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                commentLines++;
            }
            if (trimmed.contains("\"\"\"") || trimmed.contains("'''")) {
                hasDocstrings = true;
            }
        }
        
        metrics.hasComments = commentLines > 0;
        metrics.hasDocstrings = hasDocstrings;
        
        // Calcular complejidad ciclomática (simplificada)
        metrics.cyclomaticComplexity = calculateCyclomaticComplexity(code);
        
        // Verificar convenciones de nombres
        metrics.followsNamingConventions = checkNamingConventions(code);
        
        // Determinar nivel de complejidad
        if (metrics.cyclomaticComplexity <= 5) {
            metrics.complexityLevel = "LOW";
        } else if (metrics.cyclomaticComplexity <= 10) {
            metrics.complexityLevel = "MEDIUM";
        } else {
            metrics.complexityLevel = "HIGH";
        }
        
        return metrics;
    }

    /**
     * Calcula la complejidad ciclomática simplificada
     */
    private int calculateCyclomaticComplexity(String code) {
        int complexity = 1; // Base complexity
        
        // Contar estructuras de control
        String[] controlStructures = {
            "if\\s+", "elif\\s+", "else\\s*:", "for\\s+", "while\\s+", 
            "try\\s*:", "except\\s+", "finally\\s*:", "with\\s+",
            "and\\s+", "or\\s+", "\\|\\|", "&&"
        };
        
        for (String pattern : controlStructures) {
            Pattern p = Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(code);
            while (m.find()) {
                complexity++;
            }
        }
        
        return complexity;
    }

    /**
     * Verifica convenciones de nombres de Python
     */
    private boolean checkNamingConventions(String code) {
        // Verificar que las funciones usen snake_case
        Pattern functionPattern = Pattern.compile("def\\s+([a-z_][a-z0-9_]*)\\s*\\(");
        java.util.regex.Matcher m = functionPattern.matcher(code);
        
        while (m.find()) {
            String functionName = m.group(1);
            if (!functionName.matches("[a-z_][a-z0-9_]*")) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Parsea los test cases del problema
     */
    private List<TestCase> parseTestCases(Problema problema) {
        List<TestCase> testCases = new ArrayList<>();
        
        try {
            if (problema.getTestCasesJson() != null && !problema.getTestCasesJson().isEmpty()) {
                List<Map<String, String>> testData = objectMapper.readValue(
                    problema.getTestCasesJson(), 
                    new TypeReference<List<Map<String, String>>>() {}
                );
                
                for (Map<String, String> testDataItem : testData) {
                    TestCase testCase = new TestCase();
                    testCase.setInput(testDataItem.get("input"));
                    testCase.setExpectedOutput(testDataItem.get("expectedOutput"));
                    testCases.add(testCase);
                }
            }
        } catch (Exception e) {
            // Si falla el parsing, crear test cases por defecto
            System.err.println("Error parsing test cases: " + e.getMessage());
        }
        
        return testCases;
    }

    /**
     * Genera mensaje de error de comparación
     */
    private String generateComparisonError(String expected, String actual) {
        return String.format(
            "Diferencia encontrada (comparación flexible):\n" +
            "Esperado: '%s'\n" +
            "Obtenido: '%s'\n" +
            "Normalizado esperado: '%s'\n" +
            "Normalizado obtenido: '%s'",
            expected, actual,
            flexibleExecutor.normalizeOutput(expected),
            flexibleExecutor.normalizeOutput(actual)
        );
    }

    /**
     * Genera resumen de la evaluación
     */
    private String generateSummary(EvaluationResult result) {
        double percentage = result.totalTests > 0 ? 
            (double) result.passedTests / result.totalTests * 100 : 0;
        
        return String.format(
            "Pasó %d de %d test cases (%.1f%%) - Complejidad: %s",
            result.passedTests, result.totalTests, percentage,
            result.qualityMetrics.complexityLevel
        );
    }
} 