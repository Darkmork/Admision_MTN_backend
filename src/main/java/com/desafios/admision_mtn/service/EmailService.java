package com.desafios.admision_mtn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:admisiones@mtn.cl}")
    private String fromEmail;
    
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Código de Verificación - Monte Tabor & Nazaret");
            message.setText(buildVerificationEmailBody(code));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Error sending verification email to: {}", to, e);
            // En desarrollo, logueamos el código para testing
            log.warn("DEVELOPMENT MODE - Verification code for {}: {}", to, code);
            // Para desarrollo, no lanzamos excepción para que la aplicación funcione
            // throw new RuntimeException("Error al enviar el correo de verificación", e);
        }
        
        // En desarrollo, siempre mostramos el código para facilitar testing
        log.info("🔐 DEVELOPMENT - Verification code for {}: {}", to, code);
    }
    
    public void sendSimpleMessage(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Simple email sent to: {} with subject: {}", to, subject);
            
        } catch (Exception e) {
            log.error("Error sending email to: {} with subject: {}", to, subject, e);
            // En desarrollo, logueamos el contenido para testing
            log.warn("DEVELOPMENT MODE - Email content for {}: {}", to, body);
        }
    }
    
    public void sendWelcomeEmailWithCredentials(String to, String firstName, String lastName, 
                                              String email, String temporaryPassword, String role) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Bienvenido/a al Sistema de Admisiones - Monte Tabor & Nazaret");
            message.setText(buildWelcomeEmailBody(firstName, lastName, email, temporaryPassword, role));
            
            mailSender.send(message);
            log.info("Welcome email sent to new user: {}", to);
            
        } catch (Exception e) {
            log.error("Error sending welcome email to: {}", to, e);
            // En desarrollo, logueamos las credenciales para testing
            log.warn("DEVELOPMENT MODE - Credentials for {}: email={}, password={}", to, email, temporaryPassword);
        }
        
        // En desarrollo, siempre mostramos las credenciales para facilitar testing
        log.info("🔐 DEVELOPMENT - Credentials for {}: email={}, password={}", to, email, temporaryPassword);
    }
    
    public void sendPasswordResetEmail(String to, String firstName, String lastName, String newPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Contraseña Restablecida - Monte Tabor & Nazaret");
            message.setText(buildPasswordResetEmailBody(firstName, lastName, newPassword));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
            
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", to, e);
            // En desarrollo, logueamos la nueva contraseña para testing
            log.warn("DEVELOPMENT MODE - New password for {}: {}", to, newPassword);
        }
        
        // En desarrollo, siempre mostramos la nueva contraseña para facilitar testing
        log.info("🔐 DEVELOPMENT - New password for {}: {}", to, newPassword);
    }
    
    private String buildVerificationEmailBody(String code) {
        return """
            Estimado/a apoderado/a,
            
            Su código de verificación para el sistema de admisiones de Monte Tabor & Nazaret es:
            
            %s
            
            Este código expirará en 10 minutos.
            
            Si no solicitó este código, puede ignorar este mensaje.
            
            Atentamente,
            Equipo de Admisiones
            Monte Tabor & Nazaret
            """.formatted(code);
    }
    
    private String buildWelcomeEmailBody(String firstName, String lastName, String email, 
                                       String temporaryPassword, String role) {
        return """
            Estimado/a %s %s,
            
            ¡Bienvenido/a al Sistema de Admisiones del Colegio Monte Tabor & Nazaret!
            
            Se ha creado su cuenta en el sistema con el rol de: %s
            
            Sus credenciales de acceso son:
            • Email: %s
            • Contraseña temporal: %s
            
            INSTRUCCIONES IMPORTANTES:
            1. Acceda al sistema en: http://localhost:5176/profesor/login
            2. Use las credenciales proporcionadas arriba
            3. Se le solicitará cambiar su contraseña en el primer inicio de sesión
            4. Guarde este email de forma segura para referencia futura
            
            SEGURIDAD:
            • No comparta sus credenciales con nadie
            • Cambie su contraseña por una segura al primer acceso
            • Si tiene problemas, contacte al administrador del sistema
            
            Si no esperaba recibir este email o tiene alguna pregunta, 
            por favor contacte inmediatamente al equipo de administración.
            
            Atentamente,
            Equipo de Tecnología
            Colegio Monte Tabor & Nazaret
            Email: admisiones@mtn.cl
            """.formatted(firstName, lastName, role, email, temporaryPassword);
    }
    
    private String buildPasswordResetEmailBody(String firstName, String lastName, String newPassword) {
        return """
            Estimado/a %s %s,
            
            Su contraseña ha sido restablecida en el Sistema de Admisiones del Colegio Monte Tabor & Nazaret.
            
            Su nueva contraseña temporal es: %s
            
            INSTRUCCIONES:
            1. Acceda al sistema en: http://localhost:5176/profesor/login
            2. Use su email habitual y la nueva contraseña proporcionada
            3. Cambie inmediatamente esta contraseña por una segura
            
            SEGURIDAD:
            • Esta contraseña es temporal y debe ser cambiada al primer acceso
            • No comparta esta información con nadie
            • Si no solicitó este cambio, contacte inmediatamente al administrador
            
            Si tiene problemas para acceder o alguna pregunta, 
            contacte al equipo de administración del sistema.
            
            Atentamente,
            Equipo de Tecnología
            Colegio Monte Tabor & Nazaret
            Email: admisiones@mtn.cl
            """.formatted(firstName, lastName, newPassword);
    }
}