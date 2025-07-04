package com.desafios.robotcode.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@robotcode.mtn.cl}")
    private String fromEmail;
    
    @Value("${app.name:RobotCode Arena}")
    private String appName;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationEmail(String toEmail, String username, String verificationCode) {
        try {
            // En modo desarrollo, solo logueamos el código
            logger.info("=== EMAIL DE VERIFICACIÓN ===");
            logger.info("Para: {}", toEmail);
            logger.info("Usuario: {}", username);
            logger.info("CÓDIGO DE VERIFICACIÓN: {}", verificationCode);
            logger.info("============================");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verificación de cuenta - " + appName);
            
            String emailBody = buildVerificationEmailBody(username, verificationCode);
            message.setText(emailBody);
            
            // Intentar enviar email, pero no fallar si no se puede
            try {
                mailSender.send(message);
                logger.info("Verification email sent successfully to: {}", toEmail);
            } catch (Exception mailException) {
                logger.warn("Mail sending failed, but continuing (development mode): {}", mailException.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process verification email for: {}", toEmail, e);
            // En desarrollo, no lanzar excepción para que el proceso continúe
            logger.warn("Continuing without email in development mode");
        }
    }
    
    private String buildVerificationEmailBody(String username, String verificationCode) {
        return String.format(
            "¡Hola %s!\n\n" +
            "Gracias por registrarte en %s.\n\n" +
            "Tu código de verificación es: %s\n\n" +
            "Este código expira en 10 minutos.\n\n" +
            "Si no solicitaste esta verificación, puedes ignorar este mensaje.\n\n" +
            "¡Que disfrutes programando!\n\n" +
            "Equipo %s\n" +
            "---\n" +
            "Este es un mensaje automático, por favor no respondas a este correo.",
            username,
            appName,
            verificationCode,
            appName
        );
    }
    
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            logger.info("=== EMAIL DE BIENVENIDA ===");
            logger.info("Para: {}", toEmail);
            logger.info("Usuario: {}", username);
            logger.info("Cuenta verificada exitosamente!");
            logger.info("==========================");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¡Bienvenido a " + appName + "!");
            
            String emailBody = buildWelcomeEmailBody(username);
            message.setText(emailBody);
            
            try {
                mailSender.send(message);
                logger.info("Welcome email sent successfully to: {}", toEmail);
            } catch (Exception mailException) {
                logger.warn("Welcome email sending failed, but continuing: {}", mailException.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process welcome email for: {}", toEmail, e);
            // No lanzar excepción aquí para no fallar el registro si el email de bienvenida falla
        }
    }
    
    private String buildWelcomeEmailBody(String username) {
        return String.format(
            "¡Hola %s!\n\n" +
            "¡Tu cuenta en %s ha sido verificada exitosamente!\n\n" +
            "Ya puedes comenzar a resolver desafíos de programación y mejorar tus habilidades.\n\n" +
            "Características que puedes disfrutar:\n" +
            "• Problemas categorizados por tema y dificultad\n" +
            "• Editor de código integrado\n" +
            "• Sistema de puntos y ranking\n" +
            "• Seguimiento de tu progreso\n\n" +
            "¡Que comience la aventura de programación!\n\n" +
            "Equipo %s",
            username,
            appName,
            appName
        );
    }
    
    public void sendContactEmail(String senderName, String senderEmail, String subject, String messageContent) {
        try {
            logger.info("=== EMAIL DE CONTACTO ===");
            logger.info("De: {} <{}>", senderName, senderEmail);
            logger.info("Asunto: {}", subject);
            logger.info("Mensaje: {}", messageContent);
            logger.info("========================");
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("jorge.gangale@mtn.cl"); // Test temporal - cambiar después
            message.setReplyTo(senderEmail); // Para que pueda responder directamente
            message.setSubject("[RobotCode Arena] " + subject);
            
            String emailBody = buildContactEmailBody(senderName, senderEmail, subject, messageContent);
            message.setText(emailBody);
            
            try {
                logger.info("Intentando enviar email...");
                logger.info("From: {}", fromEmail);
                logger.info("To: contacto@jgangale.cl");
                logger.info("Subject: [RobotCode Arena] {}", subject);
                
                mailSender.send(message);
                logger.info("Contact email sent successfully from: {} <{}>", senderName, senderEmail);
            } catch (Exception mailException) {
                logger.error("Contact email sending failed: {}", mailException.getMessage());
                logger.error("Exception type: {}", mailException.getClass().getSimpleName());
                if (mailException.getCause() != null) {
                    logger.error("Root cause: {}", mailException.getCause().getMessage());
                }
                throw new RuntimeException("No se pudo enviar el mensaje de contacto: " + mailException.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Failed to process contact email from: {} <{}>", senderName, senderEmail, e);
            throw new RuntimeException("Error procesando el mensaje de contacto: " + e.getMessage());
        }
    }
    
    private String buildContactEmailBody(String senderName, String senderEmail, String subject, String messageContent) {
        return String.format(
            "=== NUEVO MENSAJE DE CONTACTO ===\n\n" +
            "Has recibido un nuevo mensaje desde RobotCode Arena:\n\n" +
            "👤 Nombre: %s\n" +
            "📧 Email: %s\n" +
            "📝 Asunto: %s\n\n" +
            "💬 Mensaje:\n" +
            "%s\n\n" +
            "---\n" +
            "Este mensaje fue enviado desde el formulario de contacto de RobotCode Arena.\n" +
            "Para responder, simplemente responde a este email.\n" +
            "Fecha: %s",
            senderName,
            senderEmail,
            subject,
            messageContent,
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
    }
}