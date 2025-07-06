package com.desafios.robotcode.controller;

import com.desafios.robotcode.dto.AiChatRequestDto;
import com.desafios.robotcode.dto.AiChatResponseDto;
import com.desafios.robotcode.service.RobotomAiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/robotom")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:3000"})
public class RobotomController {
    
    private static final Logger logger = LoggerFactory.getLogger(RobotomController.class);
    
    @Autowired
    private RobotomAiService robotomService;
    
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(@Valid @RequestBody AiChatRequestDto request, BindingResult bindingResult) {
        
        // Verificar errores de validación
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
            
            logger.warn("❌ Errores de validación en chat con Robotom: {}", errors);
            return ResponseEntity.badRequest()
                .body(AiChatResponseDto.error("Errores de validación: " + errors));
        }
        
        // Verificar si está configurado
        if (!robotomService.isConfigured()) {
            logger.warn("⚠️ Robotom no está configurado (falta API key)");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(AiChatResponseDto.error("Robotom está temporalmente fuera de servicio. ¡Vuelve pronto! 🔧"));
        }
        
        try {
            logger.info("🤖 Nueva consulta a Robotom de {} caracteres", request.getMessage().length());
            
            String response = robotomService.askRobotom(request.getMessage(), request.getContext());
            
            logger.info("✅ Robotom respondió exitosamente");
            
            return ResponseEntity.ok(AiChatResponseDto.success(response, 0));
            
        } catch (Exception e) {
            logger.error("❌ Error inesperado en chat con Robotom: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AiChatResponseDto.error("Robotom tuvo un problema técnico. Por favor intenta de nuevo. 🔧"));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Object> getStatus() {
        boolean isConfigured = robotomService.isConfigured();
        
        return ResponseEntity.ok(java.util.Map.of(
            "robotom_available", isConfigured,
            "message", isConfigured ? 
                "¡Robotom está listo para ayudarte! 🤖" : 
                "Robotom está configurándose... ⚙️",
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
    
    @GetMapping("/help")
    public ResponseEntity<Object> getHelp() {
        return ResponseEntity.ok(java.util.Map.of(
            "name", "Robotom 🤖",
            "description", "Asistente AI especializado en Python y programación",
            "capabilities", java.util.Arrays.asList(
                "Explicar conceptos de Python",
                "Ayudar con debugging de código", 
                "Sugerir mejoras en algoritmos",
                "Resolver dudas sobre ejercicios",
                "Enseñar buenas prácticas de programación"
            ),
            "limitations", java.util.Arrays.asList(
                "Solo responde preguntas sobre programación",
                "Especializado en Python y algoritmos",
                "No puede ejecutar código directamente"
            ),
            "usage_tips", java.util.Arrays.asList(
                "Sé específico en tus preguntas",
                "Incluye código cuando sea relevante",
                "Pregunta sobre conceptos que no entiendas",
                "Pide ejemplos prácticos"
            )
        ));
    }
}