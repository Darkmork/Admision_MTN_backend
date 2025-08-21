package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.EmailVerificationRequest;
import com.desafios.admision_mtn.dto.EmailVerificationResponse;
import com.desafios.admision_mtn.dto.VerifyCodeRequest;
import com.desafios.admision_mtn.dto.VerifyCodeResponse;
import com.desafios.admision_mtn.service.EmailVerificationService;
import com.desafios.admision_mtn.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class EmailController {
    
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    
    @PostMapping("/send-verification")
    public ResponseEntity<EmailVerificationResponse> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request) {
        
        log.info("Sending verification code to: {}", request.getEmail());
        EmailVerificationResponse response = emailVerificationService.sendVerificationCode(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/verify-code")
    public ResponseEntity<VerifyCodeResponse> verifyCode(
            @Valid @RequestBody VerifyCodeRequest request) {
        
        log.info("Verifying code for email: {}", request.getEmail());
        VerifyCodeResponse response = emailVerificationService.verifyCode(request);
        
        if (response.isSuccess()) {
            // Marcar el email como verificado en el usuario si existe
            userService.markEmailAsVerified(request.getEmail());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/check-exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        log.info("Email {} exists: {}", email, exists);
        return ResponseEntity.ok(exists);
    }
    
    // Endpoint solo para desarrollo - obtener último código
    @GetMapping("/get-last-code")
    public ResponseEntity<String> getLastCodeForDevelopment(@RequestParam String email) {
        // Solo para desarrollo
        try {
            String code = emailVerificationService.getLastVerificationCodeForDevelopment(email);
            return ResponseEntity.ok("Último código para " + email + ": " + code);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("No se encontró código para: " + email);
        }
    }
}