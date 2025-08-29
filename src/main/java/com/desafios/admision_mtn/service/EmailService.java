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
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${app.email.mock-mode:true}")
    private boolean mockMode;
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:admisiones@mtn.cl}")
    private String fromEmail;
    
    public void sendVerificationCode(String to, String code) {
        if (mockMode || "dev".equals(activeProfile)) {
            log.info("📧 [MODO DESARROLLO] Email de verificación para {}", to);
            log.info("🔐 [CÓDIGO DE VERIFICACIÓN]: {}", code);
            log.info("📄 [CONTENIDO]: {}", buildVerificationEmailBody(code));
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Código de Verificación - Monte Tabor & Nazaret");
            message.setText(buildVerificationEmailBody(code));
            
            mailSender.send(message);
            log.info("✅ Email de verificación enviado exitosamente a: {}", to);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de verificación a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar el correo de verificación: " + e.getMessage(), e);
        }
    }
    
    public void sendSimpleMessage(String to, String subject, String body) {
        if (mockMode || "dev".equals(activeProfile)) {
            log.info("📧 [MODO DESARROLLO] Email simple para {}", to);
            log.info("📝 [ASUNTO]: {}", subject);
            log.info("📄 [CONTENIDO]: {}", body);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("✅ Email simple enviado exitosamente a {} con asunto: {}", to, subject);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email simple a {} con asunto '{}': {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }
    
    public void sendWelcomeEmailWithCredentials(String to, String firstName, String lastName, 
                                              String email, String temporaryPassword, String role) {
        String emailContent = buildWelcomeEmailBody(firstName, lastName, email, temporaryPassword, role);
        
        if (mockMode || "dev".equals(activeProfile)) {
            log.info("📧 [MODO DESARROLLO] Email de bienvenida para {}", to);
            log.info("🔐 [CREDENCIALES] Email: {} | Password: {}", email, temporaryPassword);
            log.info("👤 [USUARIO] {} {} - Rol: {}", firstName, lastName, role);
            log.info("📄 [CONTENIDO]: {}", emailContent);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Bienvenido/a al Sistema de Admisiones - Monte Tabor & Nazaret");
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("✅ Email de bienvenida enviado exitosamente a: {}", to);
            log.info("🔐 [CREDENCIALES ENVIADAS] Email: {} | Usuario: {} {}", email, firstName, lastName);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de bienvenida a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email de bienvenida: " + e.getMessage(), e);
        }
    }
    
    public void sendPasswordResetEmail(String to, String firstName, String lastName, String newPassword) {
        String emailContent = buildPasswordResetEmailBody(firstName, lastName, newPassword);
        
        if (mockMode || "dev".equals(activeProfile)) {
            log.info("📧 [MODO DESARROLLO] Email de reset de contraseña para {}", to);
            log.info("🔐 [NUEVA CONTRASEÑA]: {}", newPassword);
            log.info("👤 [USUARIO]: {} {}", firstName, lastName);
            log.info("📄 [CONTENIDO]: {}", emailContent);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Contraseña Restablecida - Monte Tabor & Nazaret");
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("✅ Email de reset de contraseña enviado exitosamente a: {}", to);
            
        } catch (Exception e) {
            log.error("❌ Error enviando email de reset de contraseña a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error al enviar email de reset de contraseña: " + e.getMessage(), e);
        }
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