package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.repository.ProblemaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProblemTestingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProblemTestingService.class);

    @Autowired
    private CodeExecutionEngine executionEngine;

    @Autowired
    private FlexibleCodeExecutor flexibleExecutor;

    @Autowired
    private ProblemaRepository problemaRepository;

    @Autowired
    private DockerExecutorService dockerExecutorService;

    @Autowired
    private PistonExecutorService pistonExecutorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TestCase {
        public String input;
        public String expectedOutput;
        public String description;
        
        public TestCase() {}
        public TestCase(String input, String expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
        public TestCase(String input, String expectedOutput, String description) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.description = description;
        }
        
        // Getters y setters
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TestResult {
        public String input;
        public String expectedOutput;
        public String actualOutput;
        public boolean passed;
        public String errorMessage;
        public long executionTime;
        public TestStatus status;
        
        public TestResult() {}
        
        // Getters y setters
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
        public String getActualOutput() { return actualOutput; }
        public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public TestStatus getStatus() { return status; }
        public void setStatus(TestStatus status) { this.status = status; }
    }

    public static class ValidationReport {
        public Long problemId;
        public String problemTitle;
        public boolean allTestsPassed;
        public int totalTests;
        public int passedTests;
        public List<TestResult> testResults;
        public String summary;
        public long totalExecutionTime;
        
        public ValidationReport() {
            this.testResults = new ArrayList<>();
        }
        
        // Getters y setters
        public Long getProblemId() { return problemId; }
        public void setProblemId(Long problemId) { this.problemId = problemId; }
        public String getProblemTitle() { return problemTitle; }
        public void setProblemTitle(String problemTitle) { this.problemTitle = problemTitle; }
        public boolean isAllTestsPassed() { return allTestsPassed; }
        public void setAllTestsPassed(boolean allTestsPassed) { this.allTestsPassed = allTestsPassed; }
        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
        public int getPassedTests() { return passedTests; }
        public void setPassedTests(int passedTests) { this.passedTests = passedTests; }
        public List<TestResult> getTestResults() { return testResults; }
        public void setTestResults(List<TestResult> testResults) { this.testResults = testResults; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public void setTotalExecutionTime(long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }
    }

    public enum TestStatus {
        PASSED,
        FAILED,
        ERROR,
        TIMEOUT,
        COMPILATION_ERROR
    }

    /**
     * Busca un problema por ID
     */
    public Problema findProblemaById(Long problemId) {
        Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
        return problemaOpt.orElse(null);
    }

    /**
     * Ejecuta y valida el código del usuario contra los test cases del problema
     */
    public ValidationReport validateUserCode(Long problemId, String userCode, String language) {
        ValidationReport report = new ValidationReport();
        
        try {
            // Obtener el problema de la base de datos
            Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
            if (problemaOpt.isEmpty()) {
                report.summary = "Problema no encontrado";
                return report;
            }
            
            Problema problema = problemaOpt.get();
            report.problemId = problemId;
            report.problemTitle = problema.getTitulo();
            
            // Parsear test cases del JSON
            List<TestCase> testCases = parseTestCases(problema.getTestCasesJson());
            report.totalTests = testCases.size();
            
            // Verificar disponibilidad de Piston API una sola vez al inicio
            boolean pistonAvailable = false;
            if ("python3".equals(language) || "python".equals(language)) {
                pistonAvailable = pistonExecutorService.isPistonAvailable();
                logger.info("Piston API availability for problem {}: {}", problemId, pistonAvailable);
            }
            
            long totalTime = 0;
            int passedCount = 0;
            
            // Ejecutar cada test case
            for (int i = 0; i < testCases.size(); i++) {
                TestCase testCase = testCases.get(i);
                TestResult testResult = executeTestCaseWithMethod(userCode, language, testCase, i + 1, pistonAvailable);
                
                report.testResults.add(testResult);
                totalTime += testResult.executionTime;
                
                if (testResult.passed) {
                    passedCount++;
                }
            }
            
            report.passedTests = passedCount;
            report.allTestsPassed = (passedCount == testCases.size());
            report.totalExecutionTime = totalTime;
            report.summary = String.format("Pasó %d de %d test cases (%.1f%%)", 
                passedCount, testCases.size(), (passedCount * 100.0) / testCases.size());
            
            return report;
            
        } catch (Exception e) {
            report.summary = "Error durante la validación: " + e.getMessage();
            return report;
        }
    }

    /**
     * Valida la solución oficial de un problema contra sus test cases
     */
    public ValidationReport validateOfficialSolution(Long problemId) {
        ValidationReport report = new ValidationReport();
        
        try {
            Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
            if (problemaOpt.isEmpty()) {
                report.summary = "Problema no encontrado";
                return report;
            }
            
            Problema problema = problemaOpt.get();
            
            if (problema.getSolucionCorrecta() == null || problema.getSolucionCorrecta().trim().isEmpty()) {
                report.summary = "No hay solución oficial para validar";
                return report;
            }
            
            return validateUserCode(problemId, problema.getSolucionCorrecta(), "python3");
            
        } catch (Exception e) {
            report.summary = "Error validando solución oficial: " + e.getMessage();
            return report;
        }
    }

    /**
     * Valida código del usuario usando el sistema flexible
     */
    public ValidationReport validateUserCodeFlexibly(Long problemId, String userCode, String language) {
        ValidationReport report = new ValidationReport();
        
        try {
            // Obtener el problema de la base de datos
            Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
            if (problemaOpt.isEmpty()) {
                report.summary = "Problema no encontrado";
                return report;
            }
            
            Problema problema = problemaOpt.get();
            report.problemId = problemId;
            report.problemTitle = problema.getTitulo();
            
            // Parsear test cases del JSON
            List<TestCase> testCases = parseTestCases(problema.getTestCasesJson());
            report.totalTests = testCases.size();
            
            long totalTime = 0;
            int passedCount = 0;
            
            // Ejecutar cada test case usando el sistema flexible
            for (int i = 0; i < testCases.size(); i++) {
                TestCase testCase = testCases.get(i);
                TestResult testResult = executeTestCaseFlexibly(userCode, language, testCase, i + 1);
                
                report.testResults.add(testResult);
                totalTime += testResult.executionTime;
                
                if (testResult.passed) {
                    passedCount++;
                }
            }
            
            report.passedTests = passedCount;
            report.allTestsPassed = (passedCount == testCases.size());
            report.totalExecutionTime = totalTime;
            report.summary = String.format("Pasó %d de %d test cases (%.1f%%)", 
                passedCount, testCases.size(), (passedCount * 100.0) / testCases.size());
            
            return report;
            
        } catch (Exception e) {
            report.summary = "Error durante la validación flexible: " + e.getMessage();
            return report;
        }
    }

    /**
     * Ejecuta un test case usando el sistema flexible
     */
    private TestResult executeTestCaseFlexibly(String userCode, String language, TestCase testCase, int testNumber) {
        TestResult result = new TestResult();
        result.input = testCase.input;
        result.expectedOutput = testCase.expectedOutput;
        
        try {
            // Priorizar el ejecutor Docker para Python si está disponible
            if ("python3".equals(language) || "python".equals(language)) {
                // Intentar con Docker primero
                if (dockerExecutorService.isDockerAvailable()) {
                    DockerExecutorService.ExecutionResult dockerResult = dockerExecutorService.executeCode(userCode, testCase.input);
                    result.executionTime = dockerResult.executionTime;
                    
                    if (dockerResult.exitCode == 0 && !dockerResult.timedOut) {
                        result.actualOutput = dockerResult.output.trim();
                        result.passed = flexibleExecutor.compareOutputsFlexibly(result.expectedOutput, result.actualOutput);
                        result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                        
                        if (!result.passed) {
                            result.errorMessage = generateFlexibleDiffMessage(result.expectedOutput, result.actualOutput);
                        }
                    } else {
                        result.passed = false;
                        result.actualOutput = dockerResult.error;
                        result.errorMessage = dockerResult.error;
                        result.status = dockerResult.timedOut ? TestStatus.TIMEOUT : TestStatus.ERROR;
                    }
                } else {
                    // Fallback al ejecutor flexible si Docker no está disponible
                    FlexibleCodeExecutor.ExecutionResult execResult = flexibleExecutor.executePythonFlexibly(userCode, testCase.input);
                    result.executionTime = execResult.executionTime;
                    
                    if (execResult.status == FlexibleCodeExecutor.ExecutionStatus.SUCCESS) {
                        result.actualOutput = execResult.stdout.trim();
                        // Usar comparación flexible
                        result.passed = flexibleExecutor.compareOutputsFlexibly(result.expectedOutput, result.actualOutput);
                        result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                        
                        if (!result.passed) {
                            result.errorMessage = generateFlexibleDiffMessage(result.expectedOutput, result.actualOutput);
                        }
                    } else {
                        result.passed = false;
                        result.actualOutput = execResult.stderr;
                        result.errorMessage = execResult.errorMessage;
                        
                        switch (execResult.status) {
                            case COMPILATION_ERROR:
                                result.status = TestStatus.COMPILATION_ERROR;
                                break;
                            case TIME_LIMIT_EXCEEDED:
                                result.status = TestStatus.TIMEOUT;
                                break;
                            default:
                                result.status = TestStatus.ERROR;
                                break;
                        }
                    }
                }
            } else {
                // Para otros lenguajes, usar el método original
                String completeCode = createExecutableCode(userCode, testCase.input);
                
                CodeExecutionEngine.ExecutionRequest request = new CodeExecutionEngine.ExecutionRequest(
                    completeCode, language, ""
                );
                
                CodeExecutionEngine.ExecutionResult execResult = executionEngine.executeCode(request);
                result.executionTime = execResult.executionTime;
                
                if (execResult.status == CodeExecutionEngine.ExecutionStatus.SUCCESS) {
                    result.actualOutput = execResult.stdout.trim();
                    result.passed = result.actualOutput.equals(result.expectedOutput.trim());
                    result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                    
                    if (!result.passed) {
                        result.errorMessage = generateDiffMessage(result.expectedOutput, result.actualOutput);
                    }
                } else {
                    result.passed = false;
                    result.actualOutput = execResult.stderr;
                    result.errorMessage = execResult.errorMessage;
                    
                    switch (execResult.status) {
                        case COMPILATION_ERROR:
                            result.status = TestStatus.COMPILATION_ERROR;
                            break;
                        case TIME_LIMIT_EXCEEDED:
                            result.status = TestStatus.TIMEOUT;
                            break;
                        default:
                            result.status = TestStatus.ERROR;
                            break;
                    }
                }
            }
            
        } catch (Exception e) {
            result.passed = false;
            result.status = TestStatus.ERROR;
            result.errorMessage = "Error ejecutando test flexible: " + e.getMessage();
        }
        
        return result;
    }

    /**
     * Prueba el sistema flexible con un problema específico de la base de datos
     */
    public Map<String, Object> testFlexibleWithProblem(Long problemId) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            Optional<Problema> problemaOpt = problemaRepository.findById(problemId);
            if (problemaOpt.isEmpty()) {
                result.put("problemId", problemId);
                result.put("problemTitle", "No encontrado");
                result.put("status", "NOT_FOUND");
                return result;
            }
            
            Problema problema = problemaOpt.get();
            result.put("problemId", problemId);
            result.put("problemTitle", problema.getTitulo());
            
            if (problema.getSolucionCorrecta() == null || problema.getSolucionCorrecta().trim().isEmpty()) {
                result.put("status", "NO_SOLUTION");
                result.put("message", "No hay solución oficial");
                return result;
            }
            
            // Validar usando el sistema flexible
            ValidationReport report = validateUserCodeFlexibly(problemId, problema.getSolucionCorrecta(), "python3");
            
            result.put("status", "SUCCESS");
            result.put("allTestsPassed", report.allTestsPassed);
            result.put("passedTests", report.passedTests);
            result.put("totalTests", report.totalTests);
            result.put("executionTime", report.totalExecutionTime);
            result.put("summary", report.summary);
            
            // Agregar detalles de los test cases
            List<Map<String, Object>> testDetails = new java.util.ArrayList<>();
            for (TestResult testResult : report.testResults) {
                Map<String, Object> testDetail = new java.util.HashMap<>();
                testDetail.put("input", testResult.input);
                testDetail.put("expected", testResult.expectedOutput);
                testDetail.put("actual", testResult.actualOutput);
                testDetail.put("passed", testResult.passed);
                testDetail.put("status", testResult.status.toString());
                testDetails.add(testDetail);
            }
            result.put("testDetails", testDetails);
            
        } catch (Exception e) {
            result.put("problemId", problemId);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    private TestResult executeTestCaseWithMethod(String userCode, String language, TestCase testCase, int testNumber, boolean usePiston) {
        TestResult result = new TestResult();
        result.input = testCase.input;
        result.expectedOutput = testCase.expectedOutput;
        
        try {
            // Priorizar Piston API para Python (más confiable y gratuito)
            if ("python3".equals(language) || "python".equals(language)) {
                // Usar Piston API si está disponible
                if (usePiston) {
                    logger.info("Using Piston API for test {}", testNumber);
                    PistonExecutorService.ExecutionResult pistonResult = pistonExecutorService.executeCode(userCode, testCase.input);
                    result.executionTime = pistonResult.executionTime;
                    
                    if (pistonResult.success) {
                        result.actualOutput = pistonResult.output.trim();
                        result.passed = flexibleExecutor.compareOutputsFlexibly(result.expectedOutput, result.actualOutput);
                        result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                        
                        if (!result.passed) {
                            result.errorMessage = generateFlexibleDiffMessage(result.expectedOutput, result.actualOutput);
                        }
                    } else {
                        // Si Piston falla con rate limiting, marcar como tal pero no como error grave
                        result.passed = false;
                        if (pistonResult.errorMessage != null && pistonResult.errorMessage.contains("429")) {
                            result.actualOutput = "Rate limiting - intenta nuevamente en unos segundos";
                            result.errorMessage = "Piston API temporalmente saturado (rate limit)";
                        } else {
                            result.actualOutput = pistonResult.stderr;
                            result.errorMessage = pistonResult.errorMessage;
                        }
                        result.status = TestStatus.ERROR;
                    }
                } else if (dockerExecutorService.isDockerAvailable()) {
                    // Fallback a Docker si Piston no está disponible
                    DockerExecutorService.ExecutionResult dockerResult = dockerExecutorService.executeCode(userCode, testCase.input);
                    result.executionTime = dockerResult.executionTime;
                    
                    if (dockerResult.exitCode == 0 && !dockerResult.timedOut) {
                        result.actualOutput = dockerResult.output.trim();
                        result.passed = flexibleExecutor.compareOutputsFlexibly(result.expectedOutput, result.actualOutput);
                        result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                        
                        if (!result.passed) {
                            result.errorMessage = generateFlexibleDiffMessage(result.expectedOutput, result.actualOutput);
                        }
                    } else {
                        result.passed = false;
                        result.actualOutput = dockerResult.error;
                        result.errorMessage = dockerResult.error;
                        result.status = dockerResult.timedOut ? TestStatus.TIMEOUT : TestStatus.ERROR;
                    }
                } else {
                    // Fallback al ejecutor flexible si ni Piston ni Docker están disponibles
                    FlexibleCodeExecutor.ExecutionResult execResult = flexibleExecutor.executePythonFlexibly(userCode, testCase.input);
                    result.executionTime = execResult.executionTime;
                    
                    if (execResult.status == FlexibleCodeExecutor.ExecutionStatus.SUCCESS) {
                        result.actualOutput = execResult.stdout.trim();
                        // Usar comparación flexible
                        result.passed = flexibleExecutor.compareOutputsFlexibly(result.expectedOutput, result.actualOutput);
                        result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                        
                        if (!result.passed) {
                            result.errorMessage = generateFlexibleDiffMessage(result.expectedOutput, result.actualOutput);
                        }
                    } else {
                        result.passed = false;
                        result.actualOutput = execResult.stderr;
                        result.errorMessage = execResult.errorMessage;
                        
                        switch (execResult.status) {
                            case COMPILATION_ERROR:
                                result.status = TestStatus.COMPILATION_ERROR;
                                break;
                            case TIME_LIMIT_EXCEEDED:
                                result.status = TestStatus.TIMEOUT;
                                break;
                            default:
                                result.status = TestStatus.ERROR;
                                break;
                        }
                    }
                }
            } else {
                // Para otros lenguajes, usar el método original
                String completeCode = createExecutableCode(userCode, testCase.input);
                
                CodeExecutionEngine.ExecutionRequest request = new CodeExecutionEngine.ExecutionRequest(
                    completeCode, language, ""
                );
                
                CodeExecutionEngine.ExecutionResult execResult = executionEngine.executeCode(request);
                result.executionTime = execResult.executionTime;
                
                if (execResult.status == CodeExecutionEngine.ExecutionStatus.SUCCESS) {
                    result.actualOutput = execResult.stdout.trim();
                    result.passed = result.actualOutput.equals(result.expectedOutput.trim());
                    result.status = result.passed ? TestStatus.PASSED : TestStatus.FAILED;
                    
                    if (!result.passed) {
                        result.errorMessage = generateDiffMessage(result.expectedOutput, result.actualOutput);
                    }
                } else {
                    result.passed = false;
                    result.actualOutput = execResult.stderr;
                    result.errorMessage = execResult.errorMessage;
                    
                    switch (execResult.status) {
                        case COMPILATION_ERROR:
                            result.status = TestStatus.COMPILATION_ERROR;
                            break;
                        case TIME_LIMIT_EXCEEDED:
                            result.status = TestStatus.TIMEOUT;
                            break;
                        default:
                            result.status = TestStatus.ERROR;
                            break;
                    }
                }
            }
            
        } catch (Exception e) {
            result.passed = false;
            result.status = TestStatus.ERROR;
            result.errorMessage = "Error ejecutando test: " + e.getMessage();
        }
        
        return result;
    }

    private String createExecutableCode(String userCode, String input) {
        // Extraer el nombre de la función del código del usuario
        String functionName = extractFunctionName(userCode);
        if (functionName == null) {
            throw new RuntimeException("No se pudo encontrar una función en el código del usuario");
        }
        
        StringBuilder executableCode = new StringBuilder();
        executableCode.append(userCode).append("\n\n");
        executableCode.append("# Código de ejecución automática\n");
        executableCode.append("try:\n");
        executableCode.append("    input_str = ").append(quoteString(input)).append("\n");
        executableCode.append("    \n");
        executableCode.append("    if ',' in input_str:\n");
        executableCode.append("        # Múltiples parámetros\n");
        executableCode.append("        params_str = input_str.split(',')\n");
        executableCode.append("        params = []\n");
        executableCode.append("        for param in params_str:\n");
        executableCode.append("            param = param.strip()\n");
        executableCode.append("            try:\n");
        executableCode.append("                if '.' in param:\n");
        executableCode.append("                    params.append(float(param))\n");
        executableCode.append("                else:\n");
        executableCode.append("                    params.append(int(param))\n");
        executableCode.append("            except ValueError:\n");
        executableCode.append("                if param.startswith(\"'\") and param.endswith(\"'\"):\n");
        executableCode.append("                    params.append(param[1:-1])\n");
        executableCode.append("                elif param.startswith('\"') and param.endswith('\"'):\n");
        executableCode.append("                    params.append(param[1:-1])\n");
        executableCode.append("                else:\n");
        executableCode.append("                    params.append(param)\n");
        executableCode.append("        result = ").append(functionName).append("(*params)\n");
        executableCode.append("    else:\n");
        executableCode.append("        # Un solo parámetro\n");
        executableCode.append("        param = input_str.strip()\n");
        executableCode.append("        try:\n");
        executableCode.append("            if '.' in param:\n");
        executableCode.append("                param = float(param)\n");
        executableCode.append("            else:\n");
        executableCode.append("                param = int(param)\n");
        executableCode.append("        except ValueError:\n");
        executableCode.append("            if param.startswith(\"'\") and param.endswith(\"'\"):\n");
        executableCode.append("                param = param[1:-1]\n");
        executableCode.append("            elif param.startswith('\"') and param.endswith('\"'):\n");
        executableCode.append("                param = param[1:-1]\n");
        executableCode.append("        result = ").append(functionName).append("(param)\n");
        executableCode.append("    \n");
        executableCode.append("    print(result)\n");
        executableCode.append("except Exception as e:\n");
        executableCode.append("    print(f'Error: {e}')\n");
        
        return executableCode.toString();
    }

    private String extractFunctionName(String code) {
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

    private String quoteString(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private List<TestCase> parseTestCases(String testCasesJson) throws Exception {
        TypeReference<List<TestCase>> typeRef = new TypeReference<List<TestCase>>() {};
        return objectMapper.readValue(testCasesJson, typeRef);
    }

    private String generateDiffMessage(String expected, String actual) {
        StringBuilder diff = new StringBuilder();
        diff.append("Diferencia encontrada:\n");
        diff.append("Esperado: '").append(expected).append("'\n");
        diff.append("Obtenido: '").append(actual).append("'\n");
        
        if (expected.length() != actual.length()) {
            diff.append("Longitud esperada: ").append(expected.length()).append("\n");
            diff.append("Longitud obtenida: ").append(actual.length()).append("\n");
        }
        
        // Buscar primera diferencia carácter por carácter
        int diffIndex = -1;
        int minLength = Math.min(expected.length(), actual.length());
        for (int i = 0; i < minLength; i++) {
            if (expected.charAt(i) != actual.charAt(i)) {
                diffIndex = i;
                break;
            }
        }
        
        if (diffIndex >= 0) {
            diff.append("Primera diferencia en posición ").append(diffIndex).append("\n");
        }
        
        return diff.toString();
    }

    private String generateFlexibleDiffMessage(String expected, String actual) {
        StringBuilder diff = new StringBuilder();
        diff.append("Diferencia encontrada (comparación flexible):\n");
        diff.append("Esperado: '").append(expected).append("'\n");
        diff.append("Obtenido: '").append(actual).append("'\n");
        
        // Normalizar ambos outputs para mostrar la comparación
        String normalizedExpected = normalizeOutput(expected);
        String normalizedActual = normalizeOutput(actual);
        
        diff.append("Normalizado esperado: '").append(normalizedExpected).append("'\n");
        diff.append("Normalizado obtenido: '").append(normalizedActual).append("'\n");
        
        // Información adicional sobre el tipo de comparación
        if (isNumeric(expected) && isNumeric(actual)) {
            try {
                double expectedNum = Double.parseDouble(expected);
                double actualNum = Double.parseDouble(actual);
                double difference = Math.abs(expectedNum - actualNum);
                diff.append("Diferencia numérica: ").append(difference).append("\n");
                diff.append("Tolerancia: ").append(1e-6).append("\n");
            } catch (NumberFormatException e) {
                // Ignorar si no se pueden parsear como números
            }
        }
        
        if (looksLikeList(expected) && looksLikeList(actual)) {
            diff.append("Comparación de listas detectada\n");
        }
        
        if (looksLikeDict(expected) && looksLikeDict(actual)) {
            diff.append("Comparación de diccionarios detectada\n");
        }
        
        return diff.toString();
    }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        
        return output
            .trim()
            .replaceAll("\\s+", " ") // Reemplazar múltiples espacios con uno solo
            .replaceAll("\\n\\s*", " ") // Reemplazar saltos de línea con espacios
            .replaceAll("\\s+$", "") // Eliminar espacios al final
            .replaceAll("^\\s+", ""); // Eliminar espacios al inicio
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean looksLikeList(String str) {
        return str != null && str.startsWith("[") && str.endsWith("]");
    }

    private boolean looksLikeDict(String str) {
        return str != null && str.startsWith("{") && str.endsWith("}");
    }
}