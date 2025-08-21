package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.User;
import com.desafios.admision_mtn.entity.Evaluation;
import com.desafios.admision_mtn.entity.Application.ApplicationStatus;
import com.desafios.admision_mtn.entity.User.UserRole;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    /**
     * Obtener métricas principales del dashboard
     */
    public Map<String, Object> getDashboardMetrics() {
        log.info("📊 Calculando métricas principales del dashboard");
        
        List<Application> allApplications = applicationRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<Evaluation> allEvaluations = evaluationRepository.findAll();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Total de postulaciones
        metrics.put("totalApplications", allApplications.size());
        
        // Postulaciones del último mes
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        long applicationsThisMonth = allApplications.stream()
            .filter(app -> app.getCreatedAt().isAfter(lastMonth))
            .count();
        metrics.put("applicationsThisMonth", applicationsThisMonth);
        
        // Tasa de conversión (aceptadas)
        long acceptedApplications = allApplications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
            .count();
        double conversionRate = allApplications.isEmpty() ? 0.0 : 
            (double) acceptedApplications / allApplications.size() * 100;
        metrics.put("conversionRate", Math.round(conversionRate));
        metrics.put("acceptedApplications", acceptedApplications);
        
        // Tiempo promedio para completar postulación
        double averageCompletionDays = allApplications.stream()
            .filter(app -> app.getSubmissionDate() != null)
            .mapToLong(app -> {
                return java.time.Duration.between(app.getCreatedAt(), app.getSubmissionDate()).toDays();
            })
            .average()
            .orElse(0.0);
        metrics.put("averageCompletionDays", Math.round(averageCompletionDays));
        
        // Evaluadores activos
        long activeEvaluators = allUsers.stream()
            .filter(user -> user.getActive())
            .filter(user -> Arrays.asList(
                UserRole.TEACHER, 
                UserRole.COORDINATOR, 
                UserRole.PSYCHOLOGIST, 
                UserRole.CYCLE_DIRECTOR
            ).contains(user.getRole()))
            .count();
        metrics.put("activeEvaluators", activeEvaluators);
        
        // Total usuarios activos
        long totalActiveUsers = allUsers.stream()
            .filter(user -> user.getActive())
            .count();
        metrics.put("totalActiveUsers", totalActiveUsers);
        
        log.info("✅ Métricas principales calculadas: {} postulaciones, {}% conversión, {} evaluadores", 
                allApplications.size(), Math.round(conversionRate), activeEvaluators);
        
        return metrics;
    }

    /**
     * Obtener distribución por estado de postulaciones
     */
    public Map<String, Object> getStatusDistribution() {
        log.info("📈 Calculando distribución por estado");
        
        List<Application> allApplications = applicationRepository.findAll();
        
        Map<String, Long> statusCount = allApplications.stream()
            .collect(Collectors.groupingBy(
                app -> app.getStatus().name(),
                Collectors.counting()
            ));
        
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("statusCount", statusCount);
        distribution.put("totalApplications", allApplications.size());
        
        // Calcular porcentajes
        Map<String, Double> statusPercentages = new HashMap<>();
        for (Map.Entry<String, Long> entry : statusCount.entrySet()) {
            double percentage = allApplications.isEmpty() ? 0.0 : 
                (double) entry.getValue() / allApplications.size() * 100;
            statusPercentages.put(entry.getKey(), Math.round(percentage * 100.0) / 100.0);
        }
        distribution.put("statusPercentages", statusPercentages);
        
        log.info("✅ Distribución por estado calculada: {}", statusCount);
        return distribution;
    }

    /**
     * Obtener distribución por grado académico
     */
    public Map<String, Object> getGradeDistribution() {
        log.info("📚 Calculando distribución por grado");
        
        List<Application> allApplications = applicationRepository.findAll();
        
        Map<String, Long> gradeCount = allApplications.stream()
            .collect(Collectors.groupingBy(
                app -> app.getStudent().getGradeApplied() != null ? 
                    app.getStudent().getGradeApplied() : "Sin especificar",
                Collectors.counting()
            ));
        
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("gradeCount", gradeCount);
        distribution.put("totalApplications", allApplications.size());
        
        // Calcular porcentajes
        Map<String, Double> gradePercentages = new HashMap<>();
        for (Map.Entry<String, Long> entry : gradeCount.entrySet()) {
            double percentage = allApplications.isEmpty() ? 0.0 : 
                (double) entry.getValue() / allApplications.size() * 100;
            gradePercentages.put(entry.getKey(), Math.round(percentage * 100.0) / 100.0);
        }
        distribution.put("gradePercentages", gradePercentages);
        
        log.info("✅ Distribución por grado calculada: {}", gradeCount);
        return distribution;
    }

    /**
     * Obtener análisis de evaluadores
     */
    public Map<String, Object> getEvaluatorAnalysis() {
        log.info("👥 Calculando análisis de evaluadores");
        
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Long> evaluatorsByRole = allUsers.stream()
            .filter(user -> user.getActive())
            .collect(Collectors.groupingBy(
                user -> user.getRole().name(),
                Collectors.counting()
            ));
        
        // Contar evaluadores por tipo específico
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("teachers", evaluatorsByRole.getOrDefault("TEACHER", 0L));
        analysis.put("coordinators", evaluatorsByRole.getOrDefault("COORDINATOR", 0L));
        analysis.put("psychologist", evaluatorsByRole.getOrDefault("PSYCHOLOGIST", 0L));
        analysis.put("cycleDirector", evaluatorsByRole.getOrDefault("CYCLE_DIRECTOR", 0L));
        analysis.put("admin", evaluatorsByRole.getOrDefault("ADMIN", 0L));
        
        // Total de evaluadores activos
        long totalEvaluators = Arrays.asList(
            UserRole.TEACHER, UserRole.COORDINATOR, 
            UserRole.PSYCHOLOGIST, UserRole.CYCLE_DIRECTOR
        ).stream()
            .mapToLong(role -> evaluatorsByRole.getOrDefault(role.name(), 0L))
            .sum();
        
        analysis.put("totalEvaluators", totalEvaluators);
        analysis.put("evaluatorsByRole", evaluatorsByRole);
        
        log.info("✅ Análisis de evaluadores calculado: {} total", totalEvaluators);
        return analysis;
    }

    /**
     * Obtener tendencias temporales
     */
    public Map<String, Object> getTemporalTrends() {
        log.info("📅 Calculando tendencias temporales");
        
        List<Application> allApplications = applicationRepository.findAll();
        
        // Agrupamos por mes (últimos 6 meses)
        Map<String, Long> monthlyApplications = allApplications.stream()
            .collect(Collectors.groupingBy(
                app -> app.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                Collectors.counting()
            ));
        
        // Ordenamos y tomamos los últimos 6 meses
        Map<String, Long> last6Months = monthlyApplications.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
            .limit(6)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        Map<String, Object> trends = new HashMap<>();
        trends.put("monthlyApplications", last6Months);
        
        // Comparación mes actual vs anterior
        LocalDateTime now = LocalDateTime.now();
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        long currentMonthCount = monthlyApplications.getOrDefault(currentMonth, 0L);
        long lastMonthCount = monthlyApplications.getOrDefault(lastMonth, 0L);
        
        trends.put("currentMonthApplications", currentMonthCount);
        trends.put("lastMonthApplications", lastMonthCount);
        
        // Calcular tendencia
        if (lastMonthCount > 0) {
            double growthRate = ((double) currentMonthCount - lastMonthCount) / lastMonthCount * 100;
            trends.put("monthlyGrowthRate", Math.round(growthRate * 100.0) / 100.0);
        } else {
            trends.put("monthlyGrowthRate", currentMonthCount > 0 ? 100.0 : 0.0);
        }
        
        log.info("✅ Tendencias temporales calculadas para últimos 6 meses");
        return trends;
    }

    /**
     * Obtener métricas de rendimiento del proceso
     */
    public Map<String, Object> getPerformanceMetrics() {
        log.info("⚡ Calculando métricas de rendimiento");
        
        List<Application> allApplications = applicationRepository.findAll();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Postulaciones completadas (no en PENDING)
        long completedApplications = allApplications.stream()
            .filter(app -> app.getStatus() != ApplicationStatus.PENDING)
            .count();
        double completionRate = allApplications.isEmpty() ? 0.0 : 
            (double) completedApplications / allApplications.size() * 100;
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        
        // En proceso de evaluación
        long underReviewApplications = allApplications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.UNDER_REVIEW)
            .count();
        double underReviewRate = allApplications.isEmpty() ? 0.0 : 
            (double) underReviewApplications / allApplications.size() * 100;
        metrics.put("underReviewRate", Math.round(underReviewRate * 100.0) / 100.0);
        
        // Proceso finalizado (APPROVED, REJECTED, WAITLIST)
        long finalizedApplications = allApplications.stream()
            .filter(app -> Arrays.asList(
                ApplicationStatus.APPROVED, 
                ApplicationStatus.REJECTED, 
                ApplicationStatus.WAITLIST
            ).contains(app.getStatus()))
            .count();
        double finalizationRate = allApplications.isEmpty() ? 0.0 : 
            (double) finalizedApplications / allApplications.size() * 100;
        metrics.put("finalizationRate", Math.round(finalizationRate * 100.0) / 100.0);
        
        metrics.put("completedApplications", completedApplications);
        metrics.put("underReviewApplications", underReviewApplications);
        metrics.put("finalizedApplications", finalizedApplications);
        
        log.info("✅ Métricas de rendimiento calculadas: {}% completadas, {}% finalizadas", 
                Math.round(completionRate), Math.round(finalizationRate));
        return metrics;
    }

    /**
     * Obtener insights y recomendaciones
     */
    public Map<String, Object> getInsights() {
        log.info("💡 Generando insights y recomendaciones");
        
        List<Application> allApplications = applicationRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        
        Map<String, Object> insights = new HashMap<>();
        List<Map<String, String>> recommendations = new ArrayList<>();
        
        // Carga de trabajo
        long underReviewCount = allApplications.stream()
            .filter(app -> app.getStatus() == ApplicationStatus.UNDER_REVIEW)
            .count();
        
        Map<String, String> workloadInsight = new HashMap<>();
        workloadInsight.put("type", "workload");
        workloadInsight.put("title", "Carga de Trabajo");
        if (underReviewCount > 10) {
            workloadInsight.put("message", String.format(
                "Tienes %d postulaciones en revisión. Considera asignar más evaluadores.", 
                underReviewCount
            ));
            workloadInsight.put("level", "warning");
        } else {
            workloadInsight.put("message", "La carga de trabajo está equilibrada.");
            workloadInsight.put("level", "success");
        }
        recommendations.add(workloadInsight);
        
        // Eficiencia del proceso
        long completedApplications = allApplications.stream()
            .filter(app -> app.getStatus() != ApplicationStatus.PENDING)
            .count();
        double completionRate = allApplications.isEmpty() ? 0.0 : 
            (double) completedApplications / allApplications.size();
        
        Map<String, String> efficiencyInsight = new HashMap<>();
        efficiencyInsight.put("type", "efficiency");
        efficiencyInsight.put("title", "Eficiencia del Proceso");
        if (completionRate > 0.8) {
            efficiencyInsight.put("message", "Excelente tasa de finalización de postulaciones.");
            efficiencyInsight.put("level", "success");
        } else {
            efficiencyInsight.put("message", "Considera contactar a familias con postulaciones incompletas.");
            efficiencyInsight.put("level", "info");
        }
        recommendations.add(efficiencyInsight);
        
        // Recursos humanos
        long activeEvaluators = allUsers.stream()
            .filter(user -> user.getActive())
            .filter(user -> Arrays.asList(
                UserRole.TEACHER, UserRole.COORDINATOR, 
                UserRole.PSYCHOLOGIST, UserRole.CYCLE_DIRECTOR
            ).contains(user.getRole()))
            .count();
        
        Map<String, String> resourcesInsight = new HashMap<>();
        resourcesInsight.put("type", "resources");
        resourcesInsight.put("title", "Recursos Humanos");
        resourcesInsight.put("message", String.format(
            "%d evaluadores activos para %d postulaciones.", 
            activeEvaluators, allApplications.size()
        ));
        resourcesInsight.put("level", "info");
        recommendations.add(resourcesInsight);
        
        // Tendencia general
        LocalDateTime now = LocalDateTime.now();
        String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String lastMonth = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        long currentMonthCount = allApplications.stream()
            .filter(app -> app.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(currentMonth))
            .count();
        long lastMonthCount = allApplications.stream()
            .filter(app -> app.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(lastMonth))
            .count();
        
        Map<String, String> trendInsight = new HashMap<>();
        trendInsight.put("type", "trend");
        trendInsight.put("title", "Tendencia General");
        
        if (currentMonthCount > lastMonthCount) {
            double growthRate = lastMonthCount > 0 ? 
                ((double) currentMonthCount - lastMonthCount) / lastMonthCount * 100 : 100.0;
            trendInsight.put("message", String.format(
                "📈 Aumento del %.0f%% en postulaciones este mes.", growthRate
            ));
            trendInsight.put("level", "success");
        } else if (currentMonthCount < lastMonthCount) {
            double decreaseRate = lastMonthCount > 0 ? 
                ((double) lastMonthCount - currentMonthCount) / lastMonthCount * 100 : 0.0;
            trendInsight.put("message", String.format(
                "📉 Disminución del %.0f%% en postulaciones este mes.", decreaseRate
            ));
            trendInsight.put("level", "warning");
        } else {
            trendInsight.put("message", "📊 Postulaciones estables comparado con el mes anterior.");
            trendInsight.put("level", "info");
        }
        recommendations.add(trendInsight);
        
        insights.put("recommendations", recommendations);
        insights.put("totalInsights", recommendations.size());
        
        log.info("✅ Generados {} insights y recomendaciones", recommendations.size());
        return insights;
    }

    /**
     * Obtener todas las métricas de análisis en una sola llamada
     */
    public Map<String, Object> getCompleteAnalytics() {
        log.info("🎯 Generando análisis completo");
        
        Map<String, Object> completeAnalytics = new HashMap<>();
        
        try {
            completeAnalytics.put("dashboardMetrics", getDashboardMetrics());
            completeAnalytics.put("statusDistribution", getStatusDistribution());
            completeAnalytics.put("gradeDistribution", getGradeDistribution());
            completeAnalytics.put("evaluatorAnalysis", getEvaluatorAnalysis());
            completeAnalytics.put("temporalTrends", getTemporalTrends());
            completeAnalytics.put("performanceMetrics", getPerformanceMetrics());
            completeAnalytics.put("insights", getInsights());
            
            completeAnalytics.put("generatedAt", LocalDateTime.now().toString());
            
            log.info("✅ Análisis completo generado exitosamente");
            
        } catch (Exception e) {
            log.error("❌ Error generando análisis completo", e);
            throw new RuntimeException("Error al generar análisis completo", e);
        }
        
        return completeAnalytics;
    }
}