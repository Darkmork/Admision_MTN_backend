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
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;
    
    @Value("${app.email.mock-mode:true}")
    private boolean mockMode;
    
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
            String subject = notification.getSubject(); // El subject ya está asignado en createEmailNotification
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
            
            // Verificar modo mock/development
            if (mockMode || "dev".equals(activeProfile)) {
                logger.info("📧 [MODO DESARROLLO] Email institucional para aplicación {}", application.getId());
                logger.info("📬 [DESTINATARIO]: {}", notification.getRecipientEmail());
                logger.info("📝 [ASUNTO]: {}", subject);
                logger.info("🏫 [TIPO]: {}", emailType.getDisplayName());
                logger.info("👨‍👩‍👧 [ESTUDIANTE]: {}", notification.getStudentName());
                logger.info("📄 [CONTENIDO HTML]: {}", htmlContent.substring(0, Math.min(500, htmlContent.length())) + "...");
                
                // Actualizar estado mock
                notification.setSentAt(LocalDateTime.now());
                notification.setDelivered(true);
                emailNotificationRepository.save(notification);
                
                createEmailEvent(notification, EmailEvent.EventType.SENT, "Email institucional simulado (modo desarrollo)");
                
                return CompletableFuture.completedFuture(true);
            }
            
            // Enviar email real
            mailSender.send(message);
            
            // Actualizar estado y crear evento
            notification.setSentAt(LocalDateTime.now());
            notification.setDelivered(true);
            emailNotificationRepository.save(notification);
            
            createEmailEvent(notification, EmailEvent.EventType.SENT, "Email institucional enviado exitosamente");
            
            logger.info("✅ Email institucional enviado exitosamente para aplicación {} a {}", 
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
        
        // Generar y asignar el subject antes de guardar
        String subject = generateInstitutionalSubject(emailType, notification);
        notification.setSubject(subject);
        
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

        // Saludo personalizado mejorado
        html.append("""
            <div class="personalized-greeting">
                <h2>Estimado(a) Apoderado(a),</h2>
                <p>Nos dirigimos a usted en relación a la postulación de <strong>%s</strong> a <strong>%s</strong>.</p>
                <p style="color: %s; font-weight: bold;">Nos complace estar en contacto con la familia de %s.</p>
            </div>
            """.formatted(
                getPersonalizedGreeting(notification),
                getSchoolContext(notification),
                schoolColor,
                getPersonalizedReference(notification)
            ));

        // Contenido específico del tipo de email
        html.append(generateContentByType(notification, templateData, schoolName));

        // Botones de respuesta automática mejorados si es necesario
        if (notification.getResponseRequired()) {
            String personalizedRef = getPersonalizedReference(notification);
            html.append("""
                <div style="text-align: center; margin: 40px 0; padding: 30px; background: linear-gradient(135deg, #f8fafc 0%%, #e2e8f0 100%%); border-radius: 15px; border: 2px solid %s;">
                    <h3 style="color: %s; margin-bottom: 20px;">🤝 Confirmación de Asistencia</h3>
                    <p style="margin-bottom: 25px; font-size: 16px;">Por favor, confirme la asistencia de %s haciendo clic en una de las siguientes opciones:</p>
                    
                    <div style="display: flex; justify-content: center; gap: 20px; flex-wrap: wrap;">
                        <a href="%s/api/admin/interview-response/%s/confirm" 
                           style="display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #10b981, #059669); color: white; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3); transition: all 0.3s;">
                            ✅ CONFIRMO ASISTENCIA
                        </a>
                        
                        <a href="%s/api/admin/interview-response/%s/reschedule" 
                           style="display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #f59e0b, #d97706); color: white; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);">
                            📅 SOLICITAR REPROGRAMACIÓN
                        </a>
                        
                        <a href="%s/api/admin/interview-response/%s/cancel" 
                           style="display: inline-block; padding: 15px 30px; background: linear-gradient(135deg, #ef4444, #dc2626); color: white; text-decoration: none; border-radius: 12px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);">
                            ❌ NO PUEDO ASISTIR
                        </a>
                    </div>
                    
                    <p style="margin-top: 20px; font-size: 12px; color: #6b7280;">
                        Al hacer clic, recibirá una confirmación inmediata por email.<br>
                        Para consultas adicionales, puede contactar directamente a nuestras oficinas.
                    </p>
                </div>
                
                <div style="background-color: #f0f9ff; padding: 20px; border-radius: 10px; margin: 20px 0; border-left: 4px solid #3b82f6;">
                    <p style="margin: 0; font-size: 14px; color: #1e40af;">
                        <strong>💡 Importante:</strong> Este sistema de respuesta automática le confirmará inmediatamente su decisión. 
                        Si necesita hacer cambios posteriores, puede contactarnos directamente.
                    </p>
                </div>
                """.formatted(
                    schoolColor, schoolColor, personalizedRef,
                    baseUrl, notification.getResponseToken(),
                    baseUrl, notification.getResponseToken(), 
                    baseUrl, notification.getResponseToken()
                ));
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
        String personalizedRef = getPersonalizedReference(notification);
        String schoolTreatment = getSchoolSpecificTreatment(notification);
        String schoolContext = getSchoolContext(notification);
        
        return switch (notification.getEmailType()) {
            case APPLICATION_RECEIVED -> String.format("""
                <h3 style="color: #059669;">✅ Postulación Recibida Exitosamente</h3>
                <p>Hemos recibido exitosamente la postulación de <strong>%s</strong> para el año escolar 2025 en <strong>%s</strong>.</p>
                <div style="background-color: #f0f9ff; padding: 15px; border-radius: 8px; margin: 15px 0;">
                    <p><strong>📋 Número de Postulación:</strong> #%d</p>
                    <p><strong>📅 Fecha de Recepción:</strong> %s</p>
                    <p><strong>🎯 Modalidad:</strong> Postulación a %s</p>
                </div>
                <p>Nos complace informarle que la documentación de %s ha sido registrada correctamente en nuestro sistema.</p>
                <p><strong>En los próximos días recibirá información sobre los siguientes pasos del proceso de admisión.</strong></p>
                """, 
                getPersonalizedGreeting(notification),
                schoolContext,
                notification.getApplication().getId(),
                notification.getCreatedAt().toLocalDate().toString(),
                schoolName,
                personalizedRef
            );
            
            case INTERVIEW_INVITATION -> {
                String fecha = (String) templateData.getOrDefault("fecha", "A confirmar");
                String hora = (String) templateData.getOrDefault("hora", "A confirmar");
                String lugar = (String) templateData.getOrDefault("lugar", "Oficinas de Admisión");
                yield String.format("""
                    <h3 style="color: #2563eb;">📅 Invitación a Entrevista Personal</h3>
                    <p>Nos complace extender una cordial invitación para la <strong>entrevista personal</strong> de <strong>%s</strong> como parte del proceso de admisión a <strong>%s</strong>.</p>
                    <p>Esta entrevista nos permitirá conocer mejor a %s y a su familia, así como resolver cualquier consulta sobre nuestra propuesta educativa.</p>
                    
                    <div style="background-color: #eff6ff; padding: 25px; border-radius: 12px; margin: 25px 0; border-left: 5px solid #2563eb;">
                        <h4 style="margin-top: 0; color: #1e40af;">📋 Detalles de la Cita</h4>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr><td style="padding: 8px 0;"><strong>📅 Fecha:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                            <tr><td style="padding: 8px 0;"><strong>🕐 Hora:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                            <tr><td style="padding: 8px 0;"><strong>📍 Lugar:</strong></td><td style="padding: 8px 0;">%s - %s</td></tr>
                            <tr><td style="padding: 8px 0;"><strong>⏱️ Duración:</strong></td><td style="padding: 8px 0;">Aproximadamente 45 minutos</td></tr>
                            <tr><td style="padding: 8px 0;"><strong>👥 Participantes:</strong></td><td style="padding: 8px 0;">Apoderados y %s</td></tr>
                        </table>
                    </div>
                    
                    <div style="background-color: #fefce8; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b;">
                        <h4 style="margin-top: 0; color: #92400e;">📝 Documentos a traer:</h4>
                        <ul style="margin: 10px 0; padding-left: 20px;">
                            <li>Documento de identidad de los apoderados</li>
                            <li>Documento de identidad de %s</li>
                            <li>Último informe académico (si corresponde)</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0; padding: 20px; background-color: #f8fafc; border-radius: 10px;">
                        <p style="font-size: 16px; font-weight: bold; margin-bottom: 15px;">⚠️ CONFIRMACIÓN REQUERIDA</p>
                        <p style="margin-bottom: 20px;">Por favor, confirme su asistencia haciendo clic en uno de los siguientes botones:</p>
                    </div>
                    """, 
                    getPersonalizedGreeting(notification),
                    schoolContext,
                    personalizedRef,
                    fecha, hora, lugar, schoolName,
                    schoolTreatment,
                    personalizedRef
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

    // Métodos utilitarios mejorados para personalización...
    
    /**
     * Obtiene la forma correcta de referirse al estudiante según escuela y género
     */
    private String getPersonalizedReference(EmailNotification notification) {
        boolean isMaleSchool = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR;
        boolean isFemaleName = notification.getStudentGender() == EmailNotification.Gender.FEMALE;
        
        if (isMaleSchool) { // Monte Tabor (masculino)
            return isFemaleName ? "su hija" : "su hijo";
        } else { // Nazaret (femenino)
            return "su hija"; // Nazaret siempre femenino
        }
    }
    
    /**
     * Obtiene el tratamiento apropiado según la escuela
     */
    private String getSchoolSpecificTreatment(EmailNotification notification) {
        boolean isMaleSchool = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR;
        boolean isFemaleName = notification.getStudentGender() == EmailNotification.Gender.FEMALE;
        
        if (isMaleSchool) { // Monte Tabor
            return isFemaleName ? "la alumna" : "el alumno";
        } else { // Nazaret  
            return "la alumna"; // Siempre femenino
        }
    }
    
    /**
     * Obtiene el contexto escolar específico
     */
    private String getSchoolContext(EmailNotification notification) {
        if (notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR) {
            return "nuestro colegio de varones Monte Tabor";
        } else {
            return "nuestro colegio de señoritas Nazaret";
        }
    }
    
    /**
     * Obtiene saludo personalizado según género y escuela
     */
    private String getPersonalizedGreeting(EmailNotification notification) {
        String firstName = notification.getStudentName().split(" ")[0];
        boolean isMaleSchool = notification.getTargetSchool() == EmailNotification.TargetSchool.MONTE_TABOR;
        boolean isFemaleName = notification.getStudentGender() == EmailNotification.Gender.FEMALE;
        
        if (isMaleSchool) { // Monte Tabor
            if (isFemaleName) {
                return String.format("la señorita %s, quien postula a nuestro colegio de varones", firstName);
            } else {
                return String.format("el joven %s", firstName);
            }
        } else { // Nazaret
            return String.format("la señorita %s", firstName);
        }
    }
    
    private EmailNotification.Gender determineGender(String firstName) {
        String name = firstName.toLowerCase().trim();
        
        String[] femaleNames = {
            "maría", "ana", "carmen", "francisca", "valentina", "sofia", "isidora", 
            "antonia", "amanda", "camila", "javiera", "constanza", "maría josé",
            "carolina", "andrea", "patricia", "lorena", "claudia", "daniela",
            "fernanda", "catalina", "ignacia", "josefa", "esperanza", "pilar",
            "trinidad", "macarena", "bárbara", "paz", "rosario", "magdalena"
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

    /**
     * Método específico para crear y encolar un email con contenido ya procesado por templates
     */
    public void createAndQueueEmail(String recipientEmail, String processedSubject, String processedContent, EmailNotification.EmailType emailType) {
        logger.info("Creando y encolando email templated: {} para {}", emailType, recipientEmail);
        
        try {
            // Crear la notificación de email con contenido ya procesado
            EmailNotification notification = new EmailNotification();
            notification.setRecipientEmail(recipientEmail);
            notification.setSubject(processedSubject);
            notification.setEmailType(emailType);
            notification.setDelivered(false);
            notification.setOpened(false);
                
            // Guardar en la base de datos (esto lo añade a la cola)
            EmailNotification savedNotification = emailNotificationRepository.save(notification);
            
            // Crear evento de creación
            createEmailEvent(savedNotification, EmailEvent.EventType.QUEUED, 
                "Email templated creado y encolado: " + emailType.getDisplayName());
            
            logger.info("Email templated encolado exitosamente con ID: {}", savedNotification.getId());
            
        } catch (Exception e) {
            logger.error("Error creando email templated en cola: {}", e.getMessage(), e);
            throw new RuntimeException("Error encolando email templated", e);
        }
    }
}