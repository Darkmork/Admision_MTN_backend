package com.desafios.mtn.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Aplicación principal del Notification Service
 * 
 * Microservicio de notificaciones para el Sistema de Admisión MTN
 * 
 * Características:
 * - Procesamiento de eventos EmailRequested.v1 y SmsRequested.v1
 * - Sistema de plantillas con Mustache/Handlebars
 * - Rate limiting y control de spam
 * - Retry automático con backoff exponencial (5 niveles)
 * - Dead Letter Queue para mensajes fallidos
 * - Métricas y observabilidad con Prometheus/OpenTelemetry
 * - Seguridad OAuth2 Resource Server
 * - Soporte para modo mock (desarrollo) y real (producción)
 * - Idempotencia con ventana de 5 minutos
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableScheduling
@EnableEurekaServer
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}