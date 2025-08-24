package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class InstitutionalEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(InstitutionalEmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private EmailNotificationRepository emailNotificationRepository;
    
    @Autowired
    private EmailEventRepository emailEventRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    public CompletableFuture<Boolean> sendInstitutionalNotification(
            Application application, 
            EmailNotification.EmailType emailType,
            Map<String, Object> templateData) {
        
        try {
            // Crear notificación en base de datos
            EmailNotification notification = createEmailNotification(application, emailType);
            
            // Generar contenido institucional personalizado
            String subject = generateInstitutionalSubject(emailType, notification);
            String htmlContent = generateInstitutionalHtmlContent(notification, templateData);
            
            // Configurar y enviar email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Configuración institucional
            helper.setFrom(fromEmail, "Colegio Monte Tabor y Nazaret - Sistema de Admisión");
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Headers institucionales
            message.setHeader("X-Mailer", "Sistema Admision MTN v1.0");
            message.setHeader("Organization", "Colegio Monte Tabor y Nazaret");
            
            // Enviar email
            mailSender.send(message);
            
            // Actualizar estado y crear evento
            notification.setSentAt(LocalDateTime.now());
            notification.setDelivered(true);
            emailNotificationRepository.save(notification);
            
            createEmailEvent(notification, EmailEvent.EventType.SENT, "Email institucional enviado exitosamente");
            
            logger.info("Email institucional enviado exitosamente para aplicación {} a {}", 
                       application.getId(), notification.getRecipientEmail());
            
            return CompletableFuture.completedFuture(true);
            
        } catch (Exception e) {
            logger.error("Error enviando email institucional para aplicación {}: {}", 
                        application.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }

    private EmailNotification createEmailNotification(Application application, EmailNotification.EmailType emailType) {
        Student student = application.getStudent();
        
        EmailNotification notification = new EmailNotification();
        notification.setApplication(application);
        notification.setRecipientEmail(getParentEmail(application));
        notification.setEmailType(emailType);
        notification.setStudentName(student.getFirstName() + " " + student.getPaternalLastName());
        notification.setStudentGender(determineGender(student.getFirstName()));
        notification.setTargetSchool(determineTargetSchool(student));
        notification.setTrackingToken(generateSecureToken());
        
        // Configurar respuesta automática para ciertos tipos
        if (emailType == EmailNotification.EmailType.INTERVIEW_INVITATION) {
            notification.setResponseRequired(true);
            notification.setResponseToken(generateSecureToken());
        }
        
        notification.setCreatedAt(LocalDateTime.now());
        
        return emailNotificationRepository.save(notification);
    }

    private String generateInstitutionalSubject(EmailNotification.EmailType emailType, EmailNotification notification) {
        String schoolName = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR 
            ? "Monte Tabor" : "Nazaret";
        
        return switch (emailType) {
            case APPLICATION_RECEIVED -> 
                String.format("Postulación Recibida - %s %s - Colegio %s", 
                    notification.getStudentGender().getPrefix(), 
                    notification.getStudentName().split(" ")[0], 
                    schoolName);
                    
            case INTERVIEW_INVITATION -> 
                String.format("Invitación a Entrevista - %s %s - Colegio %s", 
                    notification.getStudentGender().getPrefix(), 
                    notification.getStudentName().split(" ")[0], 
                    schoolName);
                    
            case APPLICATION_STATUS_UPDATE -> 
                String.format("Actualización de Estado - %s %s - Colegio %s", 
                    notification.getStudentGender().getPrefix(), 
                    notification.getStudentName().split(" ")[0], 
                    schoolName);
                    
            case DOCUMENT_REMINDER -> 
                String.format("Documentos Pendientes - %s %s - Colegio %s", 
                    notification.getStudentGender().getPrefix(), 
                    notification.getStudentName().split(" ")[0], 
                    schoolName);
                    
            case ADMISSION_RESULT -> 
                String.format("Resultado de Admisión - %s %s - Colegio %s", 
                    notification.getStudentGender().getPrefix(), 
                    notification.getStudentName().split(" ")[0], 
                    schoolName);
                    
            default -> String.format("Notificación - Colegio %s", schoolName);
        };
    }

    private String generateInstitutionalHtmlContent(EmailNotification notification, Map<String, Object> templateData) {
        String schoolName = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR 
            ? "Monte Tabor" : "Nazaret";
        String schoolColor = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR 
            ? "#2563eb" : "#dc2626"; // Azul para Monte Tabor, Rojo para Nazaret
        
        StringBuilder html = new StringBuilder();
        
        // Header institucional
        html.append("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Colegio %s - Sistema de Admisión</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-color: #f8fafc; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; }
                    .header { background: linear-gradient(135deg, %s 0%%, %s 100%%); color: white; padding: 30px 20px; text-align: center; }
                    .logo { font-size: 28px; font-weight: bold; margin-bottom: 10px; }
                    .content { padding: 30px 20px; line-height: 1.6; color: #374151; }
                    .personalized-greeting { background-color: #f3f4f6; padding: 20px; border-left: 4px solid %s; margin: 20px 0; }
                    .button { display: inline-block; padding: 12px 24px; margin: 10px 5px; text-decoration: none; border-radius: 6px; font-weight: bold; text-align: center; }
                    .button-accept { background-color: #10b981; color: white; }
                    .button-reject { background-color: #ef4444; color: white; }
                    .footer { background-color: #f9fafb; padding: 20px; text-align: center; font-size: 12px; color: #6b7280; }
                    .tracking { width: 1px; height: 1px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">Colegio %s</div>
                        <div>Sistema de Admisión 2025</div>
                    </div>
                    <div class="content">
            """.formatted(schoolName, schoolColor, schoolColor, schoolColor, schoolName));

        // Saludo personalizado
        html.append("""
            <div class="personalized-greeting">
                <h2>Estimado(a) Apoderado(a),</h2>
                <p>Nos dirigimos a usted en relación a la postulación de <strong>%s %s</strong> al <strong>Colegio %s</strong>.</p>
            </div>
            """.formatted(
                notification.getStudentGender().getPrefix(),
                notification.getStudentName().split(" ")[0],
                schoolName
            ));

        // Contenido específico del tipo de email
        html.append(generateContentByType(notification, templateData, schoolName));

        // Botones de respuesta si es necesario
        if (notification.getResponseRequired()) {
            html.append("""
                <div style="text-align: center; margin: 30px 0;">
                    <p><strong>Por favor, confirme su respuesta:</strong></p>
                    <a href="%s/api/emails/respond/%s/accept" class="button button-accept">✓ ACEPTO</a>
                    <a href="%s/api/emails/respond/%s/reject" class="button button-reject">✗ NO ACEPTO</a>
                </div>
                """.formatted(baseUrl, notification.getResponseToken(), baseUrl, notification.getResponseToken()));
        }

        // Footer institucional
        html.append("""
                    </div>
                    <div class="footer">
                        <p><strong>Colegio %s</strong></p>
                        <p>📧 admision@mtn.cl | 📞 +56 2 2234 5678 | 🌐 www.mtn.cl</p>
                        <p>Este es un correo automático del Sistema de Admisión. Por favor, no responda directamente.</p>
                        <p style="font-size: 10px; color: #9ca3af;">
                            Si no puede ver este email correctamente, 
                            <a href="%s/api/emails/view/%s" style="color: %s;">haga clic aquí</a>
                        </p>
                    </div>
                </div>
                <img src="%s/api/emails/track/%s" class="tracking" alt="" />
            </body>
            </html>
            """.formatted(schoolName, baseUrl, notification.getTrackingToken(), schoolColor, baseUrl, notification.getTrackingToken()));

        return html.toString();
    }

    private String generateContentByType(EmailNotification notification, Map<String, Object> templateData, String schoolName) {
        return switch (notification.getEmailType()) {
            case APPLICATION_RECEIVED -> String.format("""
                <h3 style="color: #059669;">✅ Postulación Recibida Exitosamente</h3>
                <p>Hemos recibido la postulación de %s %s para el año escolar 2025 en el Colegio %s.</p>
                <p><strong>Número de Postulación:</strong> #%d</p>
                <p><strong>Fecha de Recepción:</strong> %s</p>
                <p>En los próximos días recibirá información sobre los siguientes pasos del proceso de admisión.</p>
                """, 
                notification.getStudentGender().getPrefix(),
                notification.getStudentName().split(" ")[0],
                schoolName,
                notification.getApplication().getId(),
                notification.getCreatedAt().toLocalDate().toString()
            );
            
            case INTERVIEW_INVITATION -> {
                String fecha = (String) templateData.getOrDefault("fecha", "A confirmar");
                String hora = (String) templateData.getOrDefault("hora", "A confirmar");
                yield String.format("""
                    <h3 style="color: #2563eb;">📅 Invitación a Entrevista</h3>
                    <p>Nos complace invitarlos a la entrevista para %s %s como parte del proceso de admisión al Colegio %s.</p>
                    <div style="background-color: #eff6ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <p><strong>📅 Fecha:</strong> %s</p>
                        <p><strong>🕐 Hora:</strong> %s</p>
                        <p><strong>📍 Lugar:</strong> Oficinas de Admisión - Colegio %s</p>
                        <p><strong>⏱️ Duración:</strong> Aproximadamente 45 minutos</p>
                    </div>
                    <p><strong>Por favor, confirme su asistencia usando los botones a continuación.</strong></p>
                    """, 
                    notification.getStudentGender().getPrefix(),
                    notification.getStudentName().split(" ")[0],
                    schoolName, fecha, hora, schoolName
                );
            }
            
            case APPLICATION_STATUS_UPDATE -> {
                String nuevoEstado = (String) templateData.getOrDefault("nuevoEstado", "En revisión");
                yield String.format("""
                    <h3 style="color: #7c3aed;">📋 Actualización de Estado</h3>
                    <p>El estado de la postulación de %s %s ha sido actualizado.</p>
                    <div style="background-color: #faf5ff; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <p><strong>Estado Actual:</strong> <span style="color: #7c3aed; font-weight: bold;">%s</span></p>
                    </div>
                    <p>Puede revisar el estado completo ingresando a nuestro sistema con sus credenciales.</p>
                    """, 
                    notification.getStudentGender().getPrefix(),
                    notification.getStudentName().split(" ")[0],
                    nuevoEstado
                );
            }
            
            case DOCUMENT_REMINDER -> {
                String documentos = (String) templateData.getOrDefault("documentosPendientes", "documentos requeridos");
                yield String.format("""
                    <h3 style="color: #f59e0b;">📄 Documentos Pendientes</h3>
                    <p>Para continuar con el proceso de postulación de %s %s, necesitamos que complete la entrega de los siguientes documentos:</p>
                    <div style="background-color: #fffbeb; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b;">
                        <p><strong>Documentos Pendientes:</strong></p>
                        <p>%s</p>
                    </div>
                    <p>Por favor, ingrese al sistema y cargue los documentos en la sección correspondiente.</p>
                    """, 
                    notification.getStudentGender().getPrefix(),
                    notification.getStudentName().split(" ")[0],
                    documentos
                );
            }
            
            case ADMISSION_RESULT -> {
                String resultado = (String) templateData.getOrDefault("resultado", "En proceso");
                String mensaje = (String) templateData.getOrDefault("mensaje", "");
                yield String.format("""
                    <h3 style="color: #059669;">🎉 Resultado del Proceso de Admisión</h3>
                    <p>El proceso de admisión de %s %s al Colegio %s ha concluido.</p>
                    <div style="background-color: #ecfdf5; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;">
                        <h2 style="color: #059669; margin: 0;">%s</h2>
                    </div>
                    %s
                    """, 
                    notification.getStudentGender().getPrefix(),
                    notification.getStudentName().split(" ")[0],
                    schoolName,
                    resultado,
                    mensaje.isEmpty() ? "" : "<p>" + mensaje + "</p>"
                );
            }
            
            default -> String.format("""
                <p>Le informamos sobre una actualización en el proceso de postulación de %s %s.</p>
                <p>Para más detalles, ingrese al sistema con sus credenciales.</p>
                """, 
                notification.getStudentGender().getPrefix(),
                notification.getStudentName().split(" ")[0]
            );
        };
    }

    // Métodos utilitarios existentes...
    private EmailNotification.Gender determineGender(String firstName) {
        String name = firstName.toLowerCase().trim();
        
        String[] femaleNames = {
            "maría", "ana", "carmen", "francisca", "valentina", "sofia", "isidora", 
            "antonia", "amanda", "camila", "javiera", "constanza", "maría josé",
            "carolina", "andrea", "patricia", "lorena", "claudia", "daniela"
        };
        
        for (String femaleName : femaleNames) {
            if (name.contains(femaleName)) {
                return EmailNotification.Gender.FEMALE;
            }
        }
        
        return EmailNotification.Gender.MALE;
    }

    private EmailNotification.TargetSchool determineTargetSchool(Student student) {
        // Por ahora, usar lógica simple basada en el RUT o edad
        // En el futuro, esto puede ser configurado por el usuario
        if (student.getBirthDate() != null) {
            int age = java.time.Period.between(student.getBirthDate(), java.time.LocalDate.now()).getYears();
            return age <= 10 ? EmailNotification.TargetSchool.MONTE_TABOR : EmailNotification.TargetSchool.NAZARET;
        }
        return EmailNotification.TargetSchool.MONTE_TABOR;
    }

    private String getParentEmail(Application application) {
        if (application.getFather() != null && application.getFather().getEmail() != null) {
            return application.getFather().getEmail();
        }
        if (application.getMother() != null && application.getMother().getEmail() != null) {
            return application.getMother().getEmail();
        }
        if (application.getApplicantUser() != null) {
            return application.getApplicantUser().getEmail();
        }
        throw new RuntimeException("No se encontró email de contacto para la aplicación " + application.getId());
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
    }

    private void createEmailEvent(EmailNotification notification, EmailEvent.EventType eventType, String description) {
        EmailEvent event = new EmailEvent();
        event.setEmailNotification(notification);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setEventDate(LocalDateTime.now());
        emailEventRepository.save(event);
    }

    // Método público para enviar diferentes tipos de emails
    public CompletableFuture<Boolean> sendApplicationReceivedEmail(Application application) {
        Map<String, Object> data = new HashMap<>();
        return sendInstitutionalNotification(application, EmailNotification.EmailType.APPLICATION_RECEIVED, data);
    }

    public CompletableFuture<Boolean> sendInterviewInvitationEmail(Application application, Interview interview) {
        Map<String, Object> data = new HashMap<>();
        data.put("fecha", interview.getInterviewDate().toString());
        data.put("hora", interview.getInterviewTime() != null ? interview.getInterviewTime().toString() : "Por confirmar");
        return sendInstitutionalNotification(application, EmailNotification.EmailType.INTERVIEW_INVITATION, data);
    }

    public CompletableFuture<Boolean> sendStatusUpdateEmail(Application application, String newStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("nuevoEstado", newStatus);
        return sendInstitutionalNotification(application, EmailNotification.EmailType.APPLICATION_STATUS_UPDATE, data);
    }

    public CompletableFuture<Boolean> sendDocumentReminderEmail(Application application, String pendingDocuments) {
        Map<String, Object> data = new HashMap<>();
        data.put("documentosPendientes", pendingDocuments);
        return sendInstitutionalNotification(application, EmailNotification.EmailType.DOCUMENT_REMINDER, data);
    }

    public CompletableFuture<Boolean> sendAdmissionResultEmail(Application application, String result, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("resultado", result);
        data.put("mensaje", message);
        return sendInstitutionalNotification(application, EmailNotification.EmailType.ADMISSION_RESULT, data);
    }
}