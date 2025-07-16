package com.desafios.robotcode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionEngine {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Constantes para límites de ejecución
    private static final int TIMEOUT_SECONDS = 10;
    private static final long MAX_OUTPUT_SIZE = 10000; // 10KB max output
    private static final long MAX_MEMORY_MB = 128;

    public static class ExecutionRequest {
        public String sourceCode;
        public String language; // "python3", "java", "javascript", etc.
        public String stdin;
        public int timeLimit = TIMEOUT_SECONDS;
        
        // Constructors
        public ExecutionRequest() {}
        public ExecutionRequest(String sourceCode, String language, String stdin) {
            this.sourceCode = sourceCode;
            this.language = language;
            this.stdin = stdin;
        }
        
        // Getters y setters
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public String getStdin() { return stdin; }
        public void setStdin(String stdin) { this.stdin = stdin; }
        public int getTimeLimit() { return timeLimit; }
        public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }
    }

    public static class ExecutionResult {
        public String stdout;
        public String stderr;
        public int exitCode;
        public long executionTime; // en milisegundos
        public long memoryUsed; // en bytes
        public ExecutionStatus status;
        public String errorMessage;
        
        public ExecutionResult() {}
        
        // Getters y setters
        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public int getExitCode() { return exitCode; }
        public void setExitCode(int exitCode) { this.exitCode = exitCode; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public long getMemoryUsed() { return memoryUsed; }
        public void setMemoryUsed(long memoryUsed) { this.memoryUsed = memoryUsed; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public enum ExecutionStatus {
        SUCCESS,           // Ejecutó correctamente
        COMPILATION_ERROR, // Error de compilación/sintaxis
        RUNTIME_ERROR,     // Error durante ejecución
        TIME_LIMIT_EXCEEDED, // Timeout
        MEMORY_LIMIT_EXCEEDED, // Excedió memoria
        OUTPUT_LIMIT_EXCEEDED, // Output muy grande
        INTERNAL_ERROR     // Error del sistema
    }

    public ExecutionResult executeCode(ExecutionRequest request) {
        ExecutionResult result = new ExecutionResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // Validar entrada
            if (request.sourceCode == null || request.sourceCode.trim().isEmpty()) {
                result.status = ExecutionStatus.COMPILATION_ERROR;
                result.errorMessage = "Código fuente vacío";
                return result;
            }
            
            // Crear archivos temporales seguros
            Path tempDir = createSecureTempDirectory();
            Path sourceFile = createSourceFile(tempDir, request.sourceCode, request.language);
            
            try {
                // Ejecutar según el lenguaje
                ExecutionResult execResult = executeByLanguage(sourceFile, request);
                
                // Calcular tiempo de ejecución
                execResult.executionTime = System.currentTimeMillis() - startTime;
                
                return execResult;
                
            } finally {
                // Limpiar archivos temporales
                cleanupTempDirectory(tempDir);
            }
            
        } catch (Exception e) {
            result.status = ExecutionStatus.INTERNAL_ERROR;
            result.errorMessage = "Error interno: " + e.getMessage();
            result.executionTime = System.currentTimeMillis() - startTime;
            return result;
        }
    }

    private ExecutionResult executeByLanguage(Path sourceFile, ExecutionRequest request) throws IOException, InterruptedException {
        ExecutionResult result = new ExecutionResult();
        
        switch (request.language.toLowerCase()) {
            case "python3":
            case "python":
                return executePython(sourceFile, request);
            case "java":
                return executeJava(sourceFile, request);
            case "javascript":
            case "node":
                return executeJavaScript(sourceFile, request);
            default:
                result.status = ExecutionStatus.COMPILATION_ERROR;
                result.errorMessage = "Lenguaje no soportado: " + request.language;
                return result;
        }
    }

    private ExecutionResult executePython(Path sourceFile, ExecutionRequest request) throws IOException, InterruptedException {
        // Diagnóstico: Verificar disponibilidad de Python
        System.out.println("🐍 DIAGNÓSTICO PYTHON:");
        System.out.println("PATH: " + System.getenv("PATH"));
        System.out.println("PYTHONPATH: " + System.getenv("PYTHONPATH"));
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        
        // Verificar si python3 está disponible
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "python3");
            Process process = pb.start();
            process.waitFor();
            System.out.println("which python3 exit code: " + process.exitValue());
        } catch (Exception e) {
            System.out.println("Error checking python3 availability: " + e.getMessage());
        }
        
        // Primero verificar la sintaxis compilando sin ejecutar
        List<String> compileCommand = new ArrayList<>();
        compileCommand.add("python3");
        compileCommand.add("-m");
        compileCommand.add("py_compile");
        compileCommand.add(sourceFile.toString());
        
        ExecutionResult compileResult = executeCommand(compileCommand, "", 10);
        
        if (compileResult.exitCode != 0) {
            ExecutionResult result = new ExecutionResult();
            result.status = ExecutionStatus.COMPILATION_ERROR;
            result.stderr = compileResult.stderr;
            result.errorMessage = "Error de sintaxis en Python: " + compileResult.stderr;
            return result;
        }
        
        // Si la compilación es exitosa, ejecutar el código
        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add(sourceFile.toString());
        
        // Usar un entorno más permisivo para librerías de ciencia de datos
        return executeCommandWithLibraries(command, request.stdin, request.timeLimit);
    }

    private ExecutionResult executeCommandWithLibraries(List<String> command, String stdin, int timeLimit) throws IOException, InterruptedException {
        ExecutionResult result = new ExecutionResult();
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        
        // Configurar entorno para librerías de ciencia de datos
        // Permitir acceso a librerías comunes de Python
        pb.environment().put("PYTHONPATH", "/usr/local/lib/python3.*/site-packages:/usr/lib/python3.*/site-packages");
        pb.environment().put("PATH", "/usr/bin:/bin:/usr/local/bin");
        
        // Configuraciones específicas para librerías de ciencia de datos
        pb.environment().put("PYTHONUNBUFFERED", "1");
        pb.environment().put("MPLBACKEND", "Agg"); // Para matplotlib sin GUI
        
        Process process = pb.start();
        
        // Escribir stdin si existe
        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(stdin);
                writer.flush();
            }
        }
        
        // Leer stdout y stderr de forma no bloqueante
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        
        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && stdout.length() < MAX_OUTPUT_SIZE) {
                    stdout.append(line).append("\n");
                }
            } catch (IOException e) {
                // Ignorar errores de lectura
            }
        });
        
        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null && stderr.length() < MAX_OUTPUT_SIZE) {
                    stderr.append(line).append("\n");
                }
            } catch (IOException e) {
                // Ignorar errores de lectura
            }
        });
        
        stdoutReader.start();
        stderrReader.start();
        
        // Esperar con timeout
        boolean finished = process.waitFor(timeLimit, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            result.status = ExecutionStatus.TIME_LIMIT_EXCEEDED;
            result.errorMessage = "Tiempo límite excedido (" + timeLimit + "s)";
            return result;
        }
        
        // Esperar a que los lectores terminen
        stdoutReader.join(1000);
        stderrReader.join(1000);
        
        result.exitCode = process.exitValue();
        result.stdout = stdout.toString().trim();
        result.stderr = stderr.toString().trim();
        
        // Verificar límites
        if (result.stdout.length() >= MAX_OUTPUT_SIZE) {
            result.status = ExecutionStatus.OUTPUT_LIMIT_EXCEEDED;
            result.errorMessage = "Output excede el límite permitido";
            return result;
        }
        
        // Determinar status
        if (result.exitCode == 0) {
            result.status = ExecutionStatus.SUCCESS;
        } else {
            result.status = ExecutionStatus.RUNTIME_ERROR;
            result.errorMessage = "Código terminó con error (exit code: " + result.exitCode + ")";
        }
        
        return result;
    }

    private ExecutionResult executeJava(Path sourceFile, ExecutionRequest request) throws IOException, InterruptedException {
        ExecutionResult result = new ExecutionResult();
        
        // Compilar primero
        List<String> compileCommand = new ArrayList<>();
        compileCommand.add("javac");
        compileCommand.add(sourceFile.toString());
        
        ExecutionResult compileResult = executeCommand(compileCommand, "", 30);
        
        if (compileResult.exitCode != 0) {
            result.status = ExecutionStatus.COMPILATION_ERROR;
            result.stderr = compileResult.stderr;
            result.errorMessage = "Error de compilación";
            return result;
        }
        
        // Ejecutar el archivo compilado
        String className = sourceFile.getFileName().toString().replace(".java", "");
        List<String> runCommand = new ArrayList<>();
        runCommand.add("java");
        runCommand.add("-cp");
        runCommand.add(sourceFile.getParent().toString());
        runCommand.add(className);
        
        return executeCommand(runCommand, request.stdin, request.timeLimit);
    }

    private ExecutionResult executeJavaScript(Path sourceFile, ExecutionRequest request) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("node");
        command.add(sourceFile.toString());
        
        return executeCommand(command, request.stdin, request.timeLimit);
    }

    private ExecutionResult executeCommand(List<String> command, String stdin, int timeLimit) throws IOException, InterruptedException {
        ExecutionResult result = new ExecutionResult();
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        
        // Configurar límites de seguridad
        pb.environment().put("PYTHONPATH", "");
        pb.environment().put("PATH", "/usr/bin:/bin");
        
        Process process = pb.start();
        
        // Escribir stdin si existe
        if (stdin != null && !stdin.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(stdin);
                writer.flush();
            }
        }
        
        // Leer stdout y stderr de forma no bloqueante
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        
        Thread stdoutReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null && stdout.length() < MAX_OUTPUT_SIZE) {
                    stdout.append(line).append("\n");
                }
            } catch (IOException e) {
                // Ignorar errores de lectura
            }
        });
        
        Thread stderrReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null && stderr.length() < MAX_OUTPUT_SIZE) {
                    stderr.append(line).append("\n");
                }
            } catch (IOException e) {
                // Ignorar errores de lectura
            }
        });
        
        stdoutReader.start();
        stderrReader.start();
        
        // Esperar con timeout
        boolean finished = process.waitFor(timeLimit, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            result.status = ExecutionStatus.TIME_LIMIT_EXCEEDED;
            result.errorMessage = "Tiempo límite excedido (" + timeLimit + "s)";
            return result;
        }
        
        // Esperar a que los lectores terminen
        stdoutReader.join(1000);
        stderrReader.join(1000);
        
        result.exitCode = process.exitValue();
        result.stdout = stdout.toString().trim();
        result.stderr = stderr.toString().trim();
        
        // Verificar límites
        if (result.stdout.length() >= MAX_OUTPUT_SIZE) {
            result.status = ExecutionStatus.OUTPUT_LIMIT_EXCEEDED;
            result.errorMessage = "Output excede el límite permitido";
            return result;
        }
        
        // Determinar status
        if (result.exitCode == 0) {
            result.status = ExecutionStatus.SUCCESS;
        } else {
            result.status = ExecutionStatus.RUNTIME_ERROR;
            result.errorMessage = "Código terminó con error (exit code: " + result.exitCode + ")";
        }
        
        return result;
    }

    private Path createSecureTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("robotcode_exec_");
        // Configurar permisos restrictivos
        tempDir.toFile().setReadable(true, true);
        tempDir.toFile().setWritable(true, true);
        tempDir.toFile().setExecutable(true, true);
        return tempDir;
    }

    private Path createSourceFile(Path tempDir, String sourceCode, String language) throws IOException {
        String extension = getFileExtension(language);
        String fileName = "Solution" + extension;
        Path sourceFile = tempDir.resolve(fileName);
        
        // Procesar secuencias de escape como \n
        String processedCode = sourceCode
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\r", "\r");
        
        Files.write(sourceFile, processedCode.getBytes());
        return sourceFile;
    }

    private String getFileExtension(String language) {
        switch (language.toLowerCase()) {
            case "python3":
            case "python":
                return ".py";
            case "java":
                return ".java";
            case "javascript":
            case "node":
                return ".js";
            default:
                return ".txt";
        }
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // Eliminar archivos antes que directorios
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignorar errores de limpieza
                    }
                });
        } catch (IOException e) {
            // Ignorar errores de limpieza
        }
    }
}