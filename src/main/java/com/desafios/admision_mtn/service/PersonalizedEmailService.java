package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalizedEmailService {

    private final JavaMailSender mailSender;
    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailEventRepository emailEventRepository;

    @Value("${app.mail.from:admisiones@mtn.cl}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("es-CL"));

    @Async
    @Transactional
    public void sendPersonalizedInterviewNotification(Interview interview) {
        try {
            log.info("Enviando notificación personalizada de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            Student student = application.getStudent();
            
            // Determinar género y colegio
            EmailNotification.Gender gender = determineGender(student.getFirstName());
            EmailNotification.TargetSchool targetSchool = determineTargetSchool(student);
            
            // Construir subject personalizado
            String subject = String.format("Entrevista Programada - %s (%s)", 
                student.getFirstName() + " " + student.getLastName(), 
                targetSchool.getDisplayName());
            
            // Crear registro de notificación
            EmailNotification notification = createEmailNotification(
                application,
                EmailNotification.EmailType.INTERVIEW_SCHEDULED,
                student.getFirstName() + " " + student.getLastName(),
                gender,
                targetSchool,
                true, // Requiere respuesta
                interview
            );
            
            // Configurar subject antes de guardar
            notification.setSubject(subject);
            
            // Guardar notificación
            notification = emailNotificationRepository.save(notification);
            
            // Construir email personalizado
            String emailBody = buildPersonalizedInterviewEmail(notification, interview);
            
            // Enviar email
            sendHtmlEmail(notification.getRecipientEmail(), subject, emailBody);
            
            // Marcar como enviado
            notification.setSentAt(LocalDateTime.now());
            emailNotificationRepository.save(notification);
            
            // Registrar evento de envío
            EmailEvent sentEvent = EmailEvent.createSentEvent(notification);
            emailEventRepository.save(sentEvent);
            
            log.info("✅ Notificación personalizada de entrevista enviada exitosamente a: {}", notification.getRecipientEmail());
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación personalizada de entrevista: {}", e.getMessage(), e);
        }
    }

    @Async
    @Transactional
    public void sendPersonalizedApplicationStatusUpdate(Application application, String status, String message) {
        try {
            Student student = application.getStudent();
            EmailNotification.Gender gender = determineGender(student.getFirstName());
            EmailNotification.TargetSchool targetSchool = determineTargetSchool(student);
            
            EmailNotification.EmailType emailType = switch (status) {
                case "APPROVED" -> EmailNotification.EmailType.ACCEPTANCE_NOTIFICATION;
                case "REJECTED" -> EmailNotification.EmailType.REJECTION_NOTIFICATION;
                case "WAITLIST" -> EmailNotification.EmailType.WAITLIST_NOTIFICATION;
                default -> EmailNotification.EmailType.GENERAL_COMMUNICATION;
            };
            
            EmailNotification notification = createEmailNotification(
                application,
                emailType,
                student.getFirstName() + " " + student.getLastName(),
                gender,
                targetSchool,
                false, // No requiere respuesta automática
                null
            );
            
            notification = emailNotificationRepository.save(notification);
            
            String subject = String.format("%s - %s (%s)", 
                emailType.getDisplayName(),
                student.getFirstName() + " " + student.getLastName(), 
                targetSchool.getDisplayName());
            notification.setSubject(subject);
            
            String emailBody = buildPersonalizedStatusEmail(notification, status, message);
            
            sendHtmlEmail(notification.getRecipientEmail(), subject, emailBody);
            
            notification.setSentAt(LocalDateTime.now());
            emailNotificationRepository.save(notification);
            
            EmailEvent sentEvent = EmailEvent.createSentEvent(notification);
            emailEventRepository.save(sentEvent);
            
            log.info("✅ Notificación personalizada de estado enviada exitosamente");
            
        } catch (Exception e) {
            log.error("❌ Error enviando notificación personalizada de estado: {}", e.getMessage(), e);
        }
    }

    private EmailNotification createEmailNotification(Application application, 
                                                    EmailNotification.EmailType emailType, 
                                                    String studentName, 
                                                    EmailNotification.Gender gender,
                                                    EmailNotification.TargetSchool targetSchool,
                                                    boolean requiresResponse,
                                                    Interview interview) {
        
        String trackingToken = UUID.randomUUID().toString().replace("-", "");
        String responseToken = requiresResponse ? UUID.randomUUID().toString().replace("-", "") : null;
        
        return EmailNotification.builder()
            .application(application)
            .recipientEmail(application.getApplicantUser().getEmail())
            .emailType(emailType)
            .studentName(studentName)
            .studentGender(gender)
            .targetSchool(targetSchool)
            .trackingToken(trackingToken)
            .responseRequired(requiresResponse)
            .responseToken(responseToken)
            .interview(interview)
            .build();
    }

    private String buildPersonalizedInterviewEmail(EmailNotification notification, Interview interview) {
        String formattedDate = interview.getScheduledDate().format(DATE_FORMATTER);
        String formattedTime = interview.getScheduledTime().format(TIME_FORMATTER);
        String studentReference = notification.getPersonalizedStudentReference();
        String schoolReference = notification.getSchoolReference();
        
        // URLs para tracking y respuesta
        String trackingPixelUrl = String.format("%s/api/emails/track/%s", backendUrl, notification.getTrackingToken());
        String acceptUrl = String.format("%s/api/emails/respond/%s/accept", backendUrl, notification.getResponseToken());
        String rejectUrl = String.format("%s/api/emails/respond/%s/reject", backendUrl, notification.getResponseToken());
        String rescheduleUrl = String.format("%s/api/emails/respond/%s/reschedule", backendUrl, notification.getResponseToken());
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f7fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #1e3a8a 0%%, #3b82f6 100%%); color: white; padding: 30px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .header p { margin: 8px 0 0 0; opacity: 0.9; }
                    .content { padding: 30px 20px; }
                    .greeting { font-size: 16px; margin-bottom: 20px; color: #374151; }
                    .student-info { background-color: #f8fafc; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #3b82f6; }
                    .details { background-color: white; padding: 20px; border-radius: 8px; margin: 20px 0; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
                    .detail-item { display: flex; margin: 12px 0; align-items: center; }
                    .detail-label { font-weight: 600; min-width: 120px; color: #4b5563; }
                    .detail-value { color: #111827; }
                    .icon { width: 20px; height: 20px; margin-right: 8px; }
                    .response-section { background-color: #f0f9ff; padding: 25px; border-radius: 8px; margin: 25px 0; text-align: center; }
                    .response-buttons { margin: 20px 0; }
                    .btn { display: inline-block; padding: 12px 24px; margin: 8px; text-decoration: none; border-radius: 6px; font-weight: 600; text-align: center; transition: all 0.3s; }
                    .btn-accept { background-color: #10b981; color: white; }
                    .btn-reject { background-color: #ef4444; color: white; }
                    .btn-reschedule { background-color: #f59e0b; color: white; }
                    .btn:hover { transform: translateY(-1px); box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
                    .footer { padding: 25px 20px; text-align: center; background-color: #f9fafb; color: #6b7280; font-size: 13px; }
                    .important-note { background-color: #fef3c7; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #f59e0b; }
                    @media (max-width: 600px) {
                        .container { margin: 0; box-shadow: none; }
                        .content { padding: 20px 15px; }
                        .btn { display: block; margin: 10px 0; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎓 Entrevista Programada</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Estimada familia,
                        </div>
                        
                        <p>Nos complace informarles que se ha programado una entrevista para <strong>%s</strong> como parte del proceso de admisión al <strong>%s</strong>.</p>
                        
                        <div class="student-info">
                            <h3>👨‍🎓 Información del Estudiante</h3>
                            <p><strong>Estudiante:</strong> %s</p>
                            <p><strong>Proceso de admisión:</strong> %s</p>
                        </div>
                        
                        <div class="details">
                            <h3>📅 Detalles de la Entrevista</h3>
                            <div class="detail-item">
                                <span class="detail-label">📆 Fecha:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">⏰ Hora:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">⌚ Duración:</span>
                                <span class="detail-value">%d minutos</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">👨‍🏫 Entrevistador:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-item">
                                <span class="detail-label">📍 Modalidad:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="response-section">
                            <h3>💬 Confirme su Asistencia</h3>
                            <p>Por favor, confirmen la disponibilidad de %s para la fecha y hora programada:</p>
                            <div class="response-buttons">
                                <a href="%s" class="btn btn-accept">✅ Confirmar Asistencia</a>
                                <a href="%s" class="btn btn-reschedule">📅 Solicitar Reprogramación</a>
                                <a href="%s" class="btn btn-reject">❌ No Podemos Asistir</a>
                            </div>
                            <p style="font-size: 13px; color: #6b7280; margin-top: 15px;">
                                Al hacer clic en cualquier botón, recibirán automáticamente una confirmación de su respuesta.
                            </p>
                        </div>
                        
                        <div class="important-note">
                            <strong>📋 Recordatorios Importantes:</strong>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>Llegar 15 minutos antes de la hora programada</li>
                                <li>Traer documentos de identificación de %s</li>
                                <li>Si es modalidad virtual, verificar la conexión previamente</li>
                                <li>En caso de inconveniente, contactar inmediatamente</li>
                            </ul>
                        </div>
                        
                        <p>Quedamos atentos a su confirmación y esperamos conocer mejor a %s en esta importante etapa del proceso.</p>
                        
                        <p>¡Saludos cordiales!</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Equipo de Admisiones</strong><br>
                        %s<br>
                        📧 Email: %s<br>
                        📞 Teléfono: +56 2 XXXX XXXX</p>
                        
                        <p style="margin-top: 15px; font-size: 11px;">
                            Este correo es generado automáticamente. Las respuestas a través de los botones son procesadas inmediatamente.
                        </p>
                    </div>
                </div>
                
                <!-- Tracking pixel -->
                <img src="%s" width="1" height="1" style="display:none;" alt="">
            </body>
            </html>
            """,
            notification.getSubject(), // title
            schoolReference, // header subtitle
            studentReference, // greeting text
            schoolReference, // school reference
            notification.getStudentName(), // student info
            schoolReference, // admission process
            formattedDate, // date
            formattedTime, // time
            interview.getDuration(), // duration
            interview.getInterviewerName(), // interviewer
            getModeLabel(interview.getMode()), // mode
            notification.getStudentName(), // confirmation text
            acceptUrl, // accept button
            rescheduleUrl, // reschedule button  
            rejectUrl, // reject button
            notification.getStudentName(), // reminder text
            studentReference, // closing text
            schoolReference, // footer school
            fromEmail, // footer email
            trackingPixelUrl // tracking pixel
        );
    }

    private String buildPersonalizedStatusEmail(EmailNotification notification, String status, String message) {
        String studentReference = notification.getPersonalizedStudentReference();
        String schoolReference = notification.getSchoolReference();
        String trackingPixelUrl = String.format("%s/api/emails/track/%s", backendUrl, notification.getTrackingToken());
        
        String statusTitle = switch (status) {
            case "APPROVED" -> "🎉 ¡Felicitaciones! Admisión Aprobada";
            case "REJECTED" -> "📋 Resultado del Proceso de Admisión";
            case "WAITLIST" -> "⏳ Lista de Espera - Proceso de Admisión";
            default -> "📬 Actualización del Proceso de Admisión";
        };
        
        String statusColor = switch (status) {
            case "APPROVED" -> "#10b981";
            case "REJECTED" -> "#6b7280";
            case "WAITLIST" -> "#f59e0b";
            default -> "#3b82f6";
        };
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f7fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; box-shadow: 0 0 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, %s 0%%, #60a5fa 100%%); color: white; padding: 30px 20px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px 20px; }
                    .message-box { background-color: #f8fafc; padding: 25px; border-radius: 8px; margin: 20px 0; }
                    .footer { padding: 25px 20px; text-align: center; background-color: #f9fafb; color: #6b7280; font-size: 13px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>%s</p>
                    </div>
                    
                    <div class="content">
                        <div class="greeting">
                            Estimada familia,
                        </div>
                        
                        <p>Les escribimos para informarles sobre el resultado del proceso de admisión de <strong>%s</strong> al <strong>%s</strong>.</p>
                        
                        <div class="message-box">
                            %s
                        </div>
                        
                        <p>Agradecemos su interés en nuestro proyecto educativo y la confianza depositada en nosotros.</p>
                        
                        <p>¡Saludos cordiales!</p>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Equipo de Admisiones</strong><br>
                        %s<br>
                        📧 Email: %s</p>
                    </div>
                </div>
                
                <!-- Tracking pixel -->
                <img src="%s" width="1" height="1" style="display:none;" alt="">
            </body>
            </html>
            """,
            notification.getSubject(), // title
            statusColor, // header color
            statusTitle, // header title
            schoolReference, // header subtitle
            studentReference, // greeting text
            schoolReference, // school reference
            message, // message content
            schoolReference, // footer school
            fromEmail, // footer email
            trackingPixelUrl // tracking pixel
        );
    }

    private void sendHtmlEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        
        mailSender.send(message);
    }

    private EmailNotification.Gender determineGender(String firstName) {
        // Lista de nombres femeninos comunes en Chile
        String[] femaleNames = {"ana", "maría", "carmen", "elena", "patricia", "sofía", "isabella", "valentina", 
                                "emilia", "magdalena", "esperanza", "paz", "fernanda", "isidora", "martina"};
        
        String lowerName = firstName.toLowerCase();
        for (String femaleName : femaleNames) {
            if (lowerName.contains(femaleName)) {
                return EmailNotification.Gender.FEMALE;
            }
        }
        
        return EmailNotification.Gender.MALE; // Default
    }

    private EmailNotification.TargetSchool determineTargetSchool(Student student) {
        // Usar el campo schoolApplied de la entidad Student
        if (student.getSchoolApplied() != null) {
            return "NAZARET".equals(student.getSchoolApplied()) ? 
                EmailNotification.TargetSchool.NAZARET : EmailNotification.TargetSchool.MONTE_TABOR;
        }
        
        // Fallback basado en curso aplicado
        String grade = student.getGradeApplied();
        if (grade != null) {
            if (grade.contains("Medio") || grade.contains("3° Básico") || grade.contains("4° Básico")) {
                return EmailNotification.TargetSchool.NAZARET;
            }
        }
        
        return EmailNotification.TargetSchool.MONTE_TABOR; // Default
    }

    private String getModeLabel(Interview.InterviewMode mode) {
        return switch (mode) {
            case IN_PERSON -> "Presencial";
            case VIRTUAL -> "Virtual (online)";
            case HYBRID -> "Híbrida";
        };
    }
}