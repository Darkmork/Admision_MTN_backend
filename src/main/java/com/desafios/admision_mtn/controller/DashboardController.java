package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para dashboards y reportes administrativos
 * 
 * Proporciona endpoints para generar reportes completos del sistema:
 * - Dashboard principal con todas las métricas
 * - Reportes específicos por área (aplicaciones, entrevistas, evaluaciones)
 * - KPIs administrativos y alertas del sistema
 * - Análisis temporal y estadísticas de rendimiento
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboards y reportes administrativos del sistema de admisión")
@PreAuthorize("hasAnyRole('ADMIN', 'CYCLE_DIRECTOR')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:5177"})
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Dashboard principal con todas las métricas del sistema
     */
    @Operation(
        summary = "Obtener dashboard completo",
        description = "Retorna un dashboard completo con todas las métricas del sistema: aplicaciones, entrevistas, evaluaciones, KPIs administrativos y alertas.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard generado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "systemOverview": {
                            "totalApplications": 150,
                            "totalUsers": 25,
                            "totalInterviews": 89,
                            "totalEvaluations": 123,
                            "systemStatus": "ACTIVE"
                        },
                        "applicationStats": {
                            "statusDistribution": {
                                "PENDING": 12,
                                "UNDER_REVIEW": 45,
                                "APPROVED": 67
                            },
                            "approvalRate": 78.5
                        },
                        "success": true,
                        "generatedAt": "2025-08-24T10:30:00"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sin permisos para acceder al dashboard"
        )
    })
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteDashboard() {
        try {
            log.info("📊 Generando dashboard completo via REST");
            
            Map<String, Object> dashboard = dashboardService.getCompleteDashboard();
            
            log.info("✅ Dashboard completo generado exitosamente");
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("❌ Error generando dashboard completo", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Resumen general del sistema
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        try {
            log.info("🔍 Generando resumen del sistema via REST");
            
            Map<String, Object> overview = dashboardService.getSystemOverview();
            overview.put("success", true);
            overview.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(overview);
            
        } catch (Exception e) {
            log.error("❌ Error generando resumen del sistema", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Estadísticas detalladas de aplicaciones
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplicationStatistics() {
        try {
            log.info("📋 Generando estadísticas de aplicaciones via REST");
            
            Map<String, Object> stats = dashboardService.getApplicationStatistics();
            stats.put("success", true);
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error generando estadísticas de aplicaciones", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Análisis completo de entrevistas
     */
    @GetMapping("/interviews")
    public ResponseEntity<Map<String, Object>> getInterviewAnalysis() {
        try {
            log.info("🎤 Generando análisis de entrevistas via REST");
            
            Map<String, Object> analysis = dashboardService.getInterviewAnalysis();
            analysis.put("success", true);
            analysis.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("❌ Error generando análisis de entrevistas", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Métricas de evaluaciones
     */
    @GetMapping("/evaluations")
    public ResponseEntity<Map<String, Object>> getEvaluationMetrics() {
        try {
            log.info("📊 Generando métricas de evaluaciones via REST");
            
            Map<String, Object> metrics = dashboardService.getEvaluationMetrics();
            metrics.put("success", true);
            metrics.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("❌ Error generando métricas de evaluaciones", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Análisis temporal (últimos 30 días)
     */
    @GetMapping("/temporal")
    public ResponseEntity<Map<String, Object>> getTemporalAnalysis() {
        try {
            log.info("⏰ Generando análisis temporal via REST");
            
            Map<String, Object> temporal = dashboardService.getTemporalAnalysis();
            temporal.put("success", true);
            temporal.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(temporal);
            
        } catch (Exception e) {
            log.error("❌ Error generando análisis temporal", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * KPIs administrativos clave
     */
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getAdministrativeKPIs() {
        try {
            log.info("📈 Generando KPIs administrativos via REST");
            
            Map<String, Object> kpis = dashboardService.getAdministrativeKPIs();
            kpis.put("success", true);
            kpis.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(kpis);
            
        } catch (Exception e) {
            log.error("❌ Error generando KPIs administrativos", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Alertas del sistema
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getSystemAlerts() {
        try {
            log.info("🚨 Generando alertas del sistema via REST");
            
            Map<String, Object> response = new HashMap<>();
            response.put("alerts", dashboardService.getSystemAlerts());
            response.put("success", true);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error generando alertas del sistema", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Reporte ejecutivo resumido
     */
    @GetMapping("/executive-summary")
    public ResponseEntity<Map<String, Object>> getExecutiveSummary() {
        try {
            log.info("📋 Generando reporte ejecutivo via REST");
            
            Map<String, Object> summary = new HashMap<>();
            
            // Obtener datos clave de diferentes servicios
            Map<String, Object> overview = dashboardService.getSystemOverview();
            Map<String, Object> applicationStats = dashboardService.getApplicationStatistics();
            Map<String, Object> kpis = dashboardService.getAdministrativeKPIs();
            
            // Compilar reporte ejecutivo
            summary.put("totalApplications", overview.get("totalApplications"));
            summary.put("totalStudents", overview.get("totalStudents"));
            summary.put("totalInterviews", overview.get("totalInterviews"));
            summary.put("approvalRate", applicationStats.get("approvalRate"));
            summary.put("averageProcessingDays", applicationStats.get("averageProcessingDays"));
            summary.put("processEfficiency", kpis.get("processEfficiency"));
            summary.put("dailyProductivity", kpis.get("dailyProductivity"));
            
            // Alertas críticas
            summary.put("criticalAlerts", dashboardService.getSystemAlerts().stream()
                    .filter(alert -> "HIGH".equals(((Map<String, Object>) alert).get("severity")))
                    .count());
            
            summary.put("reportType", "EXECUTIVE_SUMMARY");
            summary.put("success", true);
            summary.put("generatedAt", LocalDateTime.now());
            
            log.info("✅ Reporte ejecutivo generado exitosamente");
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("❌ Error generando reporte ejecutivo", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Estadísticas en tiempo real (datos más recientes)
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStats() {
        try {
            log.info("⚡ Generando estadísticas en tiempo real via REST");
            
            Map<String, Object> realtime = new HashMap<>();
            
            // Datos del sistema en tiempo real
            Map<String, Object> overview = dashboardService.getSystemOverview();
            realtime.put("currentTotalApplications", overview.get("totalApplications"));
            realtime.put("currentTotalUsers", overview.get("totalUsers"));
            realtime.put("systemStatus", overview.get("systemStatus"));
            
            // Alertas activas
            realtime.put("activeAlerts", dashboardService.getSystemAlerts().size());
            
            // Información del servidor
            realtime.put("serverInfo", Map.of(
                "uptime", "Sistema activo",
                "version", "1.0.0",
                "environment", "PRODUCTION"
            ));
            
            realtime.put("success", true);
            realtime.put("timestamp", LocalDateTime.now());
            realtime.put("updateFrequency", "REAL_TIME");
            
            return ResponseEntity.ok(realtime);
            
        } catch (Exception e) {
            log.error("❌ Error generando estadísticas en tiempo real", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Información del sistema de dashboards
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDashboardInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("dashboardName", "Sistema de Dashboards y Reportes Administrativos");
        info.put("version", "1.0.0");
        info.put("description", "Sistema completo de métricas, KPIs y reportes para el proceso de admisión");
        
        info.put("capabilities", Map.of(
            "completeDashboard", "Dashboard integral con todas las métricas del sistema",
            "applicationAnalysis", "Análisis detallado de aplicaciones y flujo de estados",
            "interviewMetrics", "Métricas completas del sistema de entrevistas",
            "evaluationTracking", "Seguimiento de evaluaciones y rendimiento",
            "temporalAnalysis", "Análisis de tendencias y patrones temporales",
            "kpiMonitoring", "Monitoreo de indicadores clave de rendimiento",
            "systemAlerts", "Sistema de alertas para elementos que requieren atención",
            "executiveReports", "Reportes ejecutivos resumidos",
            "realtimeStats", "Estadísticas en tiempo real del sistema"
        ));
        
        info.put("endpoints", Map.of(
            "GET /api/dashboard/complete", "Dashboard completo con todas las métricas",
            "GET /api/dashboard/overview", "Resumen general del sistema",
            "GET /api/dashboard/applications", "Estadísticas detalladas de aplicaciones",
            "GET /api/dashboard/interviews", "Análisis completo de entrevistas",
            "GET /api/dashboard/evaluations", "Métricas de evaluaciones",
            "GET /api/dashboard/temporal", "Análisis temporal (últimos 30 días)",
            "GET /api/dashboard/kpis", "KPIs administrativos clave",
            "GET /api/dashboard/alerts", "Alertas del sistema",
            "GET /api/dashboard/executive-summary", "Reporte ejecutivo resumido",
            "GET /api/dashboard/realtime", "Estadísticas en tiempo real"
        ));
        
        info.put("features", Map.of(
            "automaticRefresh", "Datos actualizados automáticamente",
            "roleBasedAccess", "Acceso restringido a administradores y directores",
            "comprehensiveMetrics", "Métricas completas de todo el proceso",
            "alertSystem", "Sistema de alertas para elementos críticos",
            "temporalTracking", "Seguimiento de tendencias temporales",
            "performanceKPIs", "Indicadores de rendimiento del proceso",
            "executiveReporting", "Reportes ejecutivos para toma de decisiones"
        ));
        
        info.put("dataTypes", Map.of(
            "applications", "Análisis completo del flujo de aplicaciones",
            "interviews", "Métricas de planificación y ejecución de entrevistas",
            "evaluations", "Seguimiento de evaluaciones académicas y psicológicas",
            "users", "Distribución y actividad de usuarios del sistema",
            "temporal", "Análisis de patrones y tendencias temporales",
            "performance", "KPIs de eficiencia y productividad del proceso"
        ));
        
        info.put("timestamp", LocalDateTime.now());
        info.put("status", "ACTIVE");
        
        return ResponseEntity.ok(info);
    }

    /**
     * Endpoint de testing para validar el sistema de dashboards
     */
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testDashboardSystem() {
        try {
            log.info("🧪 Ejecutando test completo del sistema de dashboards");
            
            Map<String, Object> testResult = new HashMap<>();
            
            // Test de overview
            try {
                Map<String, Object> overviewTest = dashboardService.getSystemOverview();
                testResult.put("overviewTest", Map.of(
                    "status", "OK",
                    "totalApplications", overviewTest.get("totalApplications"),
                    "totalUsers", overviewTest.get("totalUsers")
                ));
            } catch (Exception e) {
                testResult.put("overviewTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test de estadísticas de aplicaciones
            try {
                Map<String, Object> applicationTest = dashboardService.getApplicationStatistics();
                testResult.put("applicationStatsTest", Map.of(
                    "status", "OK",
                    "statusDistribution", applicationTest.get("statusDistribution")
                ));
            } catch (Exception e) {
                testResult.put("applicationStatsTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test de análisis de entrevistas
            try {
                Map<String, Object> interviewTest = dashboardService.getInterviewAnalysis();
                testResult.put("interviewAnalysisTest", Map.of(
                    "status", "OK",
                    "upcomingInterviews", interviewTest.get("upcomingInterviews")
                ));
            } catch (Exception e) {
                testResult.put("interviewAnalysisTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test de KPIs
            try {
                Map<String, Object> kpiTest = dashboardService.getAdministrativeKPIs();
                testResult.put("kpiTest", Map.of(
                    "status", "OK",
                    "processEfficiency", kpiTest.get("processEfficiency")
                ));
            } catch (Exception e) {
                testResult.put("kpiTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            // Test de alertas
            try {
                Object alertsTest = dashboardService.getSystemAlerts();
                testResult.put("alertsTest", Map.of(
                    "status", "OK",
                    "alertCount", ((java.util.List<?>) alertsTest).size()
                ));
            } catch (Exception e) {
                testResult.put("alertsTest", Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
            
            testResult.put("success", true);
            testResult.put("message", "Test completo del sistema de dashboards ejecutado");
            testResult.put("timestamp", LocalDateTime.now());
            
            log.info("✅ Test del sistema de dashboards completado");
            
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            log.error("❌ Error en test del sistema de dashboards", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}