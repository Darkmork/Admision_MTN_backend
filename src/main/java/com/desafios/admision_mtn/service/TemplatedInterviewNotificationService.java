package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.EmailTemplate;
import com.desafios.admision_mtn.entity.EmailTemplate.TemplateCategory;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.EmailNotification;
import com.desafios.admision_mtn.entity.EmailNotification.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatedInterviewNotificationService {

    private final EmailTemplateService templateService;
    private final InstitutionalEmailService institutionalEmailService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("es-CL"));

    /**
     * Enviar notificación cuando se asigna una entrevista usando templates
     */
    @Async
    public void sendInterviewAssignmentNotification(Interview interview) {
        try {
            log.info("Enviando notificación templated de asignación de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            EmailTemplate template = templateService.getTemplateByKey("INTERVIEW_ASSIGNMENT", TemplateCategory.INTERVIEW_ASSIGNMENT);
            Map<String, Object> variables = buildInterviewVariables(interview);
            
            sendTemplatedNotification(
                interview.getApplication().getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.INTERVIEW_ASSIGNMENT
            );
            
            log.info("Notificación de asignación de entrevista enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de asignación de entrevista: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación cuando se programa un set completo de 3 entrevistas
     */
    @Async
    public void sendCompleteInterviewSetNotification(Long applicationId, List<Interview> interviews) {
        try {
            log.info("Enviando notificación de set completo de entrevistas para aplicación ID: {}", applicationId);
            
            if (interviews.size() != 3) {
                log.warn("Se esperaban 3 entrevistas pero se recibieron: {}", interviews.size());
            }
            
            EmailTemplate template = templateService.getTemplateByKey("INTERVIEW_COMPLETE_SET", TemplateCategory.INTERVIEW_ASSIGNMENT);
            Map<String, Object> variables = buildCompleteSetVariables(interviews);
            
            String recipientEmail = interviews.get(0).getApplication().getApplicantUser().getEmail();
            
            sendTemplatedNotification(
                recipientEmail,
                template,
                variables,
                EmailType.INTERVIEW_COMPLETE_SET
            );
            
            log.info("Notificación de set completo de entrevistas enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de set completo de entrevistas: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación de confirmación de entrevista
     */
    @Async
    public void sendInterviewConfirmationNotification(Interview interview) {
        try {
            log.info("Enviando notificación de confirmación de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            EmailTemplate template = templateService.getTemplateByKey("INTERVIEW_CONFIRMATION", TemplateCategory.INTERVIEW_CONFIRMATION);
            Map<String, Object> variables = buildInterviewVariables(interview);
            
            sendTemplatedNotification(
                interview.getApplication().getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.INTERVIEW_CONFIRMATION
            );
            
            log.info("Notificación de confirmación de entrevista enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de confirmación de entrevista: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar recordatorio de entrevista próxima
     */
    @Async
    public void sendInterviewReminderNotification(Interview interview) {
        try {
            log.info("Enviando recordatorio de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            EmailTemplate template = templateService.getTemplateByKey("INTERVIEW_REMINDER", TemplateCategory.INTERVIEW_REMINDER);
            Map<String, Object> variables = buildInterviewVariables(interview);
            
            sendTemplatedNotification(
                interview.getApplication().getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.INTERVIEW_REMINDER
            );
            
            log.info("Recordatorio de entrevista enviado exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando recordatorio de entrevista: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación de reprogramación de entrevista
     */
    @Async
    public void sendInterviewRescheduleNotification(Interview interview, String reason) {
        try {
            log.info("Enviando notificación de reprogramación de entrevista para aplicación ID: {}", interview.getApplication().getId());
            
            EmailTemplate template = templateService.getTemplateByKey("INTERVIEW_RESCHEDULE", TemplateCategory.INTERVIEW_RESCHEDULE);
            Map<String, Object> variables = buildInterviewVariables(interview);
            variables.put("rescheduleReason", reason != null ? reason : "Cambio de horario por necesidades del colegio");
            
            sendTemplatedNotification(
                interview.getApplication().getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.INTERVIEW_RESCHEDULE
            );
            
            log.info("Notificación de reprogramación de entrevista enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de reprogramación de entrevista: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación de selección de estudiante
     */
    @Async
    public void sendStudentSelectionNotification(Application application) {
        try {
            log.info("Enviando notificación de selección de estudiante para aplicación ID: {}", application.getId());
            
            EmailTemplate template = templateService.getTemplateByKey("STUDENT_SELECTION", TemplateCategory.STUDENT_SELECTION);
            Map<String, Object> variables = buildApplicationVariables(application);
            
            sendTemplatedNotification(
                application.getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.STUDENT_SELECTION
            );
            
            log.info("Notificación de selección de estudiante enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de selección de estudiante: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación de rechazo de estudiante
     */
    @Async
    public void sendStudentRejectionNotification(Application application, String reason) {
        try {
            log.info("Enviando notificación de rechazo de estudiante para aplicación ID: {}", application.getId());
            
            EmailTemplate template = templateService.getTemplateByKey("STUDENT_REJECTION", TemplateCategory.STUDENT_REJECTION);
            Map<String, Object> variables = buildApplicationVariables(application);
            variables.put("rejectionReason", reason != null ? reason : "No cumple con los criterios de admisión");
            
            sendTemplatedNotification(
                application.getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.STUDENT_REJECTION
            );
            
            log.info("Notificación de rechazo de estudiante enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de rechazo de estudiante: {}", e.getMessage(), e);
        }
    }

    /**
     * Enviar notificación de resultados de admisión
     */
    @Async
    public void sendAdmissionResultsNotification(Application application, String result, String additionalInfo) {
        try {
            log.info("Enviando notificación de resultados de admisión para aplicación ID: {}", application.getId());
            
            EmailTemplate template = templateService.getTemplateByKey("ADMISSION_RESULTS", TemplateCategory.ADMISSION_RESULTS);
            Map<String, Object> variables = buildApplicationVariables(application);
            variables.put("admissionResult", result);
            variables.put("additionalInfo", additionalInfo != null ? additionalInfo : "");
            
            sendTemplatedNotification(
                application.getApplicantUser().getEmail(),
                template,
                variables,
                EmailType.ADMISSION_RESULTS
            );
            
            log.info("Notificación de resultados de admisión enviada exitosamente");
            
        } catch (Exception e) {
            log.error("Error enviando notificación de resultados de admisión: {}", e.getMessage(), e);
        }
    }

    // Métodos privados de utilidad

    private void sendTemplatedNotification(String recipientEmail, EmailTemplate template, Map<String, Object> variables, EmailType emailType) {
        try {
            String processedSubject = templateService.processSubject(template, variables);
            String processedContent = templateService.processTemplate(template, variables);
            
            institutionalEmailService.createAndQueueEmail(
                recipientEmail,
                processedSubject,
                processedContent,
                emailType
            );
            
            log.debug("Notificación templated enviada a la cola institucional: {} -> {}", emailType, recipientEmail);
            
        } catch (Exception e) {
            log.error("Error enviando notificación templated: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando notificación con template", e);
        }
    }

    private Map<String, Object> buildInterviewVariables(Interview interview) {
        Map<String, Object> variables = new HashMap<>();
        Application application = interview.getApplication();
        
        // Variables del estudiante
        variables.put("studentName", interview.getStudentName());
        variables.put("studentFirstName", application.getStudent().getFirstName());
        variables.put("studentLastName", application.getStudent().getLastName());
        variables.put("gradeApplied", interview.getGradeApplied());
        
        // Variables de los padres
        variables.put("parentNames", interview.getParentNames());
        variables.put("applicantName", application.getApplicantUser().getFirstName() + " " + application.getApplicantUser().getLastName());
        variables.put("applicantEmail", application.getApplicantUser().getEmail());
        
        // Variables de la entrevista
        variables.put("interviewType", getInterviewTypeDisplayName(interview.getType()));
        variables.put("interviewMode", getInterviewModeDisplayName(interview.getMode()));
        variables.put("interviewDate", interview.getScheduledDate().format(DATE_FORMATTER));
        variables.put("interviewTime", interview.getScheduledTime().format(TIME_FORMATTER));
        variables.put("interviewDuration", interview.getDuration() + " minutos");
        variables.put("interviewLocation", interview.getLocation() != null ? interview.getLocation() : "Por definir");
        variables.put("interviewerName", interview.getInterviewerName());
        variables.put("meetingLink", interview.getVirtualMeetingLink() != null ? interview.getVirtualMeetingLink() : "");
        variables.put("interviewNotes", interview.getNotes() != null ? interview.getNotes() : "");
        
        // Variables del colegio
        variables.put("collegeName", "Colegio Monte Tabor y Nazaret");
        variables.put("collegePhone", "+56 2 2345 6789");
        variables.put("collegeEmail", "admision@mtn.cl");
        variables.put("collegeAddress", "Dirección del Colegio");
        
        // Variables de fechas
        variables.put("currentDate", java.time.LocalDate.now().format(DATE_FORMATTER));
        variables.put("currentYear", String.valueOf(java.time.LocalDate.now().getYear()));
        
        return variables;
    }

    private Map<String, Object> buildCompleteSetVariables(List<Interview> interviews) {
        Map<String, Object> variables = new HashMap<>();
        
        if (interviews.isEmpty()) {
            return variables;
        }
        
        Interview firstInterview = interviews.get(0);
        Application application = firstInterview.getApplication();
        
        // Variables básicas del estudiante
        variables.putAll(buildInterviewVariables(firstInterview));
        
        // Variables específicas del set completo
        variables.put("totalInterviews", interviews.size());
        
        // Crear detalles de cada entrevista
        StringBuilder interviewDetails = new StringBuilder();
        for (int i = 0; i < interviews.size(); i++) {
            Interview interview = interviews.get(i);
            interviewDetails.append(String.format(
                "%d. %s - %s a las %s (%s) con %s<br>",
                i + 1,
                getInterviewTypeDisplayName(interview.getType()),
                interview.getScheduledDate().format(DATE_FORMATTER),
                interview.getScheduledTime().format(TIME_FORMATTER),
                interview.getLocation(),
                interview.getInterviewerName()
            ));
        }
        
        variables.put("interviewList", interviewDetails.toString());
        variables.put("allInterviewsScheduled", true);
        
        return variables;
    }

    private Map<String, Object> buildApplicationVariables(Application application) {
        Map<String, Object> variables = new HashMap<>();
        
        // Variables del estudiante
        variables.put("studentName", application.getStudent().getFirstName() + " " + application.getStudent().getLastName());
        variables.put("studentFirstName", application.getStudent().getFirstName());
        variables.put("studentLastName", application.getStudent().getLastName());
        variables.put("gradeApplied", application.getStudent().getGradeApplied());
        
        // Variables del apoderado
        variables.put("applicantName", application.getApplicantUser().getFirstName() + " " + application.getApplicantUser().getLastName());
        variables.put("applicantEmail", application.getApplicantUser().getEmail());
        
        // Variables del colegio
        variables.put("collegeName", "Colegio Monte Tabor y Nazaret");
        variables.put("collegePhone", "+56 2 2345 6789");
        variables.put("collegeEmail", "admision@mtn.cl");
        
        // Variables de fechas
        variables.put("currentDate", java.time.LocalDate.now().format(DATE_FORMATTER));
        variables.put("currentYear", String.valueOf(java.time.LocalDate.now().getYear()));
        
        return variables;
    }

    private String getInterviewTypeDisplayName(Interview.InterviewType type) {
        switch (type) {
            case PSYCHOLOGICAL: return "Evaluación Psicológica";
            case FAMILY: return "Entrevista Familiar";
            case INDIVIDUAL: return "Entrevista Individual";
            case ACADEMIC: return "Evaluación Académica";
            case BEHAVIORAL: return "Evaluación Conductual";
            default: return type.name();
        }
    }

    private String getInterviewModeDisplayName(Interview.InterviewMode mode) {
        switch (mode) {
            case IN_PERSON: return "Presencial";
            case VIRTUAL: return "Virtual";
            case HYBRID: return "Híbrida";
            default: return mode.name();
        }
    }
}