package com.desafios.robotcode.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

@Service
public class RobotomAiService {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotomAiService.class);
    
    @Value("${deepseek.api.key:}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public RobotomAiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public String askRobotom(String userMessage, String context) {
        try {
            logger.info("🤖 Robotom recibió pregunta: {}", userMessage.substring(0, Math.min(userMessage.length(), 100)));
            
            // Validar que la pregunta sea sobre programación/Python
            if (!isValidQuestion(userMessage)) {
                return "¡Hola! Soy Robotom 🤖, tu asistente de programación. Solo puedo ayudarte con preguntas sobre Python, algoritmos y problemas de programación relacionados con RobotCode Arena. ¿Tienes alguna duda sobre código o ejercicios?";
            }
            
            String systemPrompt = buildSystemPrompt();
            String fullPrompt = buildUserPrompt(userMessage, context);
            
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, fullPrompt);
            
            logger.info("📤 Enviando request a DeepSeek API...");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
                
            logger.info("📥 Respuesta recibida de DeepSeek");
            
            return parseResponse(response.getBody());
            
        } catch (Exception e) {
            logger.error("❌ Error al consultar DeepSeek API: {}", e.getMessage());
            return "Lo siento, tuve un problema técnico. Por favor intenta de nuevo en unos momentos. 🔧";
        }
    }
    
    private boolean isValidQuestion(String message) {
        String messageLower = message.toLowerCase();
        
        // Palabras clave permitidas
        String[] allowedKeywords = {
            "python", "código", "codigo", "programar", "función", "funcion", 
            "variable", "bucle", "loop", "if", "else", "for", "while",
            "lista", "diccionario", "string", "int", "float", "def",
            "return", "print", "input", "error", "bug", "algoritmo",
            "array", "método", "metodo", "clase", "class", "import",
            "sintaxis", "debug", "debuggear", "ejercicio", "problema",
            "challenge", "desafío", "desafio", "robotcode", "arena",
            "indentación", "indentacion", "lógica", "logica"
        };
        
        // Verificar si contiene al menos una palabra clave
        for (String keyword : allowedKeywords) {
            if (messageLower.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    private String buildSystemPrompt() {
        return """
            Eres Robotom 🤖, el asistente AI de RobotCode Arena, una plataforma de desafíos de programación en Python.
            
            TU PERSONALIDAD:
            - Amigable, paciente y educativo
            - Entusiasta de la programación
            - Siempre motivas a seguir aprendiendo
            - Usas emojis ocasionalmente pero sin exagerar
            
            TUS FUNCIONES:
            - Ayudar SOLO con preguntas sobre Python y programación
            - Explicar conceptos de programación de manera simple
            - Ayudar a debuggear código Python
            - Sugerir mejores prácticas de programación
            - Motivar el aprendizaje continuo
            
            REGLAS ESTRICTAS:
            - Solo respondes sobre Python, algoritmos, programación y temas técnicos relacionados
            - NO respondes sobre otros temas (historia, geografía, chismes, etc.)
            - Mantén respuestas concisas pero completas
            - Si preguntan algo no relacionado, redirige amablemente hacia programación
            - Siempre incluye ejemplos de código cuando sea relevante
            - Usa markdown para formatear código con ```python
            
            ESTILO DE RESPUESTA:
            - Explica paso a paso
            - Da ejemplos prácticos
            - Sugiere ejercicios adicionales cuando sea apropiado
            - Termina con una pregunta o invitación a seguir aprendiendo
            """;
    }
    
    private String buildUserPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();
        
        if (context != null && !context.trim().isEmpty()) {
            prompt.append("CONTEXTO DEL PROBLEMA ACTUAL:\n");
            prompt.append("```python\n").append(context).append("\n```\n\n");
        }
        
        prompt.append("PREGUNTA DEL USUARIO:\n");
        prompt.append(userMessage);
        
        return prompt.toString();
    }
    
    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 800);
        requestBody.put("temperature", 0.7);
        
        List<Map<String, String>> messages = Arrays.asList(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        );
        requestBody.put("messages", messages);
        
        return requestBody;
    }
    
    private String parseResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode choices = jsonNode.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.get("message");
                
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        String aiResponse = content.asText().trim();
                        logger.info("✅ Respuesta parseada exitosamente");
                        return aiResponse;
                    }
                }
            }
            
            logger.warn("⚠️ Formato de respuesta inesperado de DeepSeek");
            return "Recibí una respuesta en formato inesperado. Por favor intenta de nuevo.";
            
        } catch (Exception e) {
            logger.error("❌ Error al parsear respuesta: {}", e.getMessage());
            return "Tuve problemas procesando la respuesta. Por favor intenta de nuevo.";
        }
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}