package com.desafios.robotcode;

import com.desafios.robotcode.service.DockerExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DockerExecutorServiceTest {

    @Autowired
    private DockerExecutorService dockerExecutorService;

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testDockerAvailability() {
        boolean isAvailable = dockerExecutorService.isDockerAvailable();
        assertTrue(isAvailable, "Docker should be available for testing");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testSimpleCodeExecution() {
        String code = "print('Hello from Docker test')";
        String input = "";
        
        DockerExecutorService.ExecutionResult result = dockerExecutorService.executeCode(code, input);
        
        assertNotNull(result);
        assertEquals(0, result.exitCode);
        assertFalse(result.timedOut);
        assertTrue(result.output.contains("Hello from Docker test"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testFunctionExecution() {
        String code = """
            def verificar_mayor_edad(edad):
                if edad >= 18:
                    return 'Es mayor de edad'
                else:
                    return 'Es menor de edad'
            
            edad = int(input())
            print(verificar_mayor_edad(edad))
            """;
        
        // Test with age 20
        DockerExecutorService.ExecutionResult result = dockerExecutorService.executeCode(code, "20");
        
        assertNotNull(result);
        assertEquals(0, result.exitCode);
        assertFalse(result.timedOut);
        assertTrue(result.output.contains("Es mayor de edad"));
        
        // Test with age 15
        result = dockerExecutorService.executeCode(code, "15");
        
        assertNotNull(result);
        assertEquals(0, result.exitCode);
        assertFalse(result.timedOut);
        assertTrue(result.output.contains("Es menor de edad"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testCodeValidation() {
        String code = """
            def verificar_mayor_edad(edad):
                if edad >= 18:
                    return 'Es mayor de edad'
                else:
                    return 'Es menor de edad'
            
            edad = int(input())
            print(verificar_mayor_edad(edad))
            """;
        
        String testCasesJson = """
            [
                {"input": "20", "expectedOutput": "Es mayor de edad"},
                {"input": "15", "expectedOutput": "Es menor de edad"},
                {"input": "18", "expectedOutput": "Es mayor de edad"}
            ]
            """;
        
        DockerExecutorService.ValidationResult result = dockerExecutorService.validateCode(code, testCasesJson);
        
        assertNotNull(result);
        assertTrue(result.passed);
        assertEquals(3, result.correctTests);
        assertEquals(3, result.totalTests);
        assertEquals(1.0, result.successRate);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testErrorHandling() {
        String code = "print(undefined_variable)";
        String input = "";
        
        DockerExecutorService.ExecutionResult result = dockerExecutorService.executeCode(code, input);
        
        assertNotNull(result);
        assertNotEquals(0, result.exitCode);
        assertFalse(result.timedOut);
        assertTrue(result.error.contains("NameError"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
    public void testTimeoutHandling() {
        String code = """
            import time
            time.sleep(15)  # Should timeout after 10 seconds
            print('This should not print')
            """;
        String input = "";
        
        DockerExecutorService.ExecutionResult result = dockerExecutorService.executeCode(code, input);
        
        assertNotNull(result);
        assertTrue(result.timedOut);
        assertEquals(-1, result.exitCode);
        assertTrue(result.error.contains("Timeout"));
    }
}