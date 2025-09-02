package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.EmailNotification;
import com.desafios.admision_mtn.entity.EmailEvent;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import com.desafios.admision_mtn.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/interview-response")
@RequiredArgsConstructor
@Slf4j
public class InterviewResponseController {
    
    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailEventRepository emailEventRepository;
    private final EmailService emailService;
    
    /**
     * Confirmar asistencia a entrevista
     */
    @GetMapping("/{token}/confirm")
    public ResponseEntity<String> confirmAttendance(@PathVariable String token) {
        return processInterviewResponse(token, EmailNotification.ResponseValue.ACCEPT, "✅ CONFIRMADO", 
            "Su asistencia ha sido confirmada exitosamente");
    }
    
    /**
     * Solicitar reprogramación de entrevista
     */
    @GetMapping("/{token}/reschedule")
    public ResponseEntity<String> requestReschedule(@PathVariable String token) {
        return processInterviewResponse(token, EmailNotification.ResponseValue.RESCHEDULE, "📅 REPROGRAMACIÓN", 
            "Su solicitud de reprogramación ha sido recibida");
    }
    
    /**
     * Cancelar asistencia a entrevista
     */
    @GetMapping("/{token}/cancel")
    public ResponseEntity<String> cancelAttendance(@PathVariable String token) {
        return processInterviewResponse(token, EmailNotification.ResponseValue.REJECT, "❌ CANCELADO", 
            "Su cancelación ha sido registrada");
    }
    
    private ResponseEntity<String> processInterviewResponse(String token, EmailNotification.ResponseValue responseValue, 
                                                          String statusLabel, String message) {
        try {
            log.info("🔗 Procesando respuesta de entrevista con token: {} - Acción: {}", token, responseValue);
            
            // Buscar la notificación por token
            Optional<EmailNotification> notificationOpt = emailNotificationRepository.findByResponseToken(token);
            
            if (notificationOpt.isEmpty()) {
                log.warn("⚠️ Token de respuesta no encontrado: {}", token);
                return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(generateErrorPage("Token de respuesta inválido o expirado"));
            }
            
            EmailNotification notification = notificationOpt.get();
            
            // Verificar si ya fue respondido
            if (notification.getResponded()) {
                log.info("ℹ️ Respuesta ya procesada anteriormente para token: {}", token);
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(generateAlreadyRespondedPage(notification));
            }
            
            // Actualizar la notificación
            notification.setResponded(true);
            notification.setRespondedAt(LocalDateTime.now());
            notification.setResponseValue(responseValue);
            emailNotificationRepository.save(notification);
            
            // Crear evento
            createResponseEvent(notification, responseValue, message);
            
            // Enviar email de confirmación al apoderado
            sendConfirmationEmail(notification, responseValue, statusLabel, message);
            
            // Notificar al admin (opcional)
            notifyAdminOfResponse(notification, responseValue, statusLabel);
            
            log.info("✅ Respuesta procesada exitosamente: {} para {}", responseValue, notification.getRecipientEmail());
            
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(generateSuccessPage(notification, statusLabel, message, responseValue));
                
        } catch (Exception e) {
            log.error("❌ Error procesando respuesta de entrevista: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_HTML)
                .body(generateErrorPage("Error interno del servidor"));
        }
    }
    
    private void createResponseEvent(EmailNotification notification, EmailNotification.ResponseValue responseValue, String message) {
        EmailEvent event = new EmailEvent();
        event.setEmailNotification(notification);
        event.setEventType(EmailEvent.EventType.RESPONDED);
        event.setDescription(String.format("Respuesta automática recibida: %s - %s", responseValue.getDisplayName(), message));
        event.setEventDate(LocalDateTime.now());
        emailEventRepository.save(event);
    }
    
    private void sendConfirmationEmail(EmailNotification notification, EmailNotification.ResponseValue responseValue, 
                                     String statusLabel, String message) {
        try {
            String studentName = notification.getStudentName();
            String confirmationMessage = generateConfirmationMessage(responseValue, studentName, statusLabel);
            
            emailService.sendSimpleMessage(
                notification.getRecipientEmail(),
                "Confirmación de Respuesta - Entrevista " + studentName,
                confirmationMessage
            );
            
            log.info("📧 Email de confirmación enviado a: {}", notification.getRecipientEmail());
            
        } catch (Exception e) {
            log.error("❌ Error enviando confirmación por email: {}", e.getMessage(), e);
        }
    }
    
    private void notifyAdminOfResponse(EmailNotification notification, EmailNotification.ResponseValue responseValue, String statusLabel) {
        try {
            String adminNotification = String.format("""
                Nueva respuesta de entrevista recibida:
                
                Estudiante: %s
                Email: %s
                Respuesta: %s
                Fecha: %s
                Token: %s
                
                Revisar en el sistema de administración para más detalles.
                """,
                notification.getStudentName(),
                notification.getRecipientEmail(),
                statusLabel,
                LocalDateTime.now(),
                notification.getResponseToken()
            );
            
            // Enviar notificación a admin (usando email configurado)
            emailService.sendSimpleMessage(
                "jorge.gangale@mtn.cl", // Email del admin
                "🔔 Nueva Respuesta de Entrevista - " + notification.getStudentName(),
                adminNotification
            );
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación al admin: {}", e.getMessage(), e);
        }
    }
    
    private String generateConfirmationMessage(EmailNotification.ResponseValue responseValue, String studentName, String statusLabel) {
        return switch (responseValue) {
            case ACCEPT -> String.format("""
                Estimado(a) Apoderado(a),
                
                Su confirmación de asistencia para la entrevista de %s ha sido recibida exitosamente.
                
                Estado: %s
                Fecha de confirmación: %s
                
                Nos complace confirmar que los esperamos en la fecha y hora programada.
                
                Si necesita realizar algún cambio, por favor contacte directamente a nuestras oficinas.
                
                Atentamente,
                Equipo de Admisiones
                Monte Tabor & Nazaret
                """, studentName, statusLabel, LocalDateTime.now());
                
            case RESCHEDULE -> String.format("""
                Estimado(a) Apoderado(a),
                
                Su solicitud de reprogramación para la entrevista de %s ha sido recibida.
                
                Estado: %s
                Fecha de solicitud: %s
                
                Nuestro equipo de admisiones se comunicará con usted en las próximas 24 horas 
                para coordinar una nueva fecha y hora que sea conveniente para ambas partes.
                
                Gracias por informarnos con anticipación.
                
                Atentamente,
                Equipo de Admisiones
                Monte Tabor & Nazaret
                """, studentName, statusLabel, LocalDateTime.now());
                
            case REJECT -> String.format("""
                Estimado(a) Apoderado(a),
                
                Hemos recibido su cancelación para la entrevista de %s.
                
                Estado: %s
                Fecha de cancelación: %s
                
                Lamentamos que no puedan asistir en esta ocasión. 
                Si desean reprogramar para una fecha futura, por favor contacten directamente 
                a nuestras oficinas de admisiones.
                
                Quedamos a su disposición.
                
                Atentamente,
                Equipo de Admisiones
                Monte Tabor & Nazaret
                """, studentName, statusLabel, LocalDateTime.now());
                
            default -> String.format("""
                Estimado(a) Apoderado(a),
                
                Su respuesta para la entrevista de %s ha sido registrada.
                
                Estado: %s
                Fecha: %s
                
                Gracias por responder. Si tiene consultas adicionales, 
                no dude en contactar nuestras oficinas.
                
                Atentamente,
                Equipo de Admisiones
                Monte Tabor & Nazaret
                """, studentName, statusLabel, LocalDateTime.now());
        };
    }
    
    private String generateSuccessPage(EmailNotification notification, String statusLabel, 
                                     String message, EmailNotification.ResponseValue responseValue) {
        String emoji = switch (responseValue) {
            case ACCEPT -> "✅";
            case RESCHEDULE -> "📅";
            case REJECT -> "❌";
            default -> "📝";
        };
        
        String actionMessage = switch (responseValue) {
            case ACCEPT -> "Nos complace confirmar su asistencia. Los esperamos en la fecha programada.";
            case RESCHEDULE -> "Nos comunicaremos con usted en las próximas 24 horas para reprogramar.";
            case REJECT -> "Su cancelación ha sido registrada. Si cambia de opinión, puede contactarnos directamente.";
            default -> "Su respuesta ha sido registrada correctamente.";
        };
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Respuesta Confirmada - Monte Tabor & Nazaret</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); min-height: 100vh; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 40px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); }
                    .success-icon { text-align: center; font-size: 80px; margin-bottom: 20px; }
                    h1 { color: #2d3748; text-align: center; margin-bottom: 30px; }
                    .status-badge { display: inline-block; padding: 10px 20px; background: #48bb78; color: white; border-radius: 25px; font-weight: bold; margin-bottom: 20px; }
                    .student-info { background: #f7fafc; padding: 20px; border-radius: 10px; margin: 20px 0; }
                    .action-info { background: #e6fffa; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #38b2ac; }
                    .footer { text-align: center; margin-top: 30px; color: #718096; font-size: 14px; }
                    .contact-info { background: #fff5f5; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #fc8181; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">%s</div>
                    <h1>¡Respuesta Confirmada!</h1>
                    
                    <div class="status-badge">%s</div>
                    
                    <div class="student-info">
                        <h3>Información de la Respuesta:</h3>
                        <p><strong>Estudiante:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Fecha de respuesta:</strong> %s</p>
                    </div>
                    
                    <div class="action-info">
                        <h3>📧 Confirmación enviada</h3>
                        <p>%s</p>
                        <p><strong>Se ha enviado un email de confirmación a su correo electrónico con todos los detalles.</strong></p>
                    </div>
                    
                    <div class="contact-info">
                        <h3>📞 ¿Necesita ayuda?</h3>
                        <p>Si tiene consultas adicionales, puede contactarnos directamente:</p>
                        <p><strong>📧 Email:</strong> admision@mtn.cl</p>
                        <p><strong>📱 Teléfono:</strong> +56 2 2234 5678</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Colegio Monte Tabor y Nazaret</strong></p>
                        <p>Sistema de Admisiones - Respuesta Automática</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            emoji, statusLabel, notification.getStudentName(), notification.getRecipientEmail(), 
            LocalDateTime.now(), actionMessage);
    }
    
    private String generateAlreadyRespondedPage(EmailNotification notification) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ya Respondido - Monte Tabor & Nazaret</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #ffeaa7 0%%, #fab1a0 100%%); min-height: 100vh; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 40px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); }
                    .warning-icon { text-align: center; font-size: 80px; margin-bottom: 20px; }
                    h1 { color: #2d3748; text-align: center; margin-bottom: 30px; }
                    .info-box { background: #fff3cd; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #ffc107; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="warning-icon">⚠️</div>
                    <h1>Respuesta Ya Registrada</h1>
                    
                    <div class="info-box">
                        <h3>Su respuesta ya fue procesada anteriormente</h3>
                        <p><strong>Estudiante:</strong> %s</p>
                        <p><strong>Fecha de respuesta:</strong> %s</p>
                        <p><strong>Respuesta registrada:</strong> %s</p>
                    </div>
                    
                    <p>Si necesita hacer cambios, por favor contacte directamente a nuestras oficinas.</p>
                </div>
            </body>
            </html>
            """, 
            notification.getStudentName(), 
            notification.getRespondedAt() != null ? notification.getRespondedAt() : "No disponible",
            notification.getResponseValue() != null ? notification.getResponseValue() : "No disponible");
    }
    
    private String generateErrorPage(String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Error - Monte Tabor & Nazaret</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #ff7675 0%%, #e17055 100%%); min-height: 100vh; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; padding: 40px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); }
                    .error-icon { text-align: center; font-size: 80px; margin-bottom: 20px; }
                    h1 { color: #2d3748; text-align: center; margin-bottom: 30px; }
                    .error-box { background: #fed7d7; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #f56565; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">❌</div>
                    <h1>Error en la Respuesta</h1>
                    
                    <div class="error-box">
                        <h3>No se pudo procesar su respuesta</h3>
                        <p>%s</p>
                    </div>
                    
                    <p>Por favor, contacte directamente a nuestras oficinas para obtener asistencia.</p>
                    <p><strong>📧 Email:</strong> admision@mtn.cl</p>
                </div>
            </body>
            </html>
            """, errorMessage);
    }
}