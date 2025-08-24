package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Interview;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de notificaciones automatizadas del sistema de admisiones
 * 
 * TIPOS DE NOTIFICACIONES:
 * - 📧 Cambios de estado de aplicaciones
 * - 📅 Recordatorios de entrevistas
 * - 📝 Asignación de evaluaciones
 * - ✅ Completación de evaluaciones
 * - 🎯 Decisiones de admisión
 * - ⚠️ Documentos faltantes
 * - 🔄 Recordatorios de seguimiento
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final EmailService emailService;
    private final UserRepository userRepository;
    
    @Value("${app.institutional-email.from-name:Colegio Monte Tabor y Nazaret}")
    private String institutionName;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ================================
    // NOTIFICACIONES DE ESTADO DE APLICACIÓN
    // ================================
    
    /**
     * Notifica cuando una aplicación cambia de estado
     */
    @Async
    public CompletableFuture<Void> notifyApplicationStatusChange(
            Application application, 
            Application.ApplicationStatus fromStatus, 
            Application.ApplicationStatus toStatus) {
        
        try {
            String applicantEmail = application.getApplicantUser().getEmail();
            String studentName = getStudentFullName(application);
            
            String subject = buildStatusChangeSubject(toStatus, studentName);
            String body = buildStatusChangeBody(application, fromStatus, toStatus, studentName);
            
            emailService.sendSimpleMessage(applicantEmail, subject, body);
            
            log.info("📧 Notificación de cambio de estado enviada: {} → {} para aplicación {}", 
                    fromStatus, toStatus, application.getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de cambio de estado para aplicación {}", 
                    application.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Notifica documentos faltantes
     */
    @Async
    public CompletableFuture<Void> notifyMissingDocuments(Application application, List<String> missingDocuments) {
        try {
            String applicantEmail = application.getApplicantUser().getEmail();
            String studentName = getStudentFullName(application);
            
            String subject = "📄 Documentos Faltantes - " + studentName;
            String body = buildMissingDocumentsBody(application, studentName, missingDocuments);
            
            emailService.sendSimpleMessage(applicantEmail, subject, body);
            
            log.info("📄 Notificación de documentos faltantes enviada para aplicación {}", 
                    application.getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de documentos faltantes para aplicación {}", 
                    application.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ================================
    // NOTIFICACIONES DE ENTREVISTAS
    // ================================
    
    /**
     * Notifica cuando se programa una entrevista
     */
    @Async
    public CompletableFuture<Void> notifyInterviewScheduled(Interview interview) {
        try {
            // Notificar al apoderado
            String applicantEmail = interview.getApplication().getApplicantUser().getEmail();
            String studentName = getStudentFullName(interview.getApplication());
            
            String subjectFamily = "📅 Entrevista Programada - " + studentName;
            String bodyFamily = buildInterviewScheduledBodyForFamily(interview, studentName);
            
            emailService.sendSimpleMessage(applicantEmail, subjectFamily, bodyFamily);
            
            // Notificar al entrevistador
            if (interview.getInterviewer() != null) {
                String interviewerEmail = interview.getInterviewer().getEmail();
                String subjectInterviewer = "📅 Nueva Entrevista Asignada - " + studentName;
                String bodyInterviewer = buildInterviewScheduledBodyForInterviewer(interview, studentName);
                
                emailService.sendSimpleMessage(interviewerEmail, subjectInterviewer, bodyInterviewer);
            }
            
            log.info("📅 Notificaciones de entrevista programada enviadas para aplicación {}", 
                    interview.getApplication().getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificaciones de entrevista programada {}", 
                    interview.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Recordatorio de entrevista (24 horas antes)
     */
    @Async
    public CompletableFuture<Void> sendInterviewReminder(Interview interview) {
        try {
            String applicantEmail = interview.getApplication().getApplicantUser().getEmail();
            String studentName = getStudentFullName(interview.getApplication());
            
            String subject = "🔔 Recordatorio: Entrevista Mañana - " + studentName;
            String body = buildInterviewReminderBody(interview, studentName);
            
            emailService.sendSimpleMessage(applicantEmail, subject, body);
            
            log.info("🔔 Recordatorio de entrevista enviado para aplicación {}", 
                    interview.getApplication().getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando recordatorio de entrevista {}", interview.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ================================
    // NOTIFICACIONES DE EVALUACIONES
    // ================================
    
    /**
     * Notifica cuando se asigna una evaluación a un profesor
     */
    @Async
    public CompletableFuture<Void> notifyEvaluationAssigned(Evaluation evaluation) {
        try {
            String evaluatorEmail = evaluation.getEvaluator().getEmail();
            String studentName = getStudentFullName(evaluation.getApplication());
            
            String subject = "📝 Nueva Evaluación Asignada - " + studentName;
            String body = buildEvaluationAssignedBody(evaluation, studentName);
            
            emailService.sendSimpleMessage(evaluatorEmail, subject, body);
            
            log.info("📝 Notificación de evaluación asignada enviada a {}", evaluatorEmail);
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de evaluación asignada {}", 
                    evaluation.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Notifica cuando se completa una evaluación
     */
    @Async
    public CompletableFuture<Void> notifyEvaluationCompleted(Evaluation evaluation) {
        try {
            // Notificar al coordinador académico
            List<User> coordinators = userRepository.findByRole(User.UserRole.COORDINATOR);
            
            for (User coordinator : coordinators) {
                String studentName = getStudentFullName(evaluation.getApplication());
                String subject = "✅ Evaluación Completada - " + studentName;
                String body = buildEvaluationCompletedBody(evaluation, studentName);
                
                emailService.sendSimpleMessage(coordinator.getEmail(), subject, body);
            }
            
            log.info("✅ Notificaciones de evaluación completada enviadas para evaluación {}", 
                    evaluation.getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificaciones de evaluación completada {}", 
                    evaluation.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ================================
    // NOTIFICACIONES DE DECISIÓN FINAL
    // ================================
    
    /**
     * Notifica la decisión final de admisión
     */
    @Async
    public CompletableFuture<Void> notifyAdmissionDecision(Application application) {
        try {
            String applicantEmail = application.getApplicantUser().getEmail();
            String studentName = getStudentFullName(application);
            
            String subject = buildAdmissionDecisionSubject(application.getStatus(), studentName);
            String body = buildAdmissionDecisionBody(application, studentName);
            
            emailService.sendSimpleMessage(applicantEmail, subject, body);
            
            log.info("🎯 Notificación de decisión de admisión enviada para aplicación {}", 
                    application.getId());
                    
        } catch (Exception e) {
            log.error("❌ Error enviando notificación de decisión de admisión para aplicación {}", 
                    application.getId(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================
    
    private String getStudentFullName(Application application) {
        if (application.getStudent() == null) {
            return "Estudiante";
        }
        return String.format("%s %s %s", 
                application.getStudent().getFirstName() != null ? application.getStudent().getFirstName() : "",
                application.getStudent().getLastName() != null ? application.getStudent().getLastName() : "",
                application.getStudent().getMaternalLastName() != null ? application.getStudent().getMaternalLastName() : ""
        ).trim().replaceAll("\\s+", " ");
    }
    
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy"));
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm"));
    }

    // ================================
    // TEMPLATES DE EMAILS
    // ================================
    
    private String buildStatusChangeSubject(Application.ApplicationStatus status, String studentName) {
        return switch (status) {
            case UNDER_REVIEW -> "🔄 Su postulación está en revisión - " + studentName;
            case INTERVIEW_SCHEDULED -> "📅 Entrevista programada - " + studentName;
            case EXAM_SCHEDULED -> "📝 Evaluación programada - " + studentName;
            case APPROVED -> "🎉 ¡Felicitaciones! Postulación aprobada - " + studentName;
            case REJECTED -> "📋 Resultado de postulación - " + studentName;
            case WAITLIST -> "⏳ Su postulación está en lista de espera - " + studentName;
            case DOCUMENTS_REQUESTED -> "📄 Documentos adicionales requeridos - " + studentName;
            default -> "📋 Actualización de postulación - " + studentName;
        };
    }
    
    private String buildStatusChangeBody(Application application, 
                                       Application.ApplicationStatus fromStatus,
                                       Application.ApplicationStatus toStatus, 
                                       String studentName) {
        String baseMessage = """
            Estimado/a apoderado/a,
            
            Le informamos que el estado de la postulación de %s ha cambiado:
            
            Estado anterior: %s
            Estado actual: %s
            Fecha de actualización: %s
            
            """.formatted(studentName, getStatusDisplayName(fromStatus), 
                         getStatusDisplayName(toStatus), formatDateTime(LocalDateTime.now()));
        
        String statusSpecificMessage = switch (toStatus) {
            case UNDER_REVIEW -> """
                Su postulación está ahora en revisión por nuestro equipo académico. 
                Verificaremos que todos los documentos estén completos y procederemos 
                con el siguiente paso del proceso.
                
                Tiempo estimado: 2-3 días hábiles.
                """;
                
            case INTERVIEW_SCHEDULED -> """
                Se ha programado una entrevista para su hijo/a. Pronto recibirá 
                un email separado con los detalles de fecha, hora y modalidad.
                
                Por favor, esté atento/a a su correo electrónico.
                """;
                
            case EXAM_SCHEDULED -> """
                Su hijo/a ha sido programado/a para las evaluaciones académicas.
                Recibirá información detallada sobre las fechas y modalidades 
                de las evaluaciones próximamente.
                """;
                
            case APPROVED -> """
                ¡Felicitaciones! Nos complace informarle que su hijo/a ha sido 
                ACEPTADO/A en nuestro colegio.
                
                Próximos pasos:
                1. Recibirá información sobre matrícula en los próximos días
                2. Complete el proceso de matrícula dentro del plazo establecido
                3. Participe en las actividades de bienvenida
                
                ¡Bienvenidos a la familia Monte Tabor & Nazaret!
                """;
                
            case REJECTED -> """
                Después de una cuidadosa evaluación, lamentamos informarle que 
                en esta ocasión no podemos ofrecer un cupo para su hijo/a.
                
                Esta decisión no refleja las capacidades de su hijo/a, sino 
                las limitaciones de cupos disponibles y los criterios específicos 
                de admisión para este período.
                
                Le deseamos lo mejor en su búsqueda educacional.
                """;
                
            case WAITLIST -> """
                Su postulación ha sido incluida en nuestra lista de espera.
                Esto significa que su hijo/a cumple con nuestros requisitos,
                pero actualmente no tenemos cupos disponibles.
                
                Le mantendremos informado/a si se abre un cupo disponible.
                Su posición en la lista de espera se basa en el orden de
                evaluación y criterios académicos.
                """;
                
            case DOCUMENTS_REQUESTED -> """
                Necesitamos documentos adicionales para continuar con el proceso
                de evaluación. Recibirá un email detallando los documentos
                específicos requeridos.
                
                Por favor, proporcione los documentos solicitados a la brevedad
                para evitar retrasos en el proceso.
                """;
                
            default -> "Manténgase atento/a a futuras comunicaciones.";
        };
        
        String footer = """
            
            Para consultas o dudas:
            📧 Email: admisiones@mtn.cl
            📞 Teléfono: +56 2 2234 5678
            🌐 Web: www.mtn.cl
            
            Puede revisar el estado actualizado de su postulación en nuestro 
            sistema en línea: %s
            
            Atentamente,
            Equipo de Admisiones
            %s
            """.formatted(baseUrl, institutionName);
            
        return baseMessage + statusSpecificMessage + footer;
    }
    
    private String buildMissingDocumentsBody(Application application, String studentName, List<String> missingDocuments) {
        StringBuilder docsBuilder = new StringBuilder();
        for (String doc : missingDocuments) {
            docsBuilder.append("• ").append(getDocumentDisplayName(doc)).append("\n");
        }
        
        return """
            Estimado/a apoderado/a,
            
            Para continuar con el proceso de postulación de %s, necesitamos 
            que nos proporcione los siguientes documentos:
            
            DOCUMENTOS FALTANTES:
            %s
            
            INSTRUCCIONES:
            1. Acceda a nuestro sistema en línea: %s
            2. Vaya a la sección "Documentos"
            3. Suba los documentos solicitados en formato PDF
            4. Asegúrese de que los archivos sean legibles y completos
            
            IMPORTANTE:
            • Los documentos deben estar en formato PDF
            • Tamaño máximo por archivo: 10MB
            • Asegúrese de que la información sea legible
            
            Si tiene dificultades técnicas o preguntas sobre los documentos,
            no dude en contactarnos.
            
            📧 Email: admisiones@mtn.cl
            📞 Teléfono: +56 2 2234 5678
            
            Atentamente,
            Equipo de Admisiones
            %s
            """.formatted(studentName, docsBuilder.toString(), baseUrl, institutionName);
    }
    
    private String buildInterviewScheduledBodyForFamily(Interview interview, String studentName) {
        return """
            Estimado/a apoderado/a,
            
            Nos complace informarle que se ha programado una entrevista para %s 
            como parte del proceso de admisión.
            
            DETALLES DE LA ENTREVISTA:
            📅 Fecha: %s
            🕐 Hora: %s
            👥 Tipo: %s
            📍 Modalidad: %s
            👨‍🏫 Entrevistador: %s
            
            INSTRUCCIONES:
            • Confirme su asistencia respondiendo a este email
            • Llegue 10 minutos antes de la hora programada
            • Traiga un documento de identidad
            • La entrevista durará aproximadamente 30-45 minutos
            
            Si necesita reprogramar por motivos justificados, contacte a:
            📧 admisiones@mtn.cl
            📞 +56 2 2234 5678
            
            Atentamente,
            Equipo de Admisiones
            %s
            """.formatted(
                studentName,
                formatDate(interview.getScheduledDate()),
                interview.getScheduledTime() != null ? interview.getScheduledTime().toString() : "Por confirmar",
                getInterviewTypeDisplayName(interview.getType()),
                getInterviewModeDisplayName(interview.getMode()),
                interview.getInterviewer() != null ? 
                    interview.getInterviewer().getFirstName() + " " + interview.getInterviewer().getLastName() 
                    : "Por asignar",
                institutionName
            );
    }
    
    private String buildInterviewScheduledBodyForInterviewer(Interview interview, String studentName) {
        return """
            Estimado/a colega,
            
            Se le ha asignado una nueva entrevista en el sistema de admisiones.
            
            DETALLES DE LA ENTREVISTA:
            👥 Estudiante: %s
            📅 Fecha: %s
            🕐 Hora: %s
            📍 Modalidad: %s
            📝 Tipo: %s
            
            INFORMACIÓN ADICIONAL:
            • Acceda al sistema para ver más detalles del estudiante
            • Complete la evaluación al finalizar la entrevista
            • Registre observaciones y recomendaciones
            
            Sistema de admisiones: %s
            
            Si tiene conflictos de horario, contacte inmediatamente a:
            📧 coordinacion@mtn.cl
            
            Saludos cordiales,
            Sistema de Admisiones
            %s
            """.formatted(
                studentName,
                formatDate(interview.getScheduledDate()),
                interview.getScheduledTime() != null ? interview.getScheduledTime().toString() : "Por confirmar",
                getInterviewModeDisplayName(interview.getMode()),
                getInterviewTypeDisplayName(interview.getType()),
                baseUrl,
                institutionName
            );
    }
    
    private String buildInterviewReminderBody(Interview interview, String studentName) {
        return """
            Estimado/a apoderado/a,
            
            Le recordamos que mañana tiene programada la entrevista de %s.
            
            DETALLES:
            📅 Fecha: %s (MAÑANA)
            🕐 Hora: %s
            📍 Modalidad: %s
            
            RECORDATORIOS:
            ✅ Confirme que tiene la fecha y hora correctas
            ✅ Llegue 10 minutos antes
            ✅ Traiga documento de identidad
            ✅ Prepare cualquier pregunta que desee hacer
            
            Si necesita reprogramar por emergencia, contacte inmediatamente a:
            📞 +56 2 2234 5678
            📧 admisiones@mtn.cl
            
            ¡Los esperamos!
            
            Equipo de Admisiones
            %s
            """.formatted(
                studentName,
                formatDate(interview.getScheduledDate()),
                interview.getScheduledTime() != null ? interview.getScheduledTime().toString() : "Por confirmar",
                getInterviewModeDisplayName(interview.getMode()),
                institutionName
            );
    }
    
    private String buildEvaluationAssignedBody(Evaluation evaluation, String studentName) {
        return """
            Estimado/a profesor/a,
            
            Se le ha asignado una nueva evaluación en el sistema de admisiones.
            
            DETALLES DE LA EVALUACIÓN:
            👥 Estudiante: %s
            📝 Tipo de evaluación: %s
            📅 Fecha de asignación: %s
            
            ACCIONES REQUERIDAS:
            1. Acceda al sistema de admisiones: %s
            2. Revise la información del estudiante
            3. Complete la evaluación según los criterios establecidos
            4. Registre sus observaciones y calificación
            
            IMPORTANTE:
            • Complete la evaluación dentro de los próximos 5 días hábiles
            • Use los criterios de evaluación institucionales
            • Registre observaciones detalladas y constructivas
            
            Si tiene preguntas sobre la evaluación, contacte a:
            📧 coordinacion@mtn.cl
            
            Saludos cordiales,
            Sistema de Admisiones
            %s
            """.formatted(
                studentName,
                getEvaluationTypeDisplayName(evaluation.getEvaluationType()),
                formatDateTime(LocalDateTime.now()),
                baseUrl,
                institutionName
            );
    }
    
    private String buildEvaluationCompletedBody(Evaluation evaluation, String studentName) {
        return """
            Estimado/a coordinador/a,
            
            Se ha completado una evaluación en el sistema de admisiones.
            
            DETALLES:
            👥 Estudiante: %s
            📝 Tipo de evaluación: %s
            👨‍🏫 Evaluador: %s
            ⭐ Calificación: %s
            📅 Fecha de completación: %s
            
            La evaluación está disponible para revisión en el sistema.
            
            Sistema de admisiones: %s
            
            Saludos cordiales,
            Sistema de Admisiones
            %s
            """.formatted(
                studentName,
                getEvaluationTypeDisplayName(evaluation.getEvaluationType()),
                evaluation.getEvaluator().getFirstName() + " " + evaluation.getEvaluator().getLastName(),
                evaluation.getScore() != null ? evaluation.getScore().toString() : "No calificada",
                formatDateTime(LocalDateTime.now()),
                baseUrl,
                institutionName
            );
    }
    
    private String buildAdmissionDecisionSubject(Application.ApplicationStatus status, String studentName) {
        return switch (status) {
            case APPROVED -> "🎉 ¡Felicitaciones! " + studentName + " ha sido aceptado/a";
            case REJECTED -> "📋 Resultado del proceso de admisión - " + studentName;
            case WAITLIST -> "⏳ Lista de espera - " + studentName;
            default -> "📋 Decisión de admisión - " + studentName;
        };
    }
    
    private String buildAdmissionDecisionBody(Application application, String studentName) {
        String baseMessage = """
            Estimado/a apoderado/a,
            
            Después de completar todo el proceso de evaluación, tenemos 
            el resultado final de la postulación de %s:
            
            DECISIÓN: %s
            Fecha de decisión: %s
            
            """.formatted(studentName, getStatusDisplayName(application.getStatus()), 
                         formatDateTime(LocalDateTime.now()));
        
        String decisionSpecificMessage = switch (application.getStatus()) {
            case APPROVED -> """
                ¡FELICITACIONES! Nos complace enormemente informarle que %s 
                ha sido ACEPTADO/A en nuestro colegio para el próximo año académico.
                
                PRÓXIMOS PASOS IMPORTANTES:
                1. 📋 Complete el proceso de matrícula (enlace será enviado)
                2. 📅 Asista a la jornada de bienvenida para nuevos estudiantes
                3. 📚 Participe en las actividades de integración
                4. 🏫 Conozca las instalaciones y profesores
                
                INFORMACIÓN IMPORTANTE:
                • Plazo para matrícula: 10 días hábiles desde esta notificación
                • Lista de útiles escolares será enviada próximamente
                • Calendario escolar y horarios disponibles en nuestro sitio web
                
                ¡Bienvenidos a la familia Monte Tabor & Nazaret! 
                Estamos emocionados de acompañar a %s en su crecimiento académico y personal.
                """.formatted(studentName, studentName);
                
            case REJECTED -> """
                Después de una evaluación exhaustiva y cuidadosa, lamentamos 
                informarle que en esta ocasión no podemos ofrecer un cupo 
                para %s en nuestro colegio.
                
                CONSIDERACIONES IMPORTANTES:
                • Esta decisión se basa en múltiples factores y limitaciones de cupos
                • No refleja las capacidades o potencial de %s
                • El proceso fue riguroso y equitativo para todos los postulantes
                
                Le agradecemos sinceramente por considerar nuestro colegio y 
                le deseamos el mayor de los éxitos en la búsqueda de la 
                institución educacional más adecuada para %s.
                """.formatted(studentName, studentName, studentName);
                
            case WAITLIST -> """
                Su postulación ha sido incluida en nuestra LISTA DE ESPERA.
                
                ¿QUÉ SIGNIFICA ESTO?
                • %s cumple con nuestros estándares académicos y de admisión
                • Actualmente no tenemos cupos disponibles en el nivel solicitado
                • Será contactado/a si se libera un cupo
                
                INFORMACIÓN IMPORTANTE:
                • Su posición se mantiene hasta [fecha límite]
                • Le notificaremos inmediatamente si hay disponibilidad
                • Puede considerar otras opciones mientras tanto
                
                Agradecemos su paciencia y comprensión. Mantendremos 
                comunicación constante sobre cualquier novedad.
                """.formatted(studentName);
                
            default -> "Manténgase atento/a a futuras comunicaciones.";
        };
        
        String footer = """
            
            Para cualquier consulta sobre esta decisión:
            📧 Email: admisiones@mtn.cl
            📞 Teléfono: +56 2 2234 5678
            🏫 Dirección: [Dirección del colegio]
            🌐 Web: www.mtn.cl
            
            Gracias por su interés en nuestro colegio.
            
            Cordialmente,
            Comité de Admisiones
            %s
            """.formatted(institutionName);
            
        return baseMessage + decisionSpecificMessage + footer;
    }

    // ================================
    // MÉTODOS DE FORMATEO DISPLAY
    // ================================
    
    private String getStatusDisplayName(Application.ApplicationStatus status) {
        return switch (status) {
            case PENDING -> "Pendiente";
            case UNDER_REVIEW -> "En Revisión";
            case INTERVIEW_SCHEDULED -> "Entrevista Programada";
            case EXAM_SCHEDULED -> "Evaluación Programada";
            case APPROVED -> "APROBADA ✅";
            case REJECTED -> "No Aprobada";
            case WAITLIST -> "Lista de Espera";
            case DOCUMENTS_REQUESTED -> "Documentos Requeridos";
            default -> status.toString();
        };
    }
    
    private String getDocumentDisplayName(String docType) {
        return switch (docType) {
            case "BIRTH_CERTIFICATE" -> "Certificado de Nacimiento";
            case "STUDENT_PHOTO" -> "Fotografía del Estudiante";
            case "ACADEMIC_RECORDS" -> "Registros Académicos";
            case "MEDICAL_CERTIFICATE" -> "Certificado Médico";
            case "PARENT_ID" -> "Cédula de Identidad del Apoderado";
            default -> docType;
        };
    }
    
    private String getInterviewTypeDisplayName(Interview.InterviewType type) {
        if (type == null) return "No especificado";
        return switch (type) {
            case INDIVIDUAL -> "Entrevista Individual";
            case FAMILY -> "Entrevista Familiar";
            case PSYCHOLOGICAL -> "Entrevista Psicológica";
            case ACADEMIC -> "Entrevista Académica";
            case BEHAVIORAL -> "Entrevista de Comportamiento";
            default -> type.toString();
        };
    }
    
    private String getInterviewModeDisplayName(Interview.InterviewMode mode) {
        if (mode == null) return "No especificado";
        return switch (mode) {
            case IN_PERSON -> "Presencial";
            case VIRTUAL -> "Virtual";
            case HYBRID -> "Híbrida";
            default -> mode.toString();
        };
    }
    
    private String getEvaluationTypeDisplayName(Evaluation.EvaluationType type) {
        if (type == null) return "No especificado";
        return switch (type) {
            case LANGUAGE_EXAM -> "Evaluación de Lenguaje";
            case MATHEMATICS_EXAM -> "Evaluación de Matemáticas";
            case ENGLISH_EXAM -> "Evaluación de Inglés";
            case CYCLE_DIRECTOR_REPORT -> "Informe Director de Ciclo";
            case CYCLE_DIRECTOR_INTERVIEW -> "Entrevista Director de Ciclo";
            case PSYCHOLOGICAL_INTERVIEW -> "Entrevista Psicológica";
            default -> type.toString();
        };
    }
}