package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.dto.EmailVerificationRequest;
import com.desafios.admision_mtn.dto.EmailVerificationResponse;
import com.desafios.admision_mtn.dto.VerifyCodeRequest;
import com.desafios.admision_mtn.dto.VerifyCodeResponse;
import com.desafios.admision_mtn.entity.EmailVerification;
import com.desafios.admision_mtn.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {
    
    private final EmailVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public EmailVerificationResponse sendVerificationCode(EmailVerificationRequest request) {
        try {
            // Eliminar códigos anteriores para este email
            verificationRepository.deleteByEmailAndType(request.getEmail(), request.getType());
            
            // Generar nuevo código
            String code = generateVerificationCode();
            
            // Crear nueva verificación
            EmailVerification verification = new EmailVerification();
            verification.setEmail(request.getEmail());
            verification.setCode(code);
            verification.setType(request.getType());
            verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
            verification.setCreatedAt(LocalDateTime.now());
            
            verificationRepository.save(verification);
            
            // Enviar email
            emailService.sendVerificationCode(request.getEmail(), code);
            
            return EmailVerificationResponse.success(
                "Código de verificación enviado exitosamente", 10L);
                
        } catch (Exception e) {
            log.error("Error sending verification code to: {}", request.getEmail(), e);
            return EmailVerificationResponse.error(
                "Error al enviar el código de verificación. Intente nuevamente.");
        }
    }
    
    @Transactional
    public VerifyCodeResponse verifyCode(VerifyCodeRequest request) {
        Optional<EmailVerification> verificationOpt = verificationRepository
            .findByEmailAndCodeAndUsedFalse(request.getEmail(), request.getCode());
        
        if (verificationOpt.isEmpty()) {
            return VerifyCodeResponse.error("Código de verificación inválido");
        }
        
        EmailVerification verification = verificationOpt.get();
        
        if (!verification.isValid()) {
            if (verification.isExpired()) {
                return VerifyCodeResponse.error("El código de verificación ha expirado");
            } else {
                return VerifyCodeResponse.error("El código de verificación ya fue utilizado");
            }
        }
        
        // Marcar como usado
        verification.setUsed(true);
        verification.setUsedAt(LocalDateTime.now());
        verificationRepository.save(verification);
        
        return VerifyCodeResponse.success("Email verificado exitosamente");
    }
    
    public boolean isEmailVerified(String email) {
        // Verificar si existe al menos una verificación exitosa para este email
        return verificationRepository.findByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            email, EmailVerification.VerificationType.REGISTRATION).isEmpty();
    }
    
    private String generateVerificationCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
    
    @Transactional
    public void cleanupExpiredVerifications() {
        verificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
    
    // Método solo para desarrollo - obtener último código
    public String getLastVerificationCodeForDevelopment(String email) {
        Optional<EmailVerification> verification = verificationRepository
            .findTopByEmailOrderByCreatedAtDesc(email);
        
        if (verification.isPresent()) {
            return verification.get().getCode();
        } else {
            throw new RuntimeException("No se encontró código para: " + email);
        }
    }
}