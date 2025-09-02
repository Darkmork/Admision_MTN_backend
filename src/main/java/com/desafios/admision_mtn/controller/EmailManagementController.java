package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.EmailService;
import com.desafios.admision_mtn.service.InstitutionalEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/email-management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class EmailManagementController {
    
    private final EmailService emailService;
    private final InstitutionalEmailService institutionalEmailService;
    private final JavaMailSender mailSender;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${app.email.mock-mode:true}")
    private boolean mockMode;
    
    @Value("${spring.mail.host:smtp.gmail.com}")
    private String smtpHost;
    
    @Value("${spring.mail.port:587}")
    private int smtpPort;
    
    @Value("${spring.mail.username:}")
    private String smtpUsername;
    
    /**
     * Obtener estado actual del sistema de emails
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getEmailSystemStatus() {
        log.info("📊 Admin solicitando estado del sistema de emails");
        
        Map<String, Object> status = new HashMap<>();
        
        // Información general
        status.put("activeProfile", activeProfile);
        status.put("mockMode", mockMode);
        status.put("systemStatus", mockMode ? "DESARROLLO - Solo Logs" : "PRODUCCIÓN - Envío Real");
        
        // Configuración SMTP
        Map<String, Object> smtpConfig = new HashMap<>();
        smtpConfig.put("host", smtpHost);
        smtpConfig.put("port", smtpPort);
        smtpConfig.put("username", smtpUsername.isEmpty() ? "NO CONFIGURADO" : smtpUsername);
        smtpConfig.put("configured", !smtpUsername.isEmpty());
        
        status.put("smtpConfiguration", smtpConfig);
        
        // Estado de servicios
        Map<String, Object> services = new HashMap<>();
        services.put("EmailService", "Activo");
        services.put("InstitutionalEmailService", "Activo");
        services.put("JavaMailSender", mailSender != null ? "Configurado" : "No Configurado");
        
        status.put("services", services);
        
        // Recomendaciones
        if (mockMode) {
            status.put("recommendation", "Sistema en modo desarrollo. Los emails se muestran en logs pero no se envían realmente.");
            status.put("action", "Para envío real, configure SMTP y establezca EMAIL_MOCK_MODE=false");
        } else {
            if (smtpUsername.isEmpty()) {
                status.put("recommendation", "SMTP no configurado completamente. Verifique las credenciales.");
                status.put("action", "Configure SMTP_USERNAME, SMTP_PASSWORD y otras variables de entorno");
            } else {
                status.put("recommendation", "Sistema configurado para envío real de emails.");
                status.put("action", "Monitorear logs para confirmar entregas exitosas");
            }
        }
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Probar envío de email de prueba
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@RequestParam String email) {
        log.info("🧪 Admin solicitando prueba de email a: {}", email);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testMessage = """
                Este es un email de prueba del Sistema de Admisiones.
                
                Información del sistema:
                - Perfil activo: %s
                - Modo mock: %s
                - Fecha de prueba: %s
                
                Si recibe este mensaje, el sistema de emails está funcionando correctamente.
                
                Atentamente,
                Sistema de Admisiones - Monte Tabor & Nazaret
                """.formatted(activeProfile, mockMode ? "Activado" : "Desactivado", java.time.LocalDateTime.now());
            
            emailService.sendSimpleMessage(email, "🧪 Email de Prueba - Sistema de Admisiones", testMessage);
            
            result.put("success", true);
            result.put("message", "Email de prueba procesado correctamente");
            result.put("mockMode", mockMode);
            result.put("recipient", email);
            
            if (mockMode) {
                result.put("note", "El email fue procesado en modo desarrollo. Revise los logs del servidor.");
            } else {
                result.put("note", "El email fue enviado al destinatario. Verifique la bandeja de entrada.");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error en prueba de email: {}", e.getMessage(), e);
            
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("mockMode", mockMode);
            result.put("recommendation", "Verifique la configuración SMTP y los logs del servidor");
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Obtener estadísticas de emails enviados
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEmailStatistics() {
        log.info("📈 Admin solicitando estadísticas de emails");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Aquí puedes agregar consultas a la base de datos para obtener estadísticas reales
        stats.put("totalEmailsSent", "Ver EmailNotificationRepository");
        stats.put("emailsToday", "Implementar consulta por fecha");
        stats.put("emailsThisWeek", "Implementar consulta por rango");
        stats.put("openRate", "Calcular desde EmailEvent");
        stats.put("responseRate", "Calcular desde respuestas");
        
        stats.put("note", "Estadísticas detalladas disponibles en la tabla de notificaciones");
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Información de configuración para el administrador
     */
    @GetMapping("/configuration-help")
    public ResponseEntity<Map<String, Object>> getConfigurationHelp() {
        Map<String, Object> help = new HashMap<>();
        
        help.put("title", "Guía de Configuración del Sistema de Emails");
        
        Map<String, String> envVars = new HashMap<>();
        envVars.put("EMAIL_MOCK_MODE", "true/false - Controla si los emails se envían realmente");
        envVars.put("SMTP_HOST", "smtp.gmail.com - Servidor SMTP");
        envVars.put("SMTP_PORT", "587 - Puerto SMTP");
        envVars.put("SMTP_USERNAME", "tu-email@gmail.com - Usuario SMTP");
        envVars.put("SMTP_PASSWORD", "tu-app-password - Contraseña de aplicación de Gmail");
        envVars.put("SMTP_AUTH", "true - Habilitar autenticación");
        envVars.put("SMTP_STARTTLS", "true - Habilitar TLS");
        
        help.put("environmentVariables", envVars);
        
        help.put("steps", new String[]{
            "1. Crear contraseña de aplicación en Gmail",
            "2. Configurar variables de entorno",
            "3. Establecer EMAIL_MOCK_MODE=false para producción",
            "4. Probar con el endpoint /test",
            "5. Monitorear logs para confirmar entregas"
        });
        
        help.put("currentStatus", mockMode ? "DESARROLLO" : "PRODUCCIÓN");
        
        return ResponseEntity.ok(help);
    }
}