package com.desafios.mtn.evaluationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Aplicación principal del microservicio de evaluaciones
 * 
 * Este microservicio maneja:
 * - Evaluaciones académicas y psicológicas
 * - Entrevistas de admisión
 * - Asignación automática de evaluadores
 * - Gestión de SLAs y métricas
 * - Eventos de dominio con patrón Outbox
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableTransactionManagement
public class EvaluationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvaluationServiceApplication.class, args);
    }
}