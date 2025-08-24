package com.desafios.admision_mtn.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info;

import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.UserRepository;
import com.desafios.admision_mtn.repository.InterviewRepository;
import com.desafios.admision_mtn.repository.EvaluationRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuración de métricas personalizadas para el sistema de admisión
 * 
 * Proporciona métricas específicas del negocio y health checks personalizados
 * para monitorear el rendimiento y estado del sistema de admisión escolar.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetricsConfig {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final EvaluationRepository evaluationRepository;

    // Contadores de métricas de negocio
    private final AtomicInteger activeApplications = new AtomicInteger();
    private final AtomicInteger completedEvaluations = new AtomicInteger();
    private final AtomicInteger scheduledInterviews = new AtomicInteger();

    /**
     * Health indicator personalizado para el sistema de admisión
     */
    @Bean
    public HealthIndicator admissionSystemHealthIndicator() {
        return () -> {
            try {
                // Verificar conectividad básica a la base de datos
                long applicationCount = applicationRepository.count();
                long userCount = userRepository.count();
                
                // Verificar que hay usuarios administradores activos
                long adminCount = userRepository.countByRoleAndActiveTrue(
                    com.desafios.admision_mtn.entity.User.UserRole.ADMIN);
                
                if (adminCount == 0) {
                    return Health.down()
                        .withDetail("error", "No hay administradores activos en el sistema")
                        .withDetail("applications", applicationCount)
                        .withDetail("users", userCount)
                        .build();
                }
                
                // Sistema saludable
                return Health.up()
                    .withDetail("applications", applicationCount)
                    .withDetail("users", userCount)
                    .withDetail("activeAdmins", adminCount)
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
                    
            } catch (Exception e) {
                log.error("Error en health check del sistema de admisión", e);
                return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
            }
        };
    }

    /**
     * Información adicional para /actuator/info
     */
    @Bean
    public InfoContributor admissionSystemInfoContributor() {
        return builder -> {
            try {
                Map<String, Object> admissionInfo = new HashMap<>();
                
                // Estadísticas generales del sistema
                admissionInfo.put("totalApplications", applicationRepository.count());
                admissionInfo.put("totalUsers", userRepository.count());
                admissionInfo.put("totalInterviews", interviewRepository.count());
                admissionInfo.put("totalEvaluations", evaluationRepository.count());
                
                // Distribución por roles
                Map<String, Long> usersByRole = new HashMap<>();
                for (com.desafios.admision_mtn.entity.User.UserRole role : 
                     com.desafios.admision_mtn.entity.User.UserRole.values()) {
                    usersByRole.put(role.name(), userRepository.countByRole(role));
                }
                admissionInfo.put("usersByRole", usersByRole);
                
                // Estadísticas de aplicaciones por estado
                Map<String, Long> applicationsByStatus = new HashMap<>();
                for (com.desafios.admision_mtn.entity.Application.ApplicationStatus status : 
                     com.desafios.admision_mtn.entity.Application.ApplicationStatus.values()) {
                    applicationsByStatus.put(status.name(), applicationRepository.countByStatus(status));
                }
                admissionInfo.put("applicationsByStatus", applicationsByStatus);
                
                // Información del sistema
                admissionInfo.put("systemStartTime", LocalDateTime.now());
                admissionInfo.put("features", Map.of(
                    "workflows", "enabled",
                    "notifications", "enabled",
                    "interviews", "enabled",
                    "evaluations", "enabled",
                    "documents", "enabled",
                    "stateValidation", "enabled"
                ));
                
                builder.withDetail("admissionSystem", admissionInfo);
                
            } catch (Exception e) {
                log.error("Error recopilando información del sistema", e);
                builder.withDetail("admissionSystemError", e.getMessage());
            }
        };
    }

    /**
     * Registra métricas personalizadas en Micrometer después de la inicialización
     */
    @EventListener
    public void registerCustomMetrics(org.springframework.boot.context.event.ApplicationReadyEvent event) {
        try {
            MeterRegistry meterRegistry = event.getApplicationContext().getBean(MeterRegistry.class);
        
        // Registrar Gauges simplificados usando funciones lambda
        meterRegistry.gauge("admission.applications.total", applicationRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Error obteniendo total de aplicaciones", e);
                return 0.0;
            }
        });

        meterRegistry.gauge("admission.users.total", userRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Error obteniendo total de usuarios", e);
                return 0.0;
            }
        });

        meterRegistry.gauge("admission.evaluations.total", evaluationRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Error obteniendo total de evaluaciones", e);
                return 0.0;
            }
        });

        meterRegistry.gauge("admission.interviews.total", interviewRepository, repo -> {
            try {
                return repo.count();
            } catch (Exception e) {
                log.error("Error obteniendo total de entrevistas", e);
                return 0.0;
            }
        });

        // Crear Timers para operaciones críticas del sistema
        meterRegistry.timer("admission.workflow.execution", 
            "description", "Tiempo de ejecución de workflows automáticos");

        meterRegistry.timer("admission.email.sending", 
            "description", "Tiempo de envío de emails");

        meterRegistry.timer("admission.evaluation.processing", 
            "description", "Tiempo de procesamiento de evaluaciones");

        // Crear Contadores para eventos de negocio
        meterRegistry.counter("admission.applications.created", 
            "description", "Número total de aplicaciones creadas");

        meterRegistry.counter("admission.interviews.completed", 
            "description", "Número total de entrevistas completadas");

        meterRegistry.counter("admission.evaluations.assigned", 
            "description", "Número total de evaluaciones asignadas");

        meterRegistry.counter("admission.notifications.sent", 
            "description", "Número total de notificaciones enviadas");

        meterRegistry.counter("admission.workflow.transitions", 
            "description", "Número total de transiciones de estado ejecutadas");

            log.info("✅ Métricas personalizadas del sistema de admisión registradas correctamente");
        } catch (Exception e) {
            log.error("Error registrando métricas personalizadas", e);
        }
    }
}