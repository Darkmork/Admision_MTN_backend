package com.desafios.robotcode.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TestCaseValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TestCase {
        public String input;
        public String expectedOutput;
        
        // Constructors, getters, setters
        public TestCase() {}
        public TestCase(String input, String expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
        
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    }

    public static class ValidationResult {
        public boolean success;
        public String message;
        public List<TestCaseResult> testResults;
        
        public ValidationResult(boolean success, String message, List<TestCaseResult> testResults) {
            this.success = success;
            this.message = message;
            this.testResults = testResults;
        }
        
        // Getters y setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<TestCaseResult> getTestResults() { return testResults; }
    }

    public static class TestCaseResult {
        public String input;
        public String expectedOutput;
        public String actualOutput;
        public boolean passed;
        public String error;
        
        public TestCaseResult(String input, String expectedOutput, String actualOutput, boolean passed, String error) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.actualOutput = actualOutput;
            this.passed = passed;
            this.error = error;
        }
        
        // Getters
        public String getInput() { return input; }
        public String getExpectedOutput() { return expectedOutput; }
        public String getActualOutput() { return actualOutput; }
        public boolean isPassed() { return passed; }
        public String getError() { return error; }
    }

    public ValidationResult validateTestCases(String functionCode, String testCasesJson, String functionName) {
        try {
            // Parse test cases JSON
            List<TestCase> testCases = objectMapper.readValue(testCasesJson, new TypeReference<List<TestCase>>() {});
            
            // Create Python script
            String pythonScript = createPythonScript(functionCode, testCases, functionName);
            
            // Execute Python script
            String output = executePythonScript(pythonScript);
            
            // Parse results
            return parseValidationResults(output, testCases);
            
        } catch (Exception e) {
            return new ValidationResult(false, "Error en validación: " + e.getMessage(), null);
        }
    }

    private String createPythonScript(String functionCode, List<TestCase> testCases, String functionName) {
        StringBuilder script = new StringBuilder();
        
        // Add the function code
        script.append(functionCode).append("\n\n");
        
        // Add realistic IDE simulation code
        script.append("import json\n");
        script.append("import sys\n");
        script.append("import traceback\n\n");
        
        script.append("results = []\n\n");
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String input = testCase.getInput();
            String expected = testCase.getExpectedOutput();
            
            script.append("try:\n");
            script.append("    input_str = ").append(quoteString(input)).append("\n");
            script.append("    \n");
            script.append("    if ',' in input_str:\n");
            script.append("        # Multiple parameters\n");
            script.append("        params_str = input_str.split(',')\n");
            script.append("        params = []\n");
            script.append("        for param in params_str:\n");
            script.append("            param = param.strip()\n");
            script.append("            try:\n");
            script.append("                if '.' in param:\n");
            script.append("                    params.append(float(param))\n");
            script.append("                else:\n");
            script.append("                    params.append(int(param))\n");
            script.append("            except ValueError:\n");
            script.append("                if param.startswith(\"'\") and param.endswith(\"'\"):\n");
            script.append("                    params.append(param[1:-1])\n");
            script.append("                elif param.startswith('\"') and param.endswith('\"'):\n");
            script.append("                    params.append(param[1:-1])\n");
            script.append("                else:\n");
            script.append("                    params.append(param)\n");
            script.append("        result = ").append(functionName).append("(*params)\n");
            script.append("    else:\n");
            script.append("        # Single parameter\n");
            script.append("        param = input_str.strip()\n");
            script.append("        try:\n");
            script.append("            if '.' in param:\n");
            script.append("                param = float(param)\n");
            script.append("            else:\n");
            script.append("                param = int(param)\n");
            script.append("        except ValueError:\n");
            script.append("            if param.startswith(\"'\") and param.endswith(\"'\"):\n");
            script.append("                param = param[1:-1]\n");
            script.append("            elif param.startswith('\"') and param.endswith('\"'):\n");
            script.append("                param = param[1:-1]\n");
            script.append("        result = ").append(functionName).append("(param)\n");
            script.append("    \n");
            script.append("    actual_output = str(result)\n");
            script.append("    expected_output = ").append(quoteString(expected)).append("\n");
            script.append("    passed = actual_output.strip() == expected_output.strip()\n");
            script.append("    \n");
            script.append("    results.append({\n");
            script.append("        'input': ").append(quoteString(input)).append(",\n");
            script.append("        'expected': expected_output,\n");
            script.append("        'actual': actual_output,\n");
            script.append("        'passed': passed,\n");
            script.append("        'error': None\n");
            script.append("    })\n");
            script.append("    \n");
            script.append("except Exception as e:\n");
            script.append("    results.append({\n");
            script.append("        'input': ").append(quoteString(input)).append(",\n");
            script.append("        'expected': ").append(quoteString(expected)).append(",\n");
            script.append("        'actual': f'ERROR: {e}',\n");
            script.append("        'passed': False,\n");
            script.append("        'error': str(e)\n");
            script.append("    })\n\n");
        }
        
        script.append("print(json.dumps(results))\n");
        
        return script.toString();
    }
    
    private String quoteString(String str) {
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private String executePythonScript(String script) throws IOException, InterruptedException {
        // Create temporary Python file
        Path tempFile = Files.createTempFile("test_validation", ".py");
        Files.write(tempFile, script.getBytes());
        
        try {
            // Execute Python script
            ProcessBuilder pb = new ProcessBuilder("python3", tempFile.toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // Wait for completion with timeout
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Script execution timeout");
            }
            
            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            return output.toString().trim();
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    private ValidationResult parseValidationResults(String output, List<TestCase> testCases) {
        try {
            List<Map<String, Object>> results = objectMapper.readValue(output, new TypeReference<List<Map<String, Object>>>() {});
            
            List<TestCaseResult> testResults = results.stream()
                .map(result -> new TestCaseResult(
                    (String) result.get("input"),
                    (String) result.get("expected"),
                    (String) result.get("actual"),
                    (Boolean) result.get("passed"),
                    (String) result.get("error")
                ))
                .toList();
            
            boolean allPassed = testResults.stream().allMatch(TestCaseResult::isPassed);
            long passedCount = testResults.stream().mapToLong(r -> r.isPassed() ? 1 : 0).sum();
            
            String message = String.format("Validación completada: %d/%d test cases pasaron", passedCount, testResults.size());
            
            return new ValidationResult(allPassed, message, testResults);
            
        } catch (JsonProcessingException e) {
            return new ValidationResult(false, "Error parsing validation results: " + output, null);
        }
    }
}