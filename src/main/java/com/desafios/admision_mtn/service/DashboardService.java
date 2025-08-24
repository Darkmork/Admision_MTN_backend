package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.*;
import com.desafios.admision_mtn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar dashboards y reportes administrativos completos
 * 
 * Proporciona métricas y análisis detallados del proceso de admisión:
 * - Estadísticas generales del sistema
 * - Análisis de flujo de aplicaciones
 * - Métricas de entrevistas y evaluaciones
 * - Reportes de rendimiento temporal
 * - KPIs de gestión administrativa
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    // private final StudentsRepository studentsRepository;

    /**
     * Dashboard completo con todas las métricas principales
     */
    @Cacheable(value = "statistics", key = "'complete-dashboard'")
    public Map<String, Object> getCompleteDashboard() {
        log.info("📊 Generando dashboard completo del sistema (cached)");
        
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Métricas generales del sistema
            dashboard.put("systemOverview", getSystemOverview());
            
            // Estadísticas de aplicaciones
            dashboard.put("applicationStats", getApplicationStatistics());
            
            // Análisis de entrevistas
            dashboard.put("interviewAnalysis", getInterviewAnalysis());
            
            // Métricas de evaluaciones
            dashboard.put("evaluationMetrics", getEvaluationMetrics());
            
            // Análisis temporal (últimos 30 días)
            dashboard.put("temporalAnalysis", getTemporalAnalysis());
            
            // KPIs administrativos
            dashboard.put("adminKPIs", getAdministrativeKPIs());
            
            // Alertas del sistema
            dashboard.put("systemAlerts", getSystemAlerts());
            
            // Información de generación
            dashboard.put("generatedAt", LocalDateTime.now());
            dashboard.put("reportType", "COMPLETE_DASHBOARD");
            dashboard.put("success", true);
            
            log.info("✅ Dashboard completo generado exitosamente");
            
        } catch (Exception e) {
            log.error("❌ Error generando dashboard completo", e);
            dashboard.put("success", false);
            dashboard.put("error", e.getMessage());
        }
        
        return dashboard;
    }

    /**
     * Resumen general del sistema
     */
    @Cacheable(value = "statistics", key = "'system-overview'")
    public Map<String, Object> getSystemOverview() {
        log.debug("🔍 Generando resumen general del sistema (cached)");
        
        Map<String, Object> overview = new HashMap<>();
        
        // Contadores principales
        long totalApplications = applicationRepository.count();
        long totalStudents = applicationRepository.count(); // Usando aplicaciones como proxy para estudiantes
        long totalInterviews = interviewRepository.count();
        long totalEvaluations = evaluationRepository.count();
        long totalUsers = userRepository.count();
        
        overview.put("totalApplications", totalApplications);
        overview.put("totalStudents", totalStudents);
        overview.put("totalInterviews", totalInterviews);
        overview.put("totalEvaluations", totalEvaluations);
        overview.put("totalUsers", totalUsers);
        
        // Estado del sistema
        overview.put("systemStatus", "ACTIVE");
        overview.put("lastUpdate", LocalDateTime.now());
        
        // Distribución de usuarios por rol
        Map<String, Long> usersByRole = userRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    user -> user.getRole().name(),
                    Collectors.counting()
                ));
        overview.put("userDistribution", usersByRole);
        
        return overview;
    }

    /**
     * Estadísticas detalladas de aplicaciones
     */
    public Map<String, Object> getApplicationStatistics() {
        log.debug("📋 Generando estadísticas de aplicaciones");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Distribución por estado
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            long count = applicationRepository.countByStatus(status);
            statusDistribution.put(status.name(), count);
        }
        stats.put("statusDistribution", statusDistribution);
        
        // Aplicaciones por mes (últimos 12 meses)
        Map<String, Long> monthlyApplications = getApplicationsByMonth();
        stats.put("monthlyTrend", monthlyApplications);
        
        // Tiempo promedio de procesamiento
        Double avgProcessingTime = calculateAverageProcessingTime();
        stats.put("averageProcessingDays", avgProcessingTime);
        
        // Aplicaciones recientes (últimos 7 días)
        List<Application> recentApplications = applicationRepository.findRecentApplications(
                LocalDateTime.now().minusDays(7)
        );
        stats.put("recentApplicationsCount", recentApplications.size());
        
        // Aplicaciones pendientes de revisión
        long pendingReview = applicationRepository.countByStatus(Application.ApplicationStatus.UNDER_REVIEW);
        stats.put("pendingReviewCount", pendingReview);
        
        // Tasa de aprobación
        long approvedCount = applicationRepository.countByStatus(Application.ApplicationStatus.APPROVED);
        long totalProcessed = getTotalProcessedApplications();
        double approvalRate = totalProcessed > 0 ? (double) approvedCount / totalProcessed * 100 : 0;
        stats.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
        
        return stats;
    }

    /**
     * Análisis completo de entrevistas
     */
    public Map<String, Object> getInterviewAnalysis() {
        log.debug("🎤 Generando análisis de entrevistas");
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Distribución por estado
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Interview.InterviewStatus status : Interview.InterviewStatus.values()) {
            long count = interviewRepository.countByStatus(status);
            statusDistribution.put(status.name(), count);
        }
        analysis.put("statusDistribution", statusDistribution);
        
        // Distribución por tipo
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Interview.InterviewType type : Interview.InterviewType.values()) {
            long count = interviewRepository.countByType(type);
            typeDistribution.put(type.name(), count);
        }
        analysis.put("typeDistribution", typeDistribution);
        
        // Distribución por modalidad
        Map<String, Long> modeDistribution = new HashMap<>();
        for (Interview.InterviewMode mode : Interview.InterviewMode.values()) {
            long count = interviewRepository.countByMode(mode);
            modeDistribution.put(mode.name(), count);
        }
        analysis.put("modeDistribution", modeDistribution);
        
        // Entrevistas por día (próximos 7 días)
        List<Interview> upcomingInterviews = interviewRepository.findUpcomingInterviews(
                LocalDate.now(), LocalDate.now().plusDays(7)
        );
        analysis.put("upcomingInterviews", upcomingInterviews.size());
        
        // Entrevistas programadas hoy
        List<Interview> todayInterviews = interviewRepository.findTodaysInterviews();
        analysis.put("todayInterviewsCount", todayInterviews.size());
        
        // Carga de trabajo por entrevistador
        Map<String, Long> workloadByInterviewer = getInterviewerWorkload();
        analysis.put("interviewerWorkload", workloadByInterviewer);
        
        return analysis;
    }

    /**
     * Métricas de evaluaciones
     */
    public Map<String, Object> getEvaluationMetrics() {
        log.debug("📊 Generando métricas de evaluaciones");
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Distribución por tipo de evaluación
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Evaluation.EvaluationType type : Evaluation.EvaluationType.values()) {
            long count = evaluationRepository.countByType(type);
            typeDistribution.put(type.name(), count);
        }
        metrics.put("typeDistribution", typeDistribution);
        
        // Distribución por estado
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Evaluation.EvaluationStatus status : Evaluation.EvaluationStatus.values()) {
            long count = evaluationRepository.countByStatus(status);
            statusDistribution.put(status.name(), count);
        }
        metrics.put("statusDistribution", statusDistribution);
        
        // Evaluaciones completadas vs pendientes
        long completedEvaluations = evaluationRepository.countByStatus(Evaluation.EvaluationStatus.COMPLETED);
        long pendingEvaluations = evaluationRepository.countByStatus(Evaluation.EvaluationStatus.PENDING);
        
        metrics.put("completedCount", completedEvaluations);
        metrics.put("pendingCount", pendingEvaluations);
        
        // Puntuación promedio por tipo
        Map<String, Double> averageScoresByType = calculateAverageScoresByType();
        metrics.put("averageScoresByType", averageScoresByType);
        
        // Evaluadores más activos
        Map<String, Long> evaluatorActivity = getEvaluatorActivity();
        metrics.put("evaluatorActivity", evaluatorActivity);
        
        return metrics;
    }

    /**
     * Análisis temporal de los últimos 30 días
     */
    public Map<String, Object> getTemporalAnalysis() {
        log.debug("⏰ Generando análisis temporal");
        
        Map<String, Object> temporal = new HashMap<>();
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // Aplicaciones por día (últimos 30 días)
        Map<String, Long> dailyApplications = getDailyApplications(thirtyDaysAgo);
        temporal.put("dailyApplications", dailyApplications);
        
        // Entrevistas completadas por día
        Map<String, Long> dailyInterviewsCompleted = getDailyCompletedInterviews(thirtyDaysAgo);
        temporal.put("dailyInterviewsCompleted", dailyInterviewsCompleted);
        
        // Evaluaciones completadas por día
        Map<String, Long> dailyEvaluationsCompleted = getDailyCompletedEvaluations(thirtyDaysAgo);
        temporal.put("dailyEvaluationsCompleted", dailyEvaluationsCompleted);
        
        // Tendencia general
        temporal.put("analysisperiod", "LAST_30_DAYS");
        temporal.put("fromDate", thirtyDaysAgo.toLocalDate());
        temporal.put("toDate", LocalDate.now());
        
        return temporal;
    }

    /**
     * KPIs administrativos clave
     */
    public Map<String, Object> getAdministrativeKPIs() {
        log.debug("📈 Generando KPIs administrativos");
        
        Map<String, Object> kpis = new HashMap<>();
        
        // Tiempo promedio del proceso completo
        Double avgTotalTime = calculateAverageTotalProcessTime();
        kpis.put("averageTotalProcessDays", avgTotalTime);
        
        // Eficiencia del proceso (aplicaciones completadas vs iniciadas)
        long totalApplications = applicationRepository.count();
        long completedApplications = getCompletedApplicationsCount();
        double efficiency = totalApplications > 0 ? (double) completedApplications / totalApplications * 100 : 0;
        kpis.put("processEfficiency", Math.round(efficiency * 100.0) / 100.0);
        
        // Carga de trabajo promedio por evaluador
        double avgWorkloadPerEvaluator = calculateAverageWorkloadPerEvaluator();
        kpis.put("averageWorkloadPerEvaluator", avgWorkloadPerEvaluator);
        
        // Tiempo de respuesta de entrevistas (días desde programación hasta realización)
        Double avgInterviewResponseTime = calculateAverageInterviewResponseTime();
        kpis.put("averageInterviewResponseDays", avgInterviewResponseTime);
        
        // Productividad diaria (aplicaciones procesadas por día)
        double dailyProductivity = calculateDailyProductivity();
        kpis.put("dailyProductivity", dailyProductivity);
        
        return kpis;
    }

    /**
     * Alertas del sistema que requieren atención
     */
    public List<Map<String, Object>> getSystemAlerts() {
        log.debug("🚨 Generando alertas del sistema");
        
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Aplicaciones pendientes de revisión por más de 7 días
        List<Application> overdueApplications = findOverdueApplications();
        if (!overdueApplications.isEmpty()) {
            alerts.add(Map.of(
                "type", "OVERDUE_APPLICATIONS",
                "severity", "HIGH",
                "count", overdueApplications.size(),
                "message", "Aplicaciones pendientes de revisión por más de 7 días",
                "action", "Revisar aplicaciones en estado UNDER_REVIEW"
            ));
        }
        
        // Entrevistas sin programar para aplicaciones aprobadas
        long applicationsWithoutInterviews = countApplicationsWithoutScheduledInterviews();
        if (applicationsWithoutInterviews > 0) {
            alerts.add(Map.of(
                "type", "MISSING_INTERVIEWS",
                "severity", "MEDIUM",
                "count", applicationsWithoutInterviews,
                "message", "Aplicaciones sin entrevistas programadas",
                "action", "Programar entrevistas pendientes"
            ));
        }
        
        // Evaluaciones pendientes por más de 5 días
        long overdueEvaluations = countOverdueEvaluations();
        if (overdueEvaluations > 0) {
            alerts.add(Map.of(
                "type", "OVERDUE_EVALUATIONS",
                "severity", "MEDIUM",
                "count", overdueEvaluations,
                "message", "Evaluaciones pendientes por más de 5 días",
                "action", "Revisar evaluaciones asignadas"
            ));
        }
        
        // Entrevistas programadas para hoy
        List<Interview> todayInterviews = interviewRepository.findTodaysInterviews();
        if (!todayInterviews.isEmpty()) {
            alerts.add(Map.of(
                "type", "TODAY_INTERVIEWS",
                "severity", "INFO",
                "count", todayInterviews.size(),
                "message", "Entrevistas programadas para hoy",
                "action", "Revisar agenda de entrevistas"
            ));
        }
        
        return alerts;
    }

    // ================== MÉTODOS AUXILIARES ==================

    private Map<String, Long> getApplicationsByMonth() {
        // Implementación simplificada - podría usar query nativa para mejor rendimiento
        List<Application> allApplications = applicationRepository.findAll();
        
        return allApplications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getCreatedAt().getMonth().name(),
                    Collectors.counting()
                ));
    }

    private Double calculateAverageProcessingTime() {
        List<Application> completedApps = applicationRepository.findCompletedApplications();
        
        if (completedApps.isEmpty()) return 0.0;
        
        double totalDays = completedApps.stream()
                .mapToLong(app -> ChronoUnit.DAYS.between(app.getCreatedAt(), app.getUpdatedAt()))
                .average()
                .orElse(0.0);
        
        return Math.round(totalDays * 100.0) / 100.0;
    }

    private long getTotalProcessedApplications() {
        return applicationRepository.countByStatus(Application.ApplicationStatus.APPROVED) +
               applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED) +
               applicationRepository.countByStatus(Application.ApplicationStatus.WAITLIST);
    }

    private Map<String, Long> getInterviewerWorkload() {
        List<Interview> allInterviews = interviewRepository.findAll();
        
        return allInterviews.stream()
                .filter(interview -> interview.getInterviewer() != null)
                .collect(Collectors.groupingBy(
                    interview -> interview.getInterviewer().getFirstName() + " " + 
                               interview.getInterviewer().getLastName(),
                    Collectors.counting()
                ));
    }

    private Map<String, Double> calculateAverageScoresByType() {
        List<Evaluation> allEvaluations = evaluationRepository.findAll();
        
        return allEvaluations.stream()
                .filter(eval -> eval.getScore() != null)
                .collect(Collectors.groupingBy(
                    eval -> eval.getEvaluationType().name(),
                    Collectors.averagingDouble(Evaluation::getScore)
                ));
    }

    private Map<String, Long> getEvaluatorActivity() {
        List<Evaluation> allEvaluations = evaluationRepository.findAll();
        
        return allEvaluations.stream()
                .filter(eval -> eval.getEvaluator() != null)
                .collect(Collectors.groupingBy(
                    eval -> eval.getEvaluator().getFirstName() + " " + 
                           eval.getEvaluator().getLastName(),
                    Collectors.counting()
                ));
    }

    private Map<String, Long> getDailyApplications(LocalDateTime fromDate) {
        List<Application> applications = applicationRepository.findFromDate(fromDate);
        
        return applications.stream()
                .collect(Collectors.groupingBy(
                    app -> app.getCreatedAt().toLocalDate().toString(),
                    Collectors.counting()
                ));
    }

    private Map<String, Long> getDailyCompletedInterviews(LocalDateTime fromDate) {
        List<Interview> interviews = interviewRepository.findCompletedFromDate(fromDate);
        
        return interviews.stream()
                .collect(Collectors.groupingBy(
                    interview -> interview.getCompletedAt() != null ?
                                interview.getCompletedAt().toLocalDate().toString() :
                                "Sin fecha",
                    Collectors.counting()
                ));
    }

    private Map<String, Long> getDailyCompletedEvaluations(LocalDateTime fromDate) {
        List<Evaluation> evaluations = evaluationRepository.findCompletedFromDate(fromDate);
        
        return evaluations.stream()
                .collect(Collectors.groupingBy(
                    eval -> eval.getCompletionDate() != null ?
                           eval.getCompletionDate().toLocalDate().toString() :
                           "Sin fecha",
                    Collectors.counting()
                ));
    }

    private Double calculateAverageTotalProcessTime() {
        List<Application> completedApplications = applicationRepository.findCompletedApplications();
        
        if (completedApplications.isEmpty()) return 0.0;
        
        double avgDays = completedApplications.stream()
                .filter(app -> app.getUpdatedAt() != null)
                .mapToLong(app -> ChronoUnit.DAYS.between(app.getCreatedAt(), app.getUpdatedAt()))
                .average()
                .orElse(0.0);
        
        return Math.round(avgDays * 100.0) / 100.0;
    }

    private long getCompletedApplicationsCount() {
        return applicationRepository.countByStatus(Application.ApplicationStatus.APPROVED) +
               applicationRepository.countByStatus(Application.ApplicationStatus.REJECTED) +
               applicationRepository.countByStatus(Application.ApplicationStatus.WAITLIST);
    }

    private double calculateAverageWorkloadPerEvaluator() {
        long totalEvaluations = evaluationRepository.count();
        long totalEvaluators = userRepository.countByRole(User.UserRole.TEACHER) +
                              userRepository.countByRole(User.UserRole.COORDINATOR);
        
        return totalEvaluators > 0 ? (double) totalEvaluations / totalEvaluators : 0.0;
    }

    private Double calculateAverageInterviewResponseTime() {
        List<Interview> completedInterviews = interviewRepository.findByStatus(Interview.InterviewStatus.COMPLETED);
        
        if (completedInterviews.isEmpty()) return 0.0;
        
        double avgDays = completedInterviews.stream()
                .filter(interview -> interview.getScheduledDate() != null && 
                                   interview.getCompletedAt() != null)
                .mapToLong(interview -> ChronoUnit.DAYS.between(
                    interview.getScheduledDate(),
                    interview.getCompletedAt().toLocalDate()
                ))
                .average()
                .orElse(0.0);
        
        return Math.round(avgDays * 100.0) / 100.0;
    }

    private double calculateDailyProductivity() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Application> recentApplications = applicationRepository.findFromDate(thirtyDaysAgo);
        
        return recentApplications.size() / 30.0;
    }

    private List<Application> findOverdueApplications() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return applicationRepository.findOverdueApplications(
                Application.ApplicationStatus.UNDER_REVIEW, sevenDaysAgo
        );
    }

    private long countApplicationsWithoutScheduledInterviews() {
        // Aplicaciones aprobadas que no tienen entrevistas programadas
        List<Application> approvedApplications = applicationRepository.findByStatusOrderByCreatedAtDesc(
                Application.ApplicationStatus.INTERVIEW_SCHEDULED
        );
        
        return approvedApplications.stream()
                .filter(app -> {
                    List<Interview> interviews = interviewRepository.findByApplication_IdOrderByCreatedAtDesc(app.getId());
                    return interviews.isEmpty();
                })
                .count();
    }

    private long countOverdueEvaluations() {
        LocalDateTime fiveDaysAgo = LocalDateTime.now().minusDays(5);
        return evaluationRepository.countOverdueEvaluations(
                Evaluation.EvaluationStatus.PENDING, fiveDaysAgo
        );
    }
}