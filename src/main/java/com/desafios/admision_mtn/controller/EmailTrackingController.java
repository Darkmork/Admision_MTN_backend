package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.EmailEvent;
import com.desafios.admision_mtn.entity.EmailNotification;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Permitir CORS para tracking
public class EmailTrackingController {

    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailEventRepository emailEventRepository;

    /**
     * Tracking pixel - Se ejecuta cuando el usuario abre el correo
     */
    @GetMapping("/track/{trackingToken}")
    public ResponseEntity<byte[]> trackEmailOpen(@PathVariable String trackingToken, 
                                               HttpServletRequest request) {
        try {
            log.info("📧 Tracking email open for token: {}", trackingToken);
            
            Optional<EmailNotification> notificationOpt = emailNotificationRepository.findByTrackingToken(trackingToken);
            
            if (notificationOpt.isPresent()) {
                EmailNotification notification = notificationOpt.get();
                
                // Actualizar contador de apertura
                if (!notification.hasBeenOpened()) {
                    notification.setOpened(true);
                    notification.setOpenedAt(LocalDateTime.now());
                }
                notification.setOpenCount(notification.getOpenCount() + 1);
                emailNotificationRepository.save(notification);
                
                // Registrar evento
                String ipAddress = getClientIpAddress(request);
                String userAgent = request.getHeader("User-Agent");
                
                EmailEvent openEvent = EmailEvent.createOpenedEvent(notification, ipAddress, userAgent);
                emailEventRepository.save(openEvent);
                
                log.info("✅ Email opened tracked successfully for application ID: {}", 
                    notification.getApplication().getId());
                
            } else {
                log.warn("⚠️ Tracking token not found: {}", trackingToken);
            }
            
        } catch (Exception e) {
            log.error("❌ Error tracking email open: {}", e.getMessage(), e);
        }
        
        // Devolver pixel transparente de 1x1
        byte[] pixelImage = createTransparentPixel();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .header(HttpHeaders.EXPIRES, "0")
            .body(pixelImage);
    }

    /**
     * Respuesta automática - Aceptar propuesta
     */
    @GetMapping("/respond/{responseToken}/accept")
    public ResponseEntity<String> acceptProposal(@PathVariable String responseToken,
                                                HttpServletRequest request) {
        return handleResponse(responseToken, EmailNotification.ResponseValue.ACCEPT, request, 
            "¡Perfecto! Su confirmación de asistencia ha sido registrada exitosamente.");
    }

    /**
     * Respuesta automática - Rechazar propuesta
     */
    @GetMapping("/respond/{responseToken}/reject")
    public ResponseEntity<String> rejectProposal(@PathVariable String responseToken,
                                               HttpServletRequest request) {
        return handleResponse(responseToken, EmailNotification.ResponseValue.REJECT, request,
            "Su respuesta ha sido registrada. Nos pondremos en contacto con ustedes próximamente.");
    }

    /**
     * Respuesta automática - Solicitar reprogramación
     */
    @GetMapping("/respond/{responseToken}/reschedule")
    public ResponseEntity<String> requestReschedule(@PathVariable String responseToken,
                                                   HttpServletRequest request) {
        return handleResponse(responseToken, EmailNotification.ResponseValue.RESCHEDULE, request,
            "Su solicitud de reprogramación ha sido registrada. Nos contactaremos con ustedes para coordinar una nueva fecha.");
    }

    private ResponseEntity<String> handleResponse(String responseToken, 
                                                EmailNotification.ResponseValue responseValue,
                                                HttpServletRequest request, 
                                                String successMessage) {
        try {
            log.info("📧 Processing email response: {} for token: {}", responseValue, responseToken);
            
            Optional<EmailNotification> notificationOpt = emailNotificationRepository.findByResponseToken(responseToken);
            
            if (notificationOpt.isEmpty()) {
                return ResponseEntity.ok(buildErrorPage("Token de respuesta no válido", 
                    "El enlace al que accedió no es válido o ha expirado."));
            }
            
            EmailNotification notification = notificationOpt.get();
            
            if (notification.hasResponded()) {
                return ResponseEntity.ok(buildInfoPage("Respuesta ya registrada", 
                    "Su respuesta ya fue registrada anteriormente como: " + 
                    notification.getResponseValue().getDisplayName()));
            }
            
            // Registrar respuesta
            notification.setResponded(true);
            notification.setResponseValue(responseValue);
            notification.setRespondedAt(LocalDateTime.now());
            emailNotificationRepository.save(notification);
            
            // Registrar evento
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            EmailEvent responseEvent = EmailEvent.createRespondedEvent(notification, responseValue.name());
            responseEvent.setIpAddress(ipAddress);
            responseEvent.setUserAgent(userAgent);
            emailEventRepository.save(responseEvent);
            
            log.info("✅ Email response registered successfully: {} for application ID: {}", 
                responseValue, notification.getApplication().getId());
            
            return ResponseEntity.ok(buildSuccessPage("Respuesta Registrada", successMessage, notification));
            
        } catch (Exception e) {
            log.error("❌ Error processing email response: {}", e.getMessage(), e);
            return ResponseEntity.ok(buildErrorPage("Error Interno", 
                "Ocurrió un error al procesar su respuesta. Por favor, contacte al equipo de admisiones."));
        }
    }

    private String buildSuccessPage(String title, String message, EmailNotification notification) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s - %s</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f5f7fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 0 20px rgba(0,0,0,0.1); text-align: center; }
                    .success-icon { font-size: 64px; color: #10b981; margin-bottom: 20px; }
                    .title { font-size: 24px; font-weight: 600; color: #1e3a8a; margin-bottom: 15px; }
                    .message { font-size: 16px; color: #4b5563; margin-bottom: 30px; }
                    .details { background-color: #f0f9ff; padding: 20px; border-radius: 6px; margin: 20px 0; }
                    .contact-info { font-size: 14px; color: #6b7280; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                    .btn { display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <h1 class="title">%s</h1>
                    <p class="message">%s</p>
                    
                    <div class="details">
                        <p><strong>Estudiante:</strong> %s</p>
                        <p><strong>Fecha de respuesta:</strong> %s</p>
                        <p><strong>Su respuesta:</strong> %s</p>
                    </div>
                    
                    <p>Puede cerrar esta ventana. Si necesita realizar algún cambio, por favor contacte directamente al equipo de admisiones.</p>
                    
                    <a href="mailto:admisiones@mtn.cl" class="btn">Contactar Admisiones</a>
                    
                    <div class="contact-info">
                        <p><strong>Equipo de Admisiones</strong><br>
                        📧 admisiones@mtn.cl<br>
                        📞 +56 2 XXXX XXXX</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            title, notification.getSchoolReference(), // page title
            title, // header title
            message, // message
            notification.getStudentName(), // student name
            LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm")), // response date
            notification.getResponseValue().getDisplayName() // response value
        );
    }

    private String buildErrorPage(String title, String message) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f5f7fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 0 20px rgba(0,0,0,0.1); text-align: center; }
                    .error-icon { font-size: 64px; color: #ef4444; margin-bottom: 20px; }
                    .title { font-size: 24px; font-weight: 600; color: #dc2626; margin-bottom: 15px; }
                    .message { font-size: 16px; color: #4b5563; margin-bottom: 30px; }
                    .contact-info { font-size: 14px; color: #6b7280; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                    .btn { display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">❌</div>
                    <h1 class="title">%s</h1>
                    <p class="message">%s</p>
                    
                    <p>Si necesita asistencia, por favor contacte al equipo de admisiones.</p>
                    
                    <a href="mailto:admisiones@mtn.cl" class="btn">Contactar Admisiones</a>
                    
                    <div class="contact-info">
                        <p><strong>Equipo de Admisiones</strong><br>
                        📧 admisiones@mtn.cl<br>
                        📞 +56 2 XXXX XXXX</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            title, title, message
        );
    }

    private String buildInfoPage(String title, String message) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f5f7fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 40px; border-radius: 8px; box-shadow: 0 0 20px rgba(0,0,0,0.1); text-align: center; }
                    .info-icon { font-size: 64px; color: #3b82f6; margin-bottom: 20px; }
                    .title { font-size: 24px; font-weight: 600; color: #1e3a8a; margin-bottom: 15px; }
                    .message { font-size: 16px; color: #4b5563; margin-bottom: 30px; }
                    .contact-info { font-size: 14px; color: #6b7280; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; }
                    .btn { display: inline-block; padding: 12px 24px; background-color: #3b82f6; color: white; text-decoration: none; border-radius: 6px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="info-icon">ℹ️</div>
                    <h1 class="title">%s</h1>
                    <p class="message">%s</p>
                    
                    <a href="mailto:admisiones@mtn.cl" class="btn">Contactar Admisiones</a>
                    
                    <div class="contact-info">
                        <p><strong>Equipo de Admisiones</strong><br>
                        📧 admisiones@mtn.cl<br>
                        📞 +56 2 XXXX XXXX</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            title, title, message
        );
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    private byte[] createTransparentPixel() {
        // PNG transparente de 1x1 pixel
        return new byte[]{
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
            (byte)0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00,
            0x00, 0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte)0xB4,
            0x00, 0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44,
            (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
    }
}