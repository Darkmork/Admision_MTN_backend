package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Obtener métricas principales del dashboard
     */
    @GetMapping("/dashboard-metrics")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        try {
            log.info("📊 Obteniendo métricas del dashboard");
            Map<String, Object> metrics = analyticsService.getDashboardMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("❌ Error obteniendo métricas del dashboard", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener métricas: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener distribución por estado de postulaciones
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<Map<String, Object>> getStatusDistribution() {
        try {
            log.info("📈 Obteniendo distribución por estado");
            Map<String, Object> distribution = analyticsService.getStatusDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            log.error("❌ Error obteniendo distribución por estado", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener distribución: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener distribución por grado académico
     */
    @GetMapping("/grade-distribution")
    public ResponseEntity<Map<String, Object>> getGradeDistribution() {
        try {
            log.info("📚 Obteniendo distribución por grado");
            Map<String, Object> distribution = analyticsService.getGradeDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            log.error("❌ Error obteniendo distribución por grado", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener distribución por grado: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener análisis de evaluadores
     */
    @GetMapping("/evaluator-analysis")
    public ResponseEntity<Map<String, Object>> getEvaluatorAnalysis() {
        try {
            log.info("👥 Obteniendo análisis de evaluadores");
            Map<String, Object> analysis = analyticsService.getEvaluatorAnalysis();
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("❌ Error obteniendo análisis de evaluadores", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener análisis de evaluadores: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener tendencias temporales
     */
    @GetMapping("/temporal-trends")
    public ResponseEntity<Map<String, Object>> getTemporalTrends() {
        try {
            log.info("📅 Obteniendo tendencias temporales");
            Map<String, Object> trends = analyticsService.getTemporalTrends();
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            log.error("❌ Error obteniendo tendencias temporales", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener tendencias: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener métricas de rendimiento del proceso
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            log.info("⚡ Obteniendo métricas de rendimiento");
            Map<String, Object> metrics = analyticsService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("❌ Error obteniendo métricas de rendimiento", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener métricas de rendimiento: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener insights y recomendaciones
     */
    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getInsights() {
        try {
            log.info("💡 Obteniendo insights y recomendaciones");
            Map<String, Object> insights = analyticsService.getInsights();
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            log.error("❌ Error obteniendo insights", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener insights: " + e.getMessage())
            );
        }
    }

    /**
     * Obtener todas las métricas de análisis en una sola llamada
     */
    @GetMapping("/complete-analytics")
    public ResponseEntity<Map<String, Object>> getCompleteAnalytics() {
        try {
            log.info("🎯 Obteniendo análisis completo");
            Map<String, Object> completeAnalytics = analyticsService.getCompleteAnalytics();
            return ResponseEntity.ok(completeAnalytics);
        } catch (Exception e) {
            log.error("❌ Error obteniendo análisis completo", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Error al obtener análisis completo: " + e.getMessage())
            );
        }
    }
}