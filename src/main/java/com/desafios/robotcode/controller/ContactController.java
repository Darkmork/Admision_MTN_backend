package com.desafios.robotcode.controller;

import com.desafios.robotcode.dto.ContactRequestDto;
import com.desafios.robotcode.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendContactMessage(@Valid @RequestBody ContactRequestDto contactRequest, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        
        // Verificar errores de validación
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
            
            logger.warn("Errores de validación en formulario de contacto: {}", errors);
            response.put("success", false);
            response.put("message", "Errores de validación: " + errors);
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            logger.info("Recibida solicitud de contacto válida de: {} <{}>", 
                       contactRequest.getName(), contactRequest.getEmail());
            
            // Enviar el email de contacto
            emailService.sendContactEmail(
                contactRequest.getName(),
                contactRequest.getEmail(),
                contactRequest.getSubject(),
                contactRequest.getMessage()
            );
            
            response.put("success", true);
            response.put("message", "¡Mensaje enviado exitosamente! Recibirás una respuesta pronto.");
            
            logger.info("Mensaje de contacto enviado exitosamente desde: {} <{}>", 
                       contactRequest.getName(), contactRequest.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error procesando mensaje de contacto de: {} <{}>", 
                        contactRequest.getName(), contactRequest.getEmail(), e);
            
            response.put("success", false);
            response.put("message", "Error al enviar el mensaje: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Endpoint de contacto funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }
}