package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@mtn.cl}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5176}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("es-CL"));

    @Async
    public void sendInterviewScheduledNotification(Interview interview) {
        try {
            log.info("Enviando notificación de entrevista programada para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            String recipientEmail = application.getApplicantUser().getEmail();
            String studentName = interview.getStudentName();
            String interviewerName = interview.getInterviewerName();
            
            String subject = "Entrevista Programada - " + studentName + " - Colegio Monte Tabor y Nazaret";
            String body = buildScheduledEmailBody(interview, studentName, interviewerName);
            
            sendHtmlEmail(recipientEmail, subject, body);
            log.info("Notificación de entrevista programada enviada exitosamente a: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de entrevista programada: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendInterviewConfirmedNotification(Interview interview) {
        try {
            log.info("Enviando notificación de entrevista confirmada para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            String recipientEmail = application.getApplicantUser().getEmail();
            String studentName = interview.getStudentName();
            String interviewerName = interview.getInterviewerName();
            
            String subject = "Entrevista Confirmada - " + studentName + " - Colegio Monte Tabor y Nazaret";
            String body = buildConfirmedEmailBody(interview, studentName, interviewerName);
            
            sendHtmlEmail(recipientEmail, subject, body);
            log.info("Notificación de entrevista confirmada enviada exitosamente a: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de entrevista confirmada: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendInterviewRescheduledNotification(Interview interview) {
        try {
            log.info("Enviando notificación de entrevista reprogramada para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            String recipientEmail = application.getApplicantUser().getEmail();
            String studentName = interview.getStudentName();
            String interviewerName = interview.getInterviewerName();
            
            String subject = "Entrevista Reprogramada - " + studentName + " - Colegio Monte Tabor y Nazaret";
            String body = buildRescheduledEmailBody(interview, studentName, interviewerName);
            
            sendHtmlEmail(recipientEmail, subject, body);
            log.info("Notificación de entrevista reprogramada enviada exitosamente a: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de entrevista reprogramada: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendInterviewCancelledNotification(Interview interview, String reason) {
        try {
            log.info("Enviando notificación de entrevista cancelada para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            String recipientEmail = application.getApplicantUser().getEmail();
            String studentName = interview.getStudentName();
            
            String subject = "Entrevista Cancelada - " + studentName + " - Colegio Monte Tabor y Nazaret";
            String body = buildCancelledEmailBody(interview, studentName, reason);
            
            sendHtmlEmail(recipientEmail, subject, body);
            log.info("Notificación de entrevista cancelada enviada exitosamente a: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando notificación de entrevista cancelada: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendInterviewReminderNotification(Interview interview) {
        try {
            log.info("Enviando recordatorio de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            Application application = interview.getApplication();
            String recipientEmail = application.getApplicantUser().getEmail();
            String studentName = interview.getStudentName();
            String interviewerName = interview.getInterviewerName();
            
            String subject = "Recordatorio: Entrevista Mañana - " + studentName + " - Colegio Monte Tabor y Nazaret";
            String body = buildReminderEmailBody(interview, studentName, interviewerName);
            
            sendHtmlEmail(recipientEmail, subject, body);
            log.info("Recordatorio de entrevista enviado exitosamente a: {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando recordatorio de entrevista: {}", e.getMessage(), e);
        }
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

    private String buildScheduledEmailBody(Interview interview, String studentName, String interviewerName) {
        String formattedDate = interview.getScheduledDate().format(DATE_FORMATTER);
        String formattedTime = interview.getScheduledTime().format(TIME_FORMATTER);
        String typeLabel = getTypeLabel(interview.getType());
        String modeLabel = getModeLabel(interview.getMode());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #1e3a8a; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9fafb; }
                    .details { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #1e3a8a; color: white; text-decoration: none; border-radius: 6px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Entrevista Programada</h1>
                        <p>Colegio Monte Tabor y Nazaret</p>
                    </div>
                    
                    <div class="content">
                        <p>Estimada familia,</p>
                        
                        <p>Nos complace informarles que se ha programado una entrevista para <strong>%s</strong> como parte del proceso de admisión.</p>
                        
                        <div class="details">
                            <h3>Detalles de la Entrevista</h3>
                            <p><strong>Estudiante:</strong> %s</p>
                            <p><strong>Tipo de entrevista:</strong> %s</p>
                            <p><strong>Modalidad:</strong> %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                            <p><strong>Hora:</strong> %s</p>
                            <p><strong>Duración:</strong> %d minutos</p>
                            <p><strong>Entrevistador:</strong> %s</p>
                            %s
                        </div>
                        
                        %s
                        
                        <a href="%s/apoderado/login" class="button">Ver en Portal Familiar</a>
                        
                        <p>Por favor, confirmen su asistencia respondiendo a este correo o contactándose con nosotros.</p>
                        
                        <p><strong>Importante:</strong> Lleguen 15 minutos antes de la hora programada.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Colegio Monte Tabor y Nazaret<br>
                        Proceso de Admisión<br>
                        Este es un correo automático, por favor no responda directamente.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            studentName, studentName, typeLabel, modeLabel, formattedDate, formattedTime,
            interview.getDuration(), interviewerName,
            buildLocationInfo(interview),
            buildPreparationInfo(interview),
            frontendUrl
        );
    }

    private String buildConfirmedEmailBody(Interview interview, String studentName, String interviewerName) {
        String formattedDate = interview.getScheduledDate().format(DATE_FORMATTER);
        String formattedTime = interview.getScheduledTime().format(TIME_FORMATTER);
        String typeLabel = getTypeLabel(interview.getType());
        String modeLabel = getModeLabel(interview.getMode());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #059669; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f0fdf4; }
                    .details { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #059669; color: white; text-decoration: none; border-radius: 6px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Entrevista Confirmada</h1>
                        <p>Colegio Monte Tabor y Nazaret</p>
                    </div>
                    
                    <div class="content">
                        <p>Estimada familia,</p>
                        
                        <p>Su entrevista para <strong>%s</strong> ha sido <strong>confirmada</strong>.</p>
                        
                        <div class="details">
                            <h3>Detalles Confirmados</h3>
                            <p><strong>Estudiante:</strong> %s</p>
                            <p><strong>Tipo de entrevista:</strong> %s</p>
                            <p><strong>Modalidad:</strong> %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                            <p><strong>Hora:</strong> %s</p>
                            <p><strong>Duración:</strong> %d minutos</p>
                            <p><strong>Entrevistador:</strong> %s</p>
                            %s
                        </div>
                        
                        <a href="%s/apoderado/login" class="button">Ver en Portal Familiar</a>
                        
                        <p><strong>Recuerden:</strong> Llegar 15 minutos antes de la hora programada.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Colegio Monte Tabor y Nazaret<br>
                        Proceso de Admisión</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            studentName, studentName, typeLabel, modeLabel, formattedDate, formattedTime,
            interview.getDuration(), interviewerName,
            buildLocationInfo(interview),
            frontendUrl
        );
    }

    private String buildRescheduledEmailBody(Interview interview, String studentName, String interviewerName) {
        String formattedDate = interview.getScheduledDate().format(DATE_FORMATTER);
        String formattedTime = interview.getScheduledTime().format(TIME_FORMATTER);
        String typeLabel = getTypeLabel(interview.getType());
        String modeLabel = getModeLabel(interview.getMode());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #d97706; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #fffbeb; }
                    .details { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #d97706; color: white; text-decoration: none; border-radius: 6px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📅 Entrevista Reprogramada</h1>
                        <p>Colegio Monte Tabor y Nazaret</p>
                    </div>
                    
                    <div class="content">
                        <p>Estimada familia,</p>
                        
                        <p>La entrevista para <strong>%s</strong> ha sido reprogramada.</p>
                        
                        <div class="details">
                            <h3>Nueva Fecha y Hora</h3>
                            <p><strong>Estudiante:</strong> %s</p>
                            <p><strong>Tipo de entrevista:</strong> %s</p>
                            <p><strong>Modalidad:</strong> %s</p>
                            <p><strong>Nueva fecha:</strong> %s</p>
                            <p><strong>Nueva hora:</strong> %s</p>
                            <p><strong>Duración:</strong> %d minutos</p>
                            <p><strong>Entrevistador:</strong> %s</p>
                            %s
                        </div>
                        
                        <a href="%s/apoderado/login" class="button">Ver en Portal Familiar</a>
                        
                        <p>Disculpas por cualquier inconveniente. Por favor confirmen su disponibilidad para la nueva fecha.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Colegio Monte Tabor y Nazaret<br>
                        Proceso de Admisión</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            studentName, studentName, typeLabel, modeLabel, formattedDate, formattedTime,
            interview.getDuration(), interviewerName,
            buildLocationInfo(interview),
            frontendUrl
        );
    }

    private String buildCancelledEmailBody(Interview interview, String studentName, String reason) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #dc2626; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #fef2f2; }
                    .details { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Entrevista Cancelada</h1>
                        <p>Colegio Monte Tabor y Nazaret</p>
                    </div>
                    
                    <div class="content">
                        <p>Estimada familia,</p>
                        
                        <p>Lamentablemente, la entrevista programada para <strong>%s</strong> ha sido cancelada.</p>
                        
                        %s
                        
                        <p>Nos pondremos en contacto con ustedes próximamente para reprogramar.</p>
                        
                        <p>Disculpas por cualquier inconveniente ocasionado.</p>
                    </div>
                    
                    <div class="footer">
                        <p>Colegio Monte Tabor y Nazaret<br>
                        Proceso de Admisión</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            studentName,
            reason != null && !reason.trim().isEmpty() ? 
                "<div class=\"details\"><p><strong>Motivo:</strong> " + reason + "</p></div>" : ""
        );
    }

    private String buildReminderEmailBody(Interview interview, String studentName, String interviewerName) {
        String formattedDate = interview.getScheduledDate().format(DATE_FORMATTER);
        String formattedTime = interview.getScheduledTime().format(TIME_FORMATTER);
        String typeLabel = getTypeLabel(interview.getType());
        String modeLabel = getModeLabel(interview.getMode());
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #7c3aed; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #faf5ff; }
                    .details { background-color: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { padding: 20px; text-align: center; color: #666; font-size: 12px; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #7c3aed; color: white; text-decoration: none; border-radius: 6px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 Recordatorio de Entrevista</h1>
                        <p>Colegio Monte Tabor y Nazaret</p>
                    </div>
                    
                    <div class="content">
                        <p>Estimada familia,</p>
                        
                        <p>Este es un recordatorio de que <strong>%s</strong> tiene una entrevista programada para <strong>mañana</strong>.</p>
                        
                        <div class="details">
                            <h3>Detalles de la Entrevista</h3>
                            <p><strong>Estudiante:</strong> %s</p>
                            <p><strong>Tipo de entrevista:</strong> %s</p>
                            <p><strong>Modalidad:</strong> %s</p>
                            <p><strong>Fecha:</strong> %s</p>
                            <p><strong>Hora:</strong> %s</p>
                            <p><strong>Duración:</strong> %d minutos</p>
                            <p><strong>Entrevistador:</strong> %s</p>
                            %s
                        </div>
                        
                        <a href="%s/apoderado/login" class="button">Ver en Portal Familiar</a>
                        
                        <p><strong>Recordatorios importantes:</strong></p>
                        <ul>
                            <li>Llegar 15 minutos antes de la hora programada</li>
                            <li>Traer documentos de identificación</li>
                            <li>Confirmar asistencia si aún no lo han hecho</li>
                        </ul>
                    </div>
                    
                    <div class="footer">
                        <p>Colegio Monte Tabor y Nazaret<br>
                        Proceso de Admisión</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            studentName, studentName, typeLabel, modeLabel, formattedDate, formattedTime,
            interview.getDuration(), interviewerName,
            buildLocationInfo(interview),
            frontendUrl
        );
    }

    private String buildLocationInfo(Interview interview) {
        if (interview.getMode().toString().equals("VIRTUAL")) {
            return interview.getVirtualMeetingLink() != null ?
                "<p><strong>Enlace de reunión:</strong> <a href=\"" + interview.getVirtualMeetingLink() + "\">" + interview.getVirtualMeetingLink() + "</a></p>" :
                "<p><strong>Enlace de reunión:</strong> Se enviará próximamente</p>";
        } else if (interview.getLocation() != null) {
            return "<p><strong>Ubicación:</strong> " + interview.getLocation() + "</p>";
        }
        return "";
    }

    private String buildPreparationInfo(Interview interview) {
        if (interview.getPreparation() != null && !interview.getPreparation().trim().isEmpty()) {
            return "<div class=\"details\"><h3>Preparación</h3><p>" + interview.getPreparation() + "</p></div>";
        }
        return "";
    }

    private String getTypeLabel(Interview.InterviewType type) {
        return switch (type) {
            case INDIVIDUAL -> "Individual";
            case FAMILY -> "Familiar";
            case PSYCHOLOGICAL -> "Psicológica";
            case ACADEMIC -> "Académica";
            case BEHAVIORAL -> "Conductual";
        };
    }

    private String getModeLabel(Interview.InterviewMode mode) {
        return switch (mode) {
            case IN_PERSON -> "Presencial";
            case VIRTUAL -> "Virtual";
            case HYBRID -> "Híbrida";
        };
    }
}