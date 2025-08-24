package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio especializado para el workflow completo de entrevistas
 * 
 * FLUJO COMPLETO DE ENTREVISTAS:
 * 1. 📋 Planificación automática de entrevistas
 * 2. 🔄 Asignación inteligente de entrevistadores  
 * 3. 📅 Gestión de horarios y disponibilidad
 * 4. 🔔 Notificaciones automáticas y recordatorios
 * 5. 📝 Seguimiento del progreso y estados
 * 6. 📊 Reportes y métricas de entrevistas
 * 7. 🎯 Integración con decisiones de admisión
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewWorkflowService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ApplicationWorkflowService workflowService;

    // ================================
    // PLANIFICACIÓN AUTOMÁTICA
    // ================================
    
    /**
     * Planifica automáticamente entrevistas para aplicaciones que las requieren
     */
    public Map<String, Object> planifyInterviewsForPendingApplications() {
        log.info("🔄 Iniciando planificación automática de entrevistas");
        
        Map<String, Object> result = new HashMap<>();
        int interviewsCreated = 0;
        int errors = 0;
        List<String> errorMessages = new ArrayList<>();
        
        try {
            // Buscar aplicaciones que requieren entrevistas
            List<Application> applicationsNeedingInterviews = findApplicationsNeedingInterviews();
            
            log.info("📋 Encontradas {} aplicaciones que requieren entrevistas", 
                    applicationsNeedingInterviews.size());
            
            for (Application application : applicationsNeedingInterviews) {
                try {
                    // Verificar si ya tiene entrevistas programadas
                    List<Interview> existingInterviews = interviewRepository
                            .findByApplicationId(application.getId());
                    
                    if (existingInterviews.isEmpty()) {
                        // Planificar entrevistas requeridas para esta aplicación
                        planifyRequiredInterviewsForApplication(application);
                        interviewsCreated++;
                        
                        log.info("✅ Entrevistas planificadas para aplicación {}", application.getId());
                    } else {
                        log.debug("⏭️ Aplicación {} ya tiene entrevistas programadas", application.getId());
                    }
                    
                } catch (Exception e) {
                    errors++;
                    String errorMsg = "Error planificando entrevistas para aplicación " + application.getId();
                    errorMessages.add(errorMsg + ": " + e.getMessage());
                    log.error(errorMsg, e);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error en planificación automática de entrevistas", e);
            errors++;
            errorMessages.add("Error general: " + e.getMessage());
        }
        
        result.put("success", errors == 0);
        result.put("interviewsCreated", interviewsCreated);
        result.put("errors", errors);
        result.put("errorMessages", errorMessages);
        result.put("timestamp", LocalDateTime.now());
        
        log.info("✅ Planificación automática completada: {} entrevistas creadas, {} errores", 
                interviewsCreated, errors);
        
        return result;
    }
    
    /**
     * Encuentra aplicaciones que necesitan entrevistas
     */
    private List<Application> findApplicationsNeedingInterviews() {
        // Aplicaciones en estado INTERVIEW_SCHEDULED
        return applicationRepository.findByStatusOrderByCreatedAtDesc(
                Application.ApplicationStatus.INTERVIEW_SCHEDULED);
    }
    
    /**
     * Planifica las entrevistas requeridas para una aplicación específica
     */
    private void planifyRequiredInterviewsForApplication(Application application) {
        // Tipos de entrevistas requeridas según el nivel educacional del estudiante
        List<Interview.InterviewType> requiredInterviewTypes = determineRequiredInterviewTypes(application);
        
        for (Interview.InterviewType type : requiredInterviewTypes) {
            try {
                createInterviewForApplication(application, type);
                log.debug("📅 Entrevista {} creada para aplicación {}", type, application.getId());
            } catch (Exception e) {
                log.error("❌ Error creando entrevista {} para aplicación {}", 
                        type, application.getId(), e);
                throw e; // Re-lanzar para manejo en nivel superior
            }
        }
    }
    
    /**
     * Determina los tipos de entrevistas requeridas según el nivel educacional
     */
    private List<Interview.InterviewType> determineRequiredInterviewTypes(Application application) {
        List<Interview.InterviewType> required = new ArrayList<>();
        
        // Entrevista familiar es requerida para todos
        required.add(Interview.InterviewType.FAMILY);
        
        // Evaluación psicológica para todos los niveles
        required.add(Interview.InterviewType.PSYCHOLOGICAL);
        
        // Entrevista académica para nivel básico y medio
        if (application.getStudent() != null) {
            // Por ahora, agregar entrevista académica para todos
            required.add(Interview.InterviewType.ACADEMIC);
        }
        
        return required;
    }

    // ================================
    // ASIGNACIÓN INTELIGENTE
    // ================================
    
    /**
     * Crea una entrevista para una aplicación específica con asignación inteligente
     */
    private void createInterviewForApplication(Application application, Interview.InterviewType type) {
        // Buscar el mejor entrevistador disponible
        User bestInterviewer = findBestAvailableInterviewer(type, application);
        
        if (bestInterviewer == null) {
            log.warn("⚠️ No se encontró entrevistador disponible para tipo {} de aplicación {}", 
                    type, application.getId());
            // Crear entrevista sin asignar entrevistador
            bestInterviewer = getDefaultInterviewer(type);
        }
        
        // Encontrar mejor fecha disponible
        LocalDate bestDate = findBestAvailableDate(bestInterviewer, LocalDate.now().plusDays(2));
        LocalTime bestTime = findBestAvailableTime(bestInterviewer, bestDate);
        
        // Crear la entrevista
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewer(bestInterviewer);
        interview.setType(type);
        interview.setMode(determineInterviewMode(type));
        interview.setStatus(Interview.InterviewStatus.SCHEDULED);
        interview.setScheduledDate(bestDate);
        interview.setScheduledTime(bestTime);
        interview.setLocation(determineLocation(type, interview.getMode()));
        
        Interview savedInterview = interviewRepository.save(interview);
        
        // Enviar notificaciones
        try {
            notificationService.notifyInterviewScheduled(savedInterview);
        } catch (Exception e) {
            log.error("❌ Error enviando notificaciones para entrevista {}", savedInterview.getId(), e);
            // No fallar la creación por error de notificación
        }
        
        log.info("✅ Entrevista creada: ID={}, Tipo={}, Fecha={}, Entrevistador={}", 
                savedInterview.getId(), type, bestDate, bestInterviewer.getEmail());
    }
    
    /**
     * Encuentra el mejor entrevistador disponible para un tipo de entrevista
     */
    private User findBestAvailableInterviewer(Interview.InterviewType type, Application application) {
        List<User> potentialInterviewers = findInterviewersByType(type);
        
        if (potentialInterviewers.isEmpty()) {
            return null;
        }
        
        // Ordenar por carga de trabajo (menos entrevistas asignadas = mejor)
        return potentialInterviewers.stream()
                .min((u1, u2) -> {
                    long u1Load = getInterviewerWorkload(u1.getId());
                    long u2Load = getInterviewerWorkload(u2.getId());
                    return Long.compare(u1Load, u2Load);
                })
                .orElse(potentialInterviewers.get(0));
    }
    
    /**
     * Encuentra entrevistadores por tipo de entrevista
     */
    private List<User> findInterviewersByType(Interview.InterviewType type) {
        return switch (type) {
            case FAMILY -> userRepository.findByRoleAndActiveTrue(User.UserRole.CYCLE_DIRECTOR);
            case PSYCHOLOGICAL -> userRepository.findByRoleAndActiveTrue(User.UserRole.PSYCHOLOGIST);
            case ACADEMIC -> {
                List<User> academics = new ArrayList<>();
                academics.addAll(userRepository.findByRoleAndActiveTrue(User.UserRole.TEACHER));
                academics.addAll(userRepository.findByRoleAndActiveTrue(User.UserRole.COORDINATOR));
                yield academics;
            }
            case INDIVIDUAL -> userRepository.findByRoleAndActiveTrue(User.UserRole.CYCLE_DIRECTOR);
            case BEHAVIORAL -> userRepository.findByRoleAndActiveTrue(User.UserRole.PSYCHOLOGIST);
            default -> userRepository.findByRoleAndActiveTrue(User.UserRole.CYCLE_DIRECTOR);
        };
    }
    
    /**
     * Obtiene la carga de trabajo de un entrevistador
     */
    private long getInterviewerWorkload(Long interviewerId) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2); // Próximas 2 semanas
        
        return interviewRepository.findByInterviewerAndDateRange(
                interviewerId, startDate, endDate).size();
    }
    
    /**
     * Obtiene un entrevistador por defecto si no hay disponibles
     */
    private User getDefaultInterviewer(Interview.InterviewType type) {
        List<User> admins = userRepository.findByRoleAndActiveTrue(User.UserRole.ADMIN);
        return admins.isEmpty() ? null : admins.get(0);
    }

    // ================================
    // GESTIÓN DE HORARIOS
    // ================================
    
    /**
     * Encuentra la mejor fecha disponible para una entrevista
     */
    private LocalDate findBestAvailableDate(User interviewer, LocalDate startDate) {
        LocalDate currentDate = startDate;
        LocalDate maxDate = startDate.plusWeeks(4); // Máximo 4 semanas adelante
        
        while (currentDate.isBefore(maxDate)) {
            // Saltar fines de semana
            if (isWorkingDay(currentDate)) {
                // Verificar disponibilidad del entrevistador
                if (isInterviewerAvailableOnDate(interviewer.getId(), currentDate)) {
                    return currentDate;
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        
        // Si no encuentra fecha, usar la fecha de inicio
        log.warn("⚠️ No se encontró fecha disponible para entrevistador {}, usando fecha por defecto", 
                interviewer.getEmail());
        return startDate;
    }
    
    /**
     * Encuentra la mejor hora disponible en una fecha
     */
    private LocalTime findBestAvailableTime(User interviewer, LocalDate date) {
        List<LocalTime> preferredTimes = Arrays.asList(
            LocalTime.of(9, 0),   // 9:00 AM
            LocalTime.of(10, 30), // 10:30 AM
            LocalTime.of(14, 0),  // 2:00 PM
            LocalTime.of(15, 30), // 3:30 PM
            LocalTime.of(11, 0),  // 11:00 AM
            LocalTime.of(16, 0)   // 4:00 PM
        );
        
        for (LocalTime time : preferredTimes) {
            if (isTimeSlotAvailable(interviewer.getId(), date, time)) {
                return time;
            }
        }
        
        // Si no encuentra hora disponible, usar la primera preferida
        return preferredTimes.get(0);
    }
    
    /**
     * Verifica si un día es laborable
     */
    private boolean isWorkingDay(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5; // Lunes a Viernes
    }
    
    /**
     * Verifica si un entrevistador está disponible en una fecha
     */
    private boolean isInterviewerAvailableOnDate(Long interviewerId, LocalDate date) {
        List<Interview> dayInterviews = interviewRepository.findByInterviewerAndDateRange(
                interviewerId, date, date);
        
        // Máximo 4 entrevistas por día
        return dayInterviews.size() < 4;
    }
    
    /**
     * Verifica si un slot de tiempo está disponible
     */
    private boolean isTimeSlotAvailable(Long interviewerId, LocalDate date, LocalTime time) {
        long conflicts = interviewRepository.countConflictingInterviews(interviewerId, date, time);
        return conflicts == 0;
    }

    // ================================
    // SEGUIMIENTO Y PROGRESO
    // ================================
    
    /**
     * Actualiza el progreso de las entrevistas y avanza aplicaciones si corresponde
     */
    public Map<String, Object> updateInterviewProgressAndAdvanceApplications() {
        log.info("🔄 Actualizando progreso de entrevistas y avanzando aplicaciones");
        
        Map<String, Object> result = new HashMap<>();
        int applicationsAdvanced = 0;
        int interviewsProcessed = 0;
        
        try {
            // Buscar aplicaciones con entrevistas programadas
            List<Application> applicationsWithInterviews = applicationRepository
                    .findByStatusOrderByCreatedAtDesc(Application.ApplicationStatus.INTERVIEW_SCHEDULED);
            
            for (Application application : applicationsWithInterviews) {
                List<Interview> interviews = interviewRepository.findByApplicationId(application.getId());
                interviewsProcessed += interviews.size();
                
                // Verificar si todas las entrevistas están completadas
                if (areAllInterviewsCompleted(interviews)) {
                    log.info("✅ Todas las entrevistas completadas para aplicación {}", application.getId());
                    
                    // Intentar avanzar la aplicación automáticamente
                    boolean advanced = workflowService.evaluateAndAdvanceApplication(application.getId());
                    if (advanced) {
                        applicationsAdvanced++;
                    }
                }
            }
            
            result.put("success", true);
            result.put("applicationsAdvanced", applicationsAdvanced);
            result.put("interviewsProcessed", interviewsProcessed);
            
        } catch (Exception e) {
            log.error("❌ Error actualizando progreso de entrevistas", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        result.put("timestamp", LocalDateTime.now());
        
        log.info("✅ Progreso actualizado: {} aplicaciones avanzadas, {} entrevistas procesadas", 
                applicationsAdvanced, interviewsProcessed);
        
        return result;
    }
    
    /**
     * Verifica si todas las entrevistas requeridas están completadas
     */
    private boolean areAllInterviewsCompleted(List<Interview> interviews) {
        if (interviews.isEmpty()) {
            return false;
        }
        
        // Verificar que al menos hay las entrevistas mínimas requeridas
        Set<Interview.InterviewType> requiredTypes = Set.of(
            Interview.InterviewType.FAMILY,
            Interview.InterviewType.PSYCHOLOGICAL
        );
        
        Set<Interview.InterviewType> completedTypes = interviews.stream()
                .filter(i -> i.getStatus() == Interview.InterviewStatus.COMPLETED)
                .map(Interview::getType)
                .collect(Collectors.toSet());
        
        return completedTypes.containsAll(requiredTypes);
    }

    // ================================
    // REPORTES Y MÉTRICAS
    // ================================
    
    /**
     * Genera reporte completo del estado de entrevistas
     */
    public Map<String, Object> generateInterviewReport() {
        log.info("📊 Generando reporte completo de entrevistas");
        
        Map<String, Object> report = new HashMap<>();
        
        try {
            // Estadísticas generales
            long totalInterviews = interviewRepository.count();
            
            Map<String, Long> statusDistribution = getInterviewStatusDistribution();
            Map<String, Long> typeDistribution = getInterviewTypeDistribution();
            Map<String, Long> modeDistribution = getInterviewModeDistribution();
            
            // Métricas de rendimiento
            Map<String, Object> performanceMetrics = calculatePerformanceMetrics();
            
            // Entrevistas próximas (próximos 7 días)
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            List<Interview> upcomingInterviews = interviewRepository.findByDateRangeAndStatuses(
                today, nextWeek, Arrays.asList(Interview.InterviewStatus.SCHEDULED));
            
            // Entrevistas vencidas
            List<Interview> overdueInterviews = interviewRepository.findOverdueInterviews();
            
            // Compilar reporte
            report.put("totalInterviews", totalInterviews);
            report.put("statusDistribution", statusDistribution);
            report.put("typeDistribution", typeDistribution);
            report.put("modeDistribution", modeDistribution);
            report.put("performanceMetrics", performanceMetrics);
            report.put("upcomingInterviews", upcomingInterviews.size());
            report.put("overdueInterviews", overdueInterviews.size());
            report.put("reportDate", LocalDateTime.now());
            report.put("success", true);
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte de entrevistas", e);
            report.put("success", false);
            report.put("error", e.getMessage());
        }
        
        return report;
    }
    
    private Map<String, Long> getInterviewStatusDistribution() {
        return interviewRepository.findStatusDistribution().stream()
                .collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> (Long) row[1]
                ));
    }
    
    private Map<String, Long> getInterviewTypeDistribution() {
        return interviewRepository.findTypeDistribution().stream()
                .collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> (Long) row[1]
                ));
    }
    
    private Map<String, Long> getInterviewModeDistribution() {
        return interviewRepository.findModeDistribution().stream()
                .collect(Collectors.toMap(
                    row -> row[0].toString(),
                    row -> (Long) row[1]
                ));
    }
    
    private Map<String, Object> calculatePerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Promedio de calificaciones
        Optional<Double> averageScore = interviewRepository.findAverageScore();
        metrics.put("averageScore", averageScore.orElse(0.0));
        
        // Entrevistas con resultado positivo
        long positiveResults = interviewRepository.countPositiveResults();
        metrics.put("positiveResults", positiveResults);
        
        // Tiempo promedio de completación (esto se podría calcular si se tienen fechas)
        metrics.put("completionRate", calculateCompletionRate());
        
        return metrics;
    }
    
    private double calculateCompletionRate() {
        long totalScheduled = interviewRepository.countByStatus(Interview.InterviewStatus.SCHEDULED);
        long totalCompleted = interviewRepository.countByStatus(Interview.InterviewStatus.COMPLETED);
        
        if (totalScheduled + totalCompleted == 0) {
            return 0.0;
        }
        
        return (double) totalCompleted / (totalScheduled + totalCompleted) * 100.0;
    }

    // ================================
    // MÉTODOS AUXILIARES
    // ================================
    
    /**
     * Determina el modo de entrevista según el tipo
     */
    private Interview.InterviewMode determineInterviewMode(Interview.InterviewType type) {
        return switch (type) {
            case FAMILY, INDIVIDUAL -> Interview.InterviewMode.IN_PERSON;
            case PSYCHOLOGICAL -> Interview.InterviewMode.IN_PERSON; // Preferencia presencial
            case ACADEMIC -> Interview.InterviewMode.HYBRID; // Puede ser virtual o presencial
            case BEHAVIORAL -> Interview.InterviewMode.IN_PERSON;
            default -> Interview.InterviewMode.IN_PERSON;
        };
    }
    
    /**
     * Determina la ubicación según el tipo y modo
     */
    private String determineLocation(Interview.InterviewType type, Interview.InterviewMode mode) {
        if (mode == Interview.InterviewMode.VIRTUAL) {
            return "Enlace de videollamada será enviado";
        }
        
        return switch (type) {
            case FAMILY -> "Sala de Reuniones Principal - Edificio Administrativo";
            case PSYCHOLOGICAL -> "Oficina de Orientación - Piso 2";
            case ACADEMIC -> "Sala de Profesores - Edificio Académico";
            case INDIVIDUAL -> "Oficina de Dirección - Piso 3";
            case BEHAVIORAL -> "Sala de Psicopedagogía - Piso 1";
            default -> "Por confirmar";
        };
    }
}