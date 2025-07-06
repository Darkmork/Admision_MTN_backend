package com.desafios.robotcode.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

@Service
public class FlexibleCodeExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Constantes para límites de ejecución
    private static final int TIMEOUT_SECONDS = 10;
    private static final long MAX_OUTPUT_SIZE = 10000; // 10KB max output
    private static final double FLOAT_TOLERANCE = 1e-6; // Tolerancia para comparación de floats

    public static class ExecutionResult {
        public String stdout;
        public String stderr;
        public String returnValue;
        public int exitCode;
        public long executionTime;
        public ExecutionStatus status;
        public String errorMessage;
        
        public ExecutionResult() {}
        
        // Getters y setters
        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public String getReturnValue() { return returnValue; }
        public void setReturnValue(String returnValue) { this.returnValue = returnValue; }
        public int getExitCode() { return exitCode; }
        public void setExitCode(int exitCode) { this.exitCode = exitCode; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public ExecutionStatus getStatus() { return status; }
        public void setStatus(ExecutionStatus status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public enum ExecutionStatus {
        SUCCESS,
        COMPILATION_ERROR,
        RUNTIME_ERROR,
        TIME_LIMIT_EXCEEDED,
        MEMORY_LIMIT_EXCEEDED,
        OUTPUT_LIMIT_EXCEEDED,
        INTERNAL_ERROR
    }

    /**
     * Ejecuta código Python de manera flexible, capturando tanto stdout como el valor de retorno
     */
    public ExecutionResult executePythonFlexibly(String userCode, String input) {
        ExecutionResult result = new ExecutionResult();
        long startTime = System.currentTimeMillis();
        
        try {
            // Crear archivos temporales seguros
            Path tempDir = createSecureTempDirectory();
            Path sourceFile = createPythonTestFile(tempDir, userCode, input);
            
            try {
                // Ejecutar el código Python
                result = executePythonCode(sourceFile);
                result.executionTime = System.currentTimeMillis() - startTime;
                return result;
                
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

    /**
     * Crea un archivo Python que ejecuta el código del usuario de manera flexible
     */
    private Path createPythonTestFile(Path tempDir, String userCode, String input) throws IOException {
        Path sourceFile = tempDir.resolve("test_solution.py");
        
        StringBuilder testCode = new StringBuilder();
        testCode.append("# -*- coding: utf-8 -*-\n");
        testCode.append("import sys\n");
        testCode.append("import traceback\n");
        testCode.append("import ast\n");
        testCode.append("import json\n");
        testCode.append("from io import StringIO\n");
        testCode.append("\n");
        
        // Definir el código del usuario como variable global
        testCode.append("# Código del usuario\n");
        testCode.append("userCode = ").append(quoteString(userCode)).append("\n");
        testCode.append(userCode).append("\n\n");
        
        // Usar AST para detectar funciones globales
        String detectGlobalFunctionsScript = """
import ast
code = '''%s'''
tree = ast.parse(code)
global_funcs = [node.name for node in tree.body if isinstance(node, ast.FunctionDef)]
if global_funcs:
    print(global_funcs[0])
else:
    print('')
""".formatted(userCode);
        String functionName = executePythonAndGetOutput(detectGlobalFunctionsScript).trim();

        // Si hay función global, generar wrapper para ejecutarla; si no, solo exponer clases y ejecutar el test case imprimiendo el resultado
        if (!functionName.isEmpty()) {
            // Funciones auxiliares mejoradas
            testCode.append("def find_main_function():\n");
            testCode.append("    \"\"\"Busca la función principal en el código usando AST\"\"\"\n");
            testCode.append("    try:\n");
            testCode.append("        tree = ast.parse(userCode)\n");
            testCode.append("        function_names = []\n");
            testCode.append("        \n");
            testCode.append("        for node in ast.walk(tree):\n");
            testCode.append("            if isinstance(node, ast.FunctionDef):\n");
            testCode.append("                function_names.append(node.name)\n");
            testCode.append("        \n");
            testCode.append("        if len(function_names) == 1:\n");
            testCode.append("            return function_names[0]\n");
            testCode.append("        elif len(function_names) > 1:\n");
            testCode.append("            # Priorizar funciones con nombres específicos\n");
            testCode.append("            priority_names = ['main', 'solve', 'solution', 'calculate', 'process', 'check', 'verify']\n");
            testCode.append("            for name in priority_names:\n");
            testCode.append("                if name in function_names:\n");
            testCode.append("                    return name\n");
            testCode.append("            # Si no encuentra una función prioritaria, usar la primera\n");
            testCode.append("            return function_names[0]\n");
            testCode.append("        \n");
            testCode.append("        return None\n");
            testCode.append("    except Exception as e:\n");
            testCode.append("        print(f'Error parsing AST: {e}', file=sys.stderr)\n");
            testCode.append("        return None\n");
            testCode.append("\n");
            
            testCode.append("def find_main_class():\n");
            testCode.append("    \"\"\"Busca la clase principal en el código usando AST\"\"\"\n");
            testCode.append("    try:\n");
            testCode.append("        tree = ast.parse(userCode)\n");
            testCode.append("        class_names = []\n");
            testCode.append("        for node in ast.walk(tree):\n");
            testCode.append("            if isinstance(node, ast.ClassDef):\n");
            testCode.append("                class_names.append(node.name)\n");
            testCode.append("        if len(class_names) == 1:\n");
            testCode.append("            return class_names[0]\n");
            testCode.append("        elif len(class_names) > 1:\n");
            testCode.append("            return class_names[0]\n");
            testCode.append("        return None\n");
            testCode.append("    except Exception as e:\n");
            testCode.append("        print(f'Error parsing AST for class: {e}', file=sys.stderr)\n");
            testCode.append("        return None\n");
            testCode.append("\n");
            
            testCode.append("def parse_input_advanced(input_str):\n");
            testCode.append("    \"\"\"Parsea el input de manera avanzada usando ast.literal_eval\"\"\"\n");
            testCode.append("    try:\n");
            testCode.append("        input_str = input_str.strip()\n");
            testCode.append("        \n");
            testCode.append("        # Si está vacío, devolver None\n");
            testCode.append("        if not input_str:\n");
            testCode.append("            return None\n");
            testCode.append("        \n");
            testCode.append("        # Si ya es una lista, tupla o diccionario válido, parsearlo directamente\n");
            testCode.append("        if (input_str.startswith('[') and input_str.endswith(']')) or \\\n");
            testCode.append("           (input_str.startswith('(') and input_str.endswith(')')) or \\\n");
            testCode.append("           (input_str.startswith('{') and input_str.endswith('}')):\n");
            testCode.append("            try:\n");
            testCode.append("                parsed = ast.literal_eval(input_str)\n");
            testCode.append("                # Si es una lista, devolverla como un solo argumento\n");
            testCode.append("                if isinstance(parsed, list):\n");
            testCode.append("                    return parsed\n");
            testCode.append("                return parsed\n");
            testCode.append("            except (ValueError, SyntaxError):\n");
            testCode.append("                pass\n");
            testCode.append("        \n");
            testCode.append("        # Si contiene comas pero no está en una estructura, intentar parsear como tupla\n");
            testCode.append("        if ',' in input_str and not (input_str.startswith('[') or input_str.startswith('{')):\n");
            testCode.append("            try:\n");
            testCode.append("                # Intentar parsear como tupla\n");
            testCode.append("                tuple_str = '(' + input_str + ')'\n");
            testCode.append("                return ast.literal_eval(tuple_str)\n");
            testCode.append("            except (ValueError, SyntaxError):\n");
            testCode.append("                # Si falla, parsear manualmente\n");
            testCode.append("                return parse_comma_separated(input_str)\n");
            testCode.append("        \n");
            testCode.append("        # Intentar parsear como literal de Python\n");
            testCode.append("        try:\n");
            testCode.append("            return ast.literal_eval(input_str)\n");
            testCode.append("        except (ValueError, SyntaxError):\n");
            testCode.append("            pass\n");
            testCode.append("        \n");
            testCode.append("        # Si no es nada de lo anterior, devolver como string\n");
            testCode.append("        return input_str\n");
            testCode.append("        \n");
            testCode.append("    except Exception as e:\n");
            testCode.append("        print(f'Error parsing input: {e}', file=sys.stderr)\n");
            testCode.append("        return input_str  # Devolver como string si todo falla\n");
            testCode.append("\n");
            
            testCode.append("def parse_comma_separated(input_str):\n");
            testCode.append("    \"\"\"Parsea valores separados por comas\"\"\"\n");
            testCode.append("    params = []\n");
            testCode.append("    current = \"\"\n");
            testCode.append("    paren_count = 0\n");
            testCode.append("    \n");
            testCode.append("    for char in input_str:\n");
            testCode.append("        if char == '(' or char == '[' or char == '{':\n");
            testCode.append("            paren_count += 1\n");
            testCode.append("        elif char == ')' or char == ']' or char == '}':\n");
            testCode.append("            paren_count -= 1\n");
            testCode.append("        elif char == ',' and paren_count == 0:\n");
            testCode.append("            params.append(parse_single_value(current.strip()))\n");
            testCode.append("            current = \"\"\n");
            testCode.append("            continue\n");
            testCode.append("        current += char\n");
            testCode.append("    \n");
            testCode.append("    if current.strip():\n");
            testCode.append("        params.append(parse_single_value(current.strip()))\n");
            testCode.append("    \n");
            testCode.append("    return params\n");
            testCode.append("\n");
            
            testCode.append("def parse_single_value(param):\n");
            testCode.append("    \"\"\"Parsea un valor individual de manera robusta\"\"\"\n");
            testCode.append("    param = param.strip()\n");
            testCode.append("    \n");
            testCode.append("    # Intentar parsear como literal de Python\n");
            testCode.append("    try:\n");
            testCode.append("        return ast.literal_eval(param)\n");
            testCode.append("    except (ValueError, SyntaxError):\n");
            testCode.append("        pass\n");
            testCode.append("    \n");
            testCode.append("    # String con comillas\n");
            testCode.append("    if (param.startswith(\"'\") and param.endswith(\"'\") or \n");
            testCode.append("        param.startswith('\"') and param.endswith('\"')):\n");
            testCode.append("        return param[1:-1]\n");
            testCode.append("    \n");
            testCode.append("    # Números\n");
            testCode.append("    try:\n");
            testCode.append("        if '.' in param:\n");
            testCode.append("            return float(param)\n");
            testCode.append("        else:\n");
            testCode.append("            return int(param)\n");
            testCode.append("    except ValueError:\n");
            testCode.append("        # Si no es número, devolver como string\n");
            testCode.append("        return param\n");
            testCode.append("\n");
        } else {
            // Solo exponer clases, no buscar ni ejecutar ninguna función
            testCode.append("# Solo clases expuestas, sin función principal\n");
            // Imprimir el resultado de la expresión de input para este test case
            testCode.append("try:\n");
            testCode.append("    print(" + input + ")\n");
            testCode.append("except Exception as e:\n");
            testCode.append("    print('ERROR:', e)\n");
        }
        
        // Configurar input
        // Si el input parece una expresión (no empieza y termina con comillas), asignar sin comillas
        boolean isExpression = !(input.trim().startsWith("\"") && input.trim().endsWith("\"")) && !(input.trim().startsWith("'") && input.trim().endsWith("'"));
        if (isExpression) {
            testCode.append("input_data = " + input + "\n");
        } else {
            // Remover comillas externas
            String cleanInput = input.trim();
            if ((cleanInput.startsWith("\"") && cleanInput.endsWith("\"")) || (cleanInput.startsWith("'") && cleanInput.endsWith("'"))) {
                cleanInput = cleanInput.substring(1, cleanInput.length() - 1);
            }
            testCode.append("input_data = '" + cleanInput.replace("'", "\\'") + "'\n");
        }
        testCode.append("\n");
        
        // Código para ejecutar de manera flexible
        testCode.append("try:\n");
        testCode.append("    # Capturar stdout\n");
        testCode.append("    old_stdout = sys.stdout\n");
        testCode.append("    captured_output = StringIO()\n");
        testCode.append("    sys.stdout = captured_output\n");
        testCode.append("    result = None\n");
        testCode.append("    function_name = find_main_function()\n");
        testCode.append("    class_name = find_main_class()\n");
        testCode.append("    error = None\n");
        testCode.append("    try:\n");
        testCode.append("        if function_name:\n");
        testCode.append("            # Verificar si la función necesita argumentos\n");
        testCode.append("            import inspect\n");
        testCode.append("            func = globals()[function_name]\n");
        testCode.append("            sig = inspect.signature(func)\n");
        testCode.append("            param_count = len(sig.parameters)\n");
        testCode.append("            \n");
        testCode.append("            if param_count == 0:\n");
        testCode.append("                # Función sin argumentos\n");
        testCode.append("                result = func()\n");
        testCode.append("            else:\n");
        testCode.append("                # Función con argumentos\n");
        testCode.append("                args = parse_input_advanced(input_data)\n");
        testCode.append("                if args is not None:\n");
        testCode.append("                    if param_count == 1:\n");
        testCode.append("                        # Si la función espera un solo argumento\n");
        testCode.append("                        result = func(args)\n");
        testCode.append("                    else:\n");
        testCode.append("                        # Si espera más de un argumento\n");
        testCode.append("                        if isinstance(args, (list, tuple)):\n");
        testCode.append("                            result = func(*args)\n");
        testCode.append("                        else:\n");
        testCode.append("                            result = func(args)\n");
        testCode.append("                else:\n");
        testCode.append("                    # Si no hay input pero la función necesita argumentos, usar valores por defecto\n");
        testCode.append("                    if param_count == 1:\n");
        testCode.append("                        result = func(\"\")\n");
        testCode.append("                    else:\n");
        testCode.append("                        result = func()\n");
        testCode.append("        elif class_name:\n");
        testCode.append("            # Si hay una clase principal definida, ejecutar el input como expresión Python\n");
        testCode.append("            print('DEBUG_INPUT_DATA:', repr(input_data), file=sys.stderr)\n");
        testCode.append("            try:\n");
        testCode.append("                result = eval(input_data)\n");
        testCode.append("            except Exception as e:\n");
        testCode.append("                error = traceback.format_exc()\n");
        testCode.append("            if error:\n");
        testCode.append("                print(error)\n");
        testCode.append("            elif result is not None:\n");
        testCode.append("                print(result)\n");
        testCode.append("            else:\n");
        testCode.append("                print('')\n");
        testCode.append("        else:\n");
        testCode.append("            # Si no se encuentra función, intentar ejecutar el código directamente\n");
        testCode.append("            try:\n");
        testCode.append("                exec(userCode)\n");
        testCode.append("            except Exception as e:\n");
        testCode.append("                error = traceback.format_exc()\n");
        testCode.append("    except Exception as e:\n");
        testCode.append("        error = traceback.format_exc()\n");
        testCode.append("    sys.stdout = old_stdout\n");
        testCode.append("    output = captured_output.getvalue().strip()\n");
        testCode.append("    if error:\n");
        testCode.append("        print(error)\n");
        testCode.append("    elif result is not None:\n");
        testCode.append("        print(result)\n");
        testCode.append("    elif output:\n");
        testCode.append("        print(output)\n");
        testCode.append("    else:\n");
        testCode.append("        print('')\n");
        testCode.append("except Exception as e:\n");
        testCode.append("    import traceback\n");
        testCode.append("    print(traceback.format_exc())\n");
        
        Files.write(sourceFile, testCode.toString().getBytes());
        return sourceFile;
    }

    private ExecutionResult executePythonCode(Path sourceFile) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("python3");
        command.add(sourceFile.toString());
        
        return executeCommand(command, "", TIMEOUT_SECONDS);
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

    /**
     * Compara outputs de manera flexible y robusta
     */
    public boolean compareOutputsFlexibly(String expected, String actual) {
        if (expected == null) expected = "";
        if (actual == null) actual = "";
        
        // Normalizar strings
        String normalizedExpected = normalizeOutput(expected);
        String normalizedActual = normalizeOutput(actual);
        
        // Comparación exacta después de normalización
        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }
        
        // Comparación numérica si ambos son números
        if (isNumeric(normalizedExpected) && isNumeric(normalizedActual)) {
            try {
                double expectedNum = parseNumber(normalizedExpected);
                double actualNum = parseNumber(normalizedActual);
                return Math.abs(expectedNum - actualNum) < FLOAT_TOLERANCE;
            } catch (NumberFormatException e) {
                // Si no se pueden parsear como números, continuar con otras comparaciones
            }
        }
        
        // Comparación de booleanos
        if (isBoolean(normalizedExpected) && isBoolean(normalizedActual)) {
            return parseBoolean(normalizedExpected) == parseBoolean(normalizedActual);
        }
        
        // Comparación de diccionarios si ambos parecen ser diccionarios
        if (looksLikeDict(normalizedExpected) && looksLikeDict(normalizedActual)) {
            return compareDicts(normalizedExpected, normalizedActual);
        }
        
        // Comparación de listas si ambos parecen ser listas
        if (looksLikeList(normalizedExpected) && looksLikeList(normalizedActual)) {
            return compareLists(normalizedExpected, normalizedActual);
        }
        
        // Comparación de tuplas si ambos parecen ser tuplas
        if (looksLikeTuple(normalizedExpected) && looksLikeTuple(normalizedActual)) {
            return compareTuples(normalizedExpected, normalizedActual);
        }
        
        // Comparación con tolerancia de espacios y caracteres especiales
        if (compareWithTolerance(normalizedExpected, normalizedActual)) {
            return true;
        }
        
        return false;
    }

    public String normalizeOutput(String output) {
        if (output == null) return "";
        
        return output
            .trim()
            .replaceAll("\\s+", " ") // Reemplazar múltiples espacios con uno solo
            .replaceAll("\\n\\s*", " ") // Reemplazar saltos de línea con espacios
            .replaceAll("\\s+$", "") // Eliminar espacios al final
            .replaceAll("^\\s+", "") // Eliminar espacios al inicio
            .replaceAll("'", "\"") // Normalizar comillas
            .replaceAll("True", "true") // Normalizar booleanos
            .replaceAll("False", "false")
            .replaceAll("None", "null"); // Normalizar None
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false") ||
               str.equalsIgnoreCase("True") || str.equalsIgnoreCase("False");
    }

    private boolean parseBoolean(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("True");
    }

    private double parseNumber(String str) {
        return Double.parseDouble(str);
    }

    private boolean looksLikeList(String str) {
        return str.startsWith("[") && str.endsWith("]");
    }

    private boolean looksLikeDict(String str) {
        return str.startsWith("{") && str.endsWith("}");
    }

    private boolean looksLikeTuple(String str) {
        return str.startsWith("(") && str.endsWith(")");
    }

    private boolean compareWithTolerance(String expected, String actual) {
        // Comparación con tolerancia para diferencias menores
        if (expected.equals(actual)) return true;
        
        // Ignorar diferencias en espacios extra
        String expectedNoSpaces = expected.replaceAll("\\s", "");
        String actualNoSpaces = actual.replaceAll("\\s", "");
        if (expectedNoSpaces.equals(actualNoSpaces)) return true;
        
        // Ignorar diferencias en mayúsculas/minúsculas para strings
        if (expected.equalsIgnoreCase(actual)) return true;
        
        // Comparar sin caracteres especiales
        String expectedClean = expected.replaceAll("[^a-zA-Z0-9]", "");
        String actualClean = actual.replaceAll("[^a-zA-Z0-9]", "");
        if (expectedClean.equals(actualClean)) return true;
        
        return false;
    }

    private boolean compareLists(String expected, String actual) {
        try {
            // Intentar parsear como listas de Python
            String[] expectedItems = parseListItems(expected);
            String[] actualItems = parseListItems(actual);
            
            if (expectedItems.length != actualItems.length) {
                return false;
            }
            
            for (int i = 0; i < expectedItems.length; i++) {
                if (!compareOutputsFlexibly(expectedItems[i], actualItems[i])) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean compareTuples(String expected, String actual) {
        try {
            // Parsear tuplas de manera similar a las listas
            String expectedContent = expected.substring(1, expected.length() - 1);
            String actualContent = actual.substring(1, actual.length() - 1);
            
            String[] expectedItems = parseListItems("[" + expectedContent + "]");
            String[] actualItems = parseListItems("[" + actualContent + "]");
            
            if (expectedItems.length != actualItems.length) {
                return false;
            }
            
            for (int i = 0; i < expectedItems.length; i++) {
                if (!compareOutputsFlexibly(expectedItems[i], actualItems[i])) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean compareDicts(String expected, String actual) {
        try {
            // Normalizar diccionarios eliminando espacios extra alrededor de los dos puntos
            String normalizedExpected = expected.replaceAll(":\\s+", ":");
            String normalizedActual = actual.replaceAll(":\\s+", ":");
            
            // Si ya son iguales después de normalizar espacios, retornar true
            if (normalizedExpected.equals(normalizedActual)) {
                return true;
            }
            
            // Intentar parsear como JSON para comparación más robusta
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode expectedNode = mapper.readTree(normalizedExpected);
                JsonNode actualNode = mapper.readTree(normalizedActual);
                return expectedNode.equals(actualNode);
            } catch (Exception e) {
                // Si falla el parsing JSON, continuar con comparación de strings
            }
            
            // Comparación manual de diccionarios
            return compareDictStrings(normalizedExpected, normalizedActual);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean compareDictStrings(String expected, String actual) {
        try {
            // Remover las llaves externas
            String expectedContent = expected.substring(1, expected.length() - 1);
            String actualContent = actual.substring(1, actual.length() - 1);
            
            // Parsear pares clave-valor
            Map<String, String> expectedPairs = parseDictPairs(expectedContent);
            Map<String, String> actualPairs = parseDictPairs(actualContent);
            
            if (expectedPairs.size() != actualPairs.size()) {
                return false;
            }
            
            // Comparar cada par clave-valor
            for (Map.Entry<String, String> entry : expectedPairs.entrySet()) {
                String key = entry.getKey();
                String expectedValue = entry.getValue();
                String actualValue = actualPairs.get(key);
                
                if (actualValue == null || !compareOutputsFlexibly(expectedValue, actualValue)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private Map<String, String> parseDictPairs(String content) {
        Map<String, String> pairs = new HashMap<>();
        if (content.trim().isEmpty()) {
            return pairs;
        }
        
        List<String> pairStrings = new ArrayList<>();
        int depth = 0;
        StringBuilder currentPair = new StringBuilder();
        
        for (char c : content.toCharArray()) {
            if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
            } else if (c == ',' && depth == 0) {
                pairStrings.add(currentPair.toString().trim());
                currentPair = new StringBuilder();
                continue;
            }
            currentPair.append(c);
        }
        
        if (currentPair.length() > 0) {
            pairStrings.add(currentPair.toString().trim());
        }
        
        // Parsear cada par clave:valor
        for (String pairStr : pairStrings) {
            int colonIndex = pairStr.indexOf(':');
            if (colonIndex > 0) {
                String key = pairStr.substring(0, colonIndex).trim();
                String value = pairStr.substring(colonIndex + 1).trim();
                // Remover comillas de la clave si las tiene
                if ((key.startsWith("\"") && key.endsWith("\"")) || 
                    (key.startsWith("'") && key.endsWith("'"))) {
                    key = key.substring(1, key.length() - 1);
                }
                pairs.put(key, value);
            }
        }
        
        return pairs;
    }

    private String[] parseListItems(String listStr) {
        // Parsear elementos de una lista simple
        String content = listStr.substring(1, listStr.length() - 1); // Remover [ y ]
        if (content.trim().isEmpty()) {
            return new String[0];
        }
        
        List<String> items = new ArrayList<>();
        int depth = 0;
        StringBuilder currentItem = new StringBuilder();
        
        for (char c : content.toCharArray()) {
            if (c == '[' || c == '{') {
                depth++;
            } else if (c == ']' || c == '}') {
                depth--;
            } else if (c == ',' && depth == 0) {
                items.add(currentItem.toString().trim());
                currentItem = new StringBuilder();
                continue;
            }
            currentItem.append(c);
        }
        
        if (currentItem.length() > 0) {
            items.add(currentItem.toString().trim());
        }
        
        return items.toArray(new String[0]);
    }

    private Path createSecureTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("robotcode_flex_");
        tempDir.toFile().setReadable(true, true);
        tempDir.toFile().setWritable(true, true);
        tempDir.toFile().setExecutable(true, true);
        return tempDir;
    }

    private String quoteString(String str) {
        if (str == null) return "\"\"";
        
        return "\"" + str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t") + "\"";
    }

    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
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

    private String executePythonAndGetOutput(String script) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "-c", script);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            output.append("ERROR: ").append(e.getMessage());
        }
        return output.toString();
    }
} 