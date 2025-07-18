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
            // Crear código ejecutable que llame a la función automáticamente
            String executableCode = createExecutableCode(code, input);
            
            // Preparar el payload para Piston API
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("language", "python");
            payload.put("version", PYTHON_VERSION);
            payload.put("stdin", "");  // Sin stdin ya que pasamos input directamente al código
            
            // Crear array de archivos
            ArrayNode files = objectMapper.createArrayNode();
            ObjectNode file = objectMapper.createObjectNode();
            file.put("content", executableCode);
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
                
                // Rate limiting: esperar 600ms ANTES de cada request para evitar 429 errors
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                ExecutionResult result = executeCode(code, input);
                totalExecutionTime += result.executionTime;
                
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
            // Test simple para verificar conectividad (sin usar executeCode para evitar rate limiting)
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("language", "python");
            payload.put("version", PYTHON_VERSION);
            payload.put("stdin", "");
            
            ArrayNode files = objectMapper.createArrayNode();
            ObjectNode file = objectMapper.createObjectNode();
            file.put("content", "print('test')");
            files.add(file);
            payload.set("files", files);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(PISTON_API_URL, request, String.class);
            
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            JsonNode runResult = responseJson.get("run");
            
            boolean available = runResult.get("code").asInt() == 0 && 
                               runResult.get("stdout").asText().contains("test");
            
            logger.info("Piston API availability check: {} (status: {})", available, response.getStatusCode());
            return available;
        } catch (Exception e) {
            logger.error("Piston API no disponible", e);
            return false;
        }
    }
    
    /**
     * Obtiene información sobre Piston API
     */
    public String getPistonInfo() {
        return String.format("Piston API - URL: %s, Python Version: %s", PISTON_API_URL, PYTHON_VERSION);
    }
    
    /**
     * Crea código ejecutable que llama automáticamente a la función definida
     */
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
        executableCode.append("    import ast\n");
        executableCode.append("    input_str = ").append(quoteString(input)).append("\n");
        executableCode.append("    \n");
        executableCode.append("    # Parsing robusto de parámetros de entrada\n");
        executableCode.append("    def parse_input(input_str):\n");
        executableCode.append("        # Casos especiales\n");
        executableCode.append("        if input_str.lower() == 'null' or input_str.lower() == 'none':\n");
        executableCode.append("            return None\n");
        executableCode.append("        if input_str.lower() == 'true':\n");
        executableCode.append("            return True\n");
        executableCode.append("        if input_str.lower() == 'false':\n");
        executableCode.append("            return False\n");
        executableCode.append("        \n");
        executableCode.append("        # Intentar evaluar como expresión Python\n");
        executableCode.append("        try:\n");
        executableCode.append("            return ast.literal_eval(input_str)\n");
        executableCode.append("        except (ValueError, SyntaxError):\n");
        executableCode.append("            pass\n");
        executableCode.append("        \n");
        executableCode.append("        # Intentar como número\n");
        executableCode.append("        try:\n");
        executableCode.append("            if '.' in input_str:\n");
        executableCode.append("                return float(input_str)\n");
        executableCode.append("            else:\n");
        executableCode.append("                return int(input_str)\n");
        executableCode.append("        except ValueError:\n");
        executableCode.append("            pass\n");
        executableCode.append("        \n");
        executableCode.append("        # Si nada funciona, devolver como string\n");
        executableCode.append("        return input_str\n");
        executableCode.append("    \n");
        executableCode.append("    # Parsing inteligente de parámetros - manejar casos complejos\n");
        executableCode.append("    def smart_param_split(input_str):\n");
        executableCode.append("        # Si no hay comas, es un solo parámetro\n");
        executableCode.append("        if ',' not in input_str:\n");
        executableCode.append("            return [input_str]\n");
        executableCode.append("        \n");
        executableCode.append("        # Split inteligente que respeta estructuras anidadas\n");
        executableCode.append("        params = []\n");
        executableCode.append("        current_param = ''\n");
        executableCode.append("        bracket_depth = 0\n");
        executableCode.append("        paren_depth = 0\n");
        executableCode.append("        brace_depth = 0\n");
        executableCode.append("        in_string = False\n");
        executableCode.append("        string_char = None\n");
        executableCode.append("        \n");
        executableCode.append("        for char in input_str:\n");
        executableCode.append("            if char in ['\"', \"'\"] and not in_string:\n");
        executableCode.append("                in_string = True\n");
        executableCode.append("                string_char = char\n");
        executableCode.append("            elif char == string_char and in_string:\n");
        executableCode.append("                in_string = False\n");
        executableCode.append("                string_char = None\n");
        executableCode.append("            elif not in_string:\n");
        executableCode.append("                if char == '[':\n");
        executableCode.append("                    bracket_depth += 1\n");
        executableCode.append("                elif char == ']':\n");
        executableCode.append("                    bracket_depth -= 1\n");
        executableCode.append("                elif char == '(':\n");
        executableCode.append("                    paren_depth += 1\n");
        executableCode.append("                elif char == ')':\n");
        executableCode.append("                    paren_depth -= 1\n");
        executableCode.append("                elif char == '{':\n");
        executableCode.append("                    brace_depth += 1\n");
        executableCode.append("                elif char == '}':\n");
        executableCode.append("                    brace_depth -= 1\n");
        executableCode.append("                elif char == ',' and bracket_depth == 0 and paren_depth == 0 and brace_depth == 0:\n");
        executableCode.append("                    # Esta coma separa parámetros\n");
        executableCode.append("                    params.append(current_param.strip())\n");
        executableCode.append("                    current_param = ''\n");
        executableCode.append("                    continue\n");
        executableCode.append("            \n");
        executableCode.append("            current_param += char\n");
        executableCode.append("        \n");
        executableCode.append("        # Agregar el último parámetro\n");
        executableCode.append("        if current_param.strip():\n");
        executableCode.append("            params.append(current_param.strip())\n");
        executableCode.append("        \n");
        executableCode.append("        return params\n");
        executableCode.append("    \n");
        executableCode.append("    # Dividir parámetros inteligentemente\n");
        executableCode.append("    param_strings = smart_param_split(input_str)\n");
        executableCode.append("    \n");
        executableCode.append("    if len(param_strings) == 1:\n");
        executableCode.append("        # Un solo parámetro\n");
        executableCode.append("        param = parse_input(param_strings[0])\n");
        executableCode.append("        result = ").append(functionName).append("(param)\n");
        executableCode.append("    else:\n");
        executableCode.append("        # Múltiples parámetros\n");
        executableCode.append("        params = [parse_input(p) for p in param_strings]\n");
        executableCode.append("        result = ").append(functionName).append("(*params)\n");
        executableCode.append("    \n");
        executableCode.append("    # Manejar output para compatibilidad con diferentes formatos esperados\n");
        executableCode.append("    import json\n");
        executableCode.append("    \n");
        executableCode.append("    if result is None:\n");
        executableCode.append("        print('null')\n");
        executableCode.append("    elif isinstance(result, bool):\n");
        executableCode.append("        # Mantener formato Python para booleanos: True/False\n");
        executableCode.append("        print(result)\n");
        executableCode.append("    elif isinstance(result, str):\n");
        executableCode.append("        # Para strings, imprimir sin comillas adicionales\n");
        executableCode.append("        print(result)\n");
        executableCode.append("    elif isinstance(result, dict):\n");
        executableCode.append("        # Para diccionarios, usar formato JSON con comillas dobles\n");
        executableCode.append("        print(json.dumps(result, separators=(',', ': ')))\n");
        executableCode.append("    elif isinstance(result, (list, tuple)):\n");
        executableCode.append("        # Para listas y tuplas, usar formato JSON\n");
        executableCode.append("        print(json.dumps(result, separators=(',', ': ')))\n");
        executableCode.append("    else:\n");
        executableCode.append("        print(result)\n");
        executableCode.append("except Exception as e:\n");
        executableCode.append("    print(f'Error: {e}')\n");
        
        return executableCode.toString();
    }
    
    /**
     * Extrae el nombre de la función del código del usuario
     */
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
    
    /**
     * Envuelve una cadena en comillas y escapa caracteres especiales
     */
    private String quoteString(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}