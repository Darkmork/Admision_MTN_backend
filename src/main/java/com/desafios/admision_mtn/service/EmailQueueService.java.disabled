package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.EmailNotificationRepository;
import com.desafios.admision_mtn.repository.EmailEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

@Service
public class EmailQueueService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailQueueService.class);
    
    @Autowired
    private InstitutionalEmailService institutionalEmailService;
    
    @Autowired
    private EmailNotificationRepository emailNotificationRepository;
    
    @Autowired
    private EmailEventRepository emailEventRepository;
    
    // Control de rate limiting para email institucional
    private final AtomicInteger emailsSentThisHour = new AtomicInteger(0);
    private final AtomicInteger emailsSentThisDay = new AtomicInteger(0);
    private final AtomicInteger emailsSentThisMonth = new AtomicInteger(0);
    
    // Límites institucionales conservadores
    private static final int MAX_EMAILS_PER_HOUR = 50;    // Límite por hora
    private static final int MAX_EMAILS_PER_DAY = 200;     // Límite diario
    private static final int MAX_EMAILS_PER_MONTH = 5000;  // Límite mensual
    
    // Queue para emails pendientes
    private final ConcurrentHashMap<String, QueuedEmail> pendingEmails = new ConcurrentHashMap<>();
    
    public static class QueuedEmail {
        private final String id;
        private final Application application;
        private final EmailNotification.EmailType emailType;
        private final Map<String, Object> templateData;
        private final LocalDateTime queuedAt;
        private int retryCount;
        private LocalDateTime scheduledFor;
        
        public QueuedEmail(Application application, EmailNotification.EmailType emailType, Map<String, Object> templateData) {
            this.id = UUID.randomUUID().toString();
            this.application = application;
            this.emailType = emailType;
            this.templateData = new HashMap<>(templateData);
            this.queuedAt = LocalDateTime.now();
            this.retryCount = 0;
            this.scheduledFor = LocalDateTime.now();
        }
        
        // Getters
        public String getId() { return id; }
        public Application getApplication() { return application; }
        public EmailNotification.EmailType getEmailType() { return emailType; }
        public Map<String, Object> getTemplateData() { return templateData; }
        public LocalDateTime getQueuedAt() { return queuedAt; }
        public int getRetryCount() { return retryCount; }
        public LocalDateTime getScheduledFor() { return scheduledFor; }
        
        public void incrementRetry() {
            this.retryCount++;
            // Backoff exponencial: 5 min, 15 min, 45 min, 2 horas
            long delayMinutes = (long) Math.pow(3, retryCount) * 5;
            this.scheduledFor = LocalDateTime.now().plusMinutes(delayMinutes);
        }
        
        public boolean shouldRetry() {
            return retryCount < 4; // Máximo 4 intentos
        }
    }

    /**
     * Agregar email a la cola institucional
     */
    public String queueInstitutionalEmail(Application application, EmailNotification.EmailType emailType, Map<String, Object> templateData) {
        QueuedEmail queuedEmail = new QueuedEmail(application, emailType, templateData);
        
        // Verificar límites antes de agregar a la cola
        if (!canQueueMoreEmails()) {
            logger.warn("Límite de emails alcanzado. Email para aplicación {} agregado a cola con delay", application.getId());
            // Programar para la próxima hora
            queuedEmail.scheduledFor = getNextAvailableSlot();
        }
        
        pendingEmails.put(queuedEmail.getId(), queuedEmail);
        
        // Crear evento de queue
        createQueueEvent(application, "Email agregado a cola institucional: " + emailType.name());
        
        logger.info("Email {} agregado a cola institucional para aplicación {}", emailType, application.getId());
        return queuedEmail.getId();
    }

    /**
     * Procesamiento automático de cola cada 2 minutos
     */
    @Scheduled(fixedDelay = 120000) // 2 minutos
    @Transactional
    public void processEmailQueue() {
        if (pendingEmails.isEmpty()) {
            return;
        }
        
        logger.info("Procesando cola de emails institucionales. Emails pendientes: {}", pendingEmails.size());
        
        LocalDateTime now = LocalDateTime.now();
        List<QueuedEmail> readyToProcess = pendingEmails.values().stream()
            .filter(email -> email.getScheduledFor().isBefore(now) || email.getScheduledFor().isEqual(now))
            .sorted((a, b) -> a.getQueuedAt().compareTo(b.getQueuedAt())) // FIFO
            .limit(10) // Procesar máximo 10 por vez
            .toList();
        
        for (QueuedEmail queuedEmail : readyToProcess) {
            if (!canSendEmail()) {
                logger.info("Límite de emails alcanzado. Pausando procesamiento hasta la próxima hora.");
                break;
            }
            
            try {
                // Intentar enviar email
                boolean sent = institutionalEmailService.sendInstitutionalNotification(
                    queuedEmail.getApplication(), 
                    queuedEmail.getEmailType(),
                    queuedEmail.getTemplateData()
                ).get(); // Esperar resultado
                
                if (sent) {
                    // Email enviado exitosamente
                    pendingEmails.remove(queuedEmail.getId());
                    incrementEmailCounters();
                    
                    createQueueEvent(queuedEmail.getApplication(), 
                        "Email institucional enviado desde cola: " + queuedEmail.getEmailType());
                    
                    logger.info("Email {} enviado exitosamente desde cola para aplicación {}", 
                               queuedEmail.getEmailType(), queuedEmail.getApplication().getId());
                    
                } else {
                    // Error al enviar, reintentar
                    handleEmailFailure(queuedEmail);
                }
                
                // Pausa entre emails para no sobrecargar
                Thread.sleep(2000); // 2 segundos entre emails
                
            } catch (Exception e) {
                logger.error("Error procesando email desde cola: {}", e.getMessage(), e);
                handleEmailFailure(queuedEmail);
            }
        }
        
        // Limpiar emails vencidos (más de 24 horas)
        cleanupExpiredEmails();
    }

    private void handleEmailFailure(QueuedEmail queuedEmail) {
        if (queuedEmail.shouldRetry()) {
            queuedEmail.incrementRetry();
            createQueueEvent(queuedEmail.getApplication(), 
                String.format("Error enviando email. Reintento #%d programado para %s", 
                    queuedEmail.getRetryCount(), queuedEmail.getScheduledFor()));
            
            logger.warn("Email {} falló. Reintento #{} programado para {}",
                       queuedEmail.getEmailType(), queuedEmail.getRetryCount(), queuedEmail.getScheduledFor());
        } else {
            // Máximo de reintentos alcanzado
            pendingEmails.remove(queuedEmail.getId());
            createQueueEvent(queuedEmail.getApplication(), 
                "Email falló después de múltiples intentos: " + queuedEmail.getEmailType());
            
            logger.error("Email {} falló definitivamente después de {} intentos para aplicación {}",
                        queuedEmail.getEmailType(), queuedEmail.getRetryCount(), queuedEmail.getApplication().getId());
        }
    }

    private void cleanupExpiredEmails() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<String> expiredIds = pendingEmails.values().stream()
            .filter(email -> email.getQueuedAt().isBefore(cutoff))
            .map(QueuedEmail::getId)
            .toList();
        
        for (String id : expiredIds) {
            QueuedEmail expired = pendingEmails.remove(id);
            if (expired != null) {
                createQueueEvent(expired.getApplication(), 
                    "Email expirado removido de cola: " + expired.getEmailType());
                logger.warn("Email expirado removido de cola: {} para aplicación {}", 
                           expired.getEmailType(), expired.getApplication().getId());
            }
        }
    }

    /**
     * Control de límites institucionales
     */
    private boolean canQueueMoreEmails() {
        return pendingEmails.size() < 100; // Máximo 100 emails en cola
    }

    private boolean canSendEmail() {
        return emailsSentThisHour.get() < MAX_EMAILS_PER_HOUR &&
               emailsSentThisDay.get() < MAX_EMAILS_PER_DAY &&
               emailsSentThisMonth.get() < MAX_EMAILS_PER_MONTH;
    }

    private LocalDateTime getNextAvailableSlot() {
        if (emailsSentThisHour.get() >= MAX_EMAILS_PER_HOUR) {
            return LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1);
        }
        return LocalDateTime.now().plusMinutes(10);
    }

    private void incrementEmailCounters() {
        emailsSentThisHour.incrementAndGet();
        emailsSentThisDay.incrementAndGet();
        emailsSentThisMonth.incrementAndGet();
    }

    /**
     * Reset de contadores cada hora
     */
    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    public void resetHourlyCounter() {
        int sent = emailsSentThisHour.getAndSet(0);
        logger.info("Reset contador horario. Emails enviados en la última hora: {}", sent);
    }

    /**
     * Reset de contadores cada día a medianoche
     */
    @Scheduled(cron = "0 0 0 * * *") // Cada medianoche
    public void resetDailyCounter() {
        int sent = emailsSentThisDay.getAndSet(0);
        logger.info("Reset contador diario. Emails enviados en el último día: {}", sent);
    }

    /**
     * Reset de contadores cada mes
     */
    @Scheduled(cron = "0 0 0 1 * *") // Primer día de cada mes
    public void resetMonthlyCounter() {
        int sent = emailsSentThisMonth.getAndSet(0);
        logger.info("Reset contador mensual. Emails enviados en el último mes: {}", sent);
    }

    private void createQueueEvent(Application application, String description) {
        try {
            // Buscar si ya existe una notificación para crear el evento
            List<EmailNotification> notifications = emailNotificationRepository.findByApplication(application);
            if (!notifications.isEmpty()) {
                EmailNotification notification = notifications.get(notifications.size() - 1); // Usar la más reciente
                
                EmailEvent event = new EmailEvent();
                event.setEmailNotification(notification);
                event.setEventType(EmailEvent.EventType.QUEUED);
                event.setDescription(description);
                event.setEventDate(LocalDateTime.now());
                emailEventRepository.save(event);
            }
        } catch (Exception e) {
            logger.warn("No se pudo crear evento de queue para aplicación {}: {}", application.getId(), e.getMessage());
        }
    }

    /**
     * Métodos públicos para usar desde otros servicios
     */
    public String queueApplicationReceivedEmail(Application application) {
        return queueInstitutionalEmail(application, EmailNotification.EmailType.APPLICATION_RECEIVED, new HashMap<>());
    }

    public String queueInterviewInvitationEmail(Application application, Interview interview) {
        Map<String, Object> data = new HashMap<>();
        data.put("fecha", interview.getInterviewDate().toString());
        data.put("hora", interview.getInterviewTime() != null ? interview.getInterviewTime().toString() : "Por confirmar");
        return queueInstitutionalEmail(application, EmailNotification.EmailType.INTERVIEW_INVITATION, data);
    }

    public String queueStatusUpdateEmail(Application application, String newStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("nuevoEstado", newStatus);
        return queueInstitutionalEmail(application, EmailNotification.EmailType.APPLICATION_STATUS_UPDATE, data);
    }

    public String queueDocumentReminderEmail(Application application, String pendingDocuments) {
        Map<String, Object> data = new HashMap<>();
        data.put("documentosPendientes", pendingDocuments);
        return queueInstitutionalEmail(application, EmailNotification.EmailType.DOCUMENT_REMINDER, data);
    }

    public String queueAdmissionResultEmail(Application application, String result, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("resultado", result);
        data.put("mensaje", message);
        return queueInstitutionalEmail(application, EmailNotification.EmailType.ADMISSION_RESULT, data);
    }

    /**
     * Estadísticas de la cola
     */
    public Map<String, Object> getQueueStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingEmails", pendingEmails.size());
        stats.put("emailsSentThisHour", emailsSentThisHour.get());
        stats.put("emailsSentThisDay", emailsSentThisDay.get());
        stats.put("emailsSentThisMonth", emailsSentThisMonth.get());
        stats.put("maxEmailsPerHour", MAX_EMAILS_PER_HOUR);
        stats.put("maxEmailsPerDay", MAX_EMAILS_PER_DAY);
        stats.put("maxEmailsPerMonth", MAX_EMAILS_PER_MONTH);
        
        // Detalles de la cola
        Map<String, Integer> queueByType = new HashMap<>();
        pendingEmails.values().forEach(email -> {
            String type = email.getEmailType().name();
            queueByType.put(type, queueByType.getOrDefault(type, 0) + 1);
        });
        stats.put("queueByType", queueByType);
        
        return stats;
    }

    /**
     * Limpiar cola (para testing o emergencias)
     */
    public void clearQueue() {
        logger.warn("Limpiando cola de emails institucionales. Emails perdidos: {}", pendingEmails.size());
        pendingEmails.clear();
    }

    /**
     * Forzar procesamiento inmediato (para testing)
     */
    public void forceProcessQueue() {
        logger.info("Forzando procesamiento inmediato de cola");
        processEmailQueue();
    }
}