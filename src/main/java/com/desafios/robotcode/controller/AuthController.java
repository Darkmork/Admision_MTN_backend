package com.desafios.robotcode.controller;

import com.desafios.robotcode.dto.RegisterRequestDto;
import com.desafios.robotcode.dto.VerifyEmailRequestDto;
import com.desafios.robotcode.dto.ResendVerificationRequestDto;
import com.desafios.robotcode.dto.UsuarioDto;
import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true")
@Validated
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final EmailVerificationService emailVerificationService;
    
    @Autowired
    public AuthController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }
    
    /**
     * Inicia el proceso de registro con verificación por email
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDto request) {
        try {
            emailVerificationService.initiateRegistration(
                request.getUsername(), 
                request.getEmail(), 
                request.getPassword()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Se ha enviado un código de verificación a tu correo electrónico");
            response.put("email", request.getEmail());
            
            logger.info("Registration initiated for user: {} with email: {}", 
                       request.getUsername(), request.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            logger.warn("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error interno del servidor. Por favor, inténtalo más tarde.");
            
            logger.error("Unexpected error during registration for email {}: ", request.getEmail(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Verifica el código de email y completa el registro
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto request) {
        try {
            Usuario usuario = emailVerificationService.verifyEmailAndCompleteRegistration(
                request.getEmail(), 
                request.getVerificationCode()
            );
            
            // Convertir a DTO para respuesta
            UsuarioDto usuarioDto = new UsuarioDto();
            usuarioDto.setId(usuario.getId());
            usuarioDto.setUsername(usuario.getUsername());
            usuarioDto.setEmail(usuario.getEmail());
            usuarioDto.setRol(usuario.getRol());
            usuarioDto.setPuntaje(usuario.getPuntaje());
            usuarioDto.setFechaRegistro(usuario.getFechaRegistro());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email verificado exitosamente. Tu cuenta ha sido creada.");
            response.put("user", usuarioDto);
            
            logger.info("Email verification successful for user: {} with email: {}", 
                       usuario.getUsername(), request.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            logger.warn("Email verification failed for email {}: {}", request.getEmail(), e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error interno del servidor. Por favor, inténtalo más tarde.");
            
            logger.error("Unexpected error during email verification for email {}: ", request.getEmail(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Reenvía el código de verificación
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@Valid @RequestBody ResendVerificationRequestDto request) {
        try {
            emailVerificationService.resendVerificationCode(request.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Se ha reenviado el código de verificación a tu correo electrónico");
            response.put("email", request.getEmail());
            
            logger.info("Verification code resent for email: {}", request.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            logger.warn("Resend verification failed for email {}: {}", request.getEmail(), e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error interno del servidor. Por favor, inténtalo más tarde.");
            
            logger.error("Unexpected error during resend verification for email {}: ", request.getEmail(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint para validar si un email es de dominio MTN
     */
    @PostMapping("/validate-mtn-email")
    public ResponseEntity<Map<String, Object>> validateMTNEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        Map<String, Object> response = new HashMap<>();
        boolean isValid = emailVerificationService.isValidMTNEmail(email);
        
        response.put("isValid", isValid);
        if (!isValid) {
            response.put("message", "Solo se permiten correos con dominios @mtn.cl o @alumnos.mtn.cl");
        }
        
        return ResponseEntity.ok(response);
    }
}