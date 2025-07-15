package com.desafios.robotcode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

@Component
public class ApplicationStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        
        logger.info("🚀 RobotCode Backend iniciado exitosamente!");
        logger.info("📋 Configuración de la aplicación:");
        logger.info("   - Puerto: {}", env.getProperty("server.port", "8080"));
        logger.info("   - Dirección: {}", env.getProperty("server.address", "0.0.0.0"));
        logger.info("   - Perfil activo: {}", env.getProperty("spring.profiles.active", "default"));
        logger.info("   - Base de datos: {}", env.getProperty("spring.datasource.url", "no configurada"));
        
        logger.info("🔗 Endpoints disponibles:");
        logger.info("   - Health check: http://localhost:{}/health", env.getProperty("server.port", "8080"));
        logger.info("   - API root: http://localhost:{}/api/", env.getProperty("server.port", "8080"));
        logger.info("   - Ping: http://localhost:{}/ping", env.getProperty("server.port", "8080"));
        logger.info("   - Ready: http://localhost:{}/ready", env.getProperty("server.port", "8080"));
        
        logger.info("✅ Aplicación lista para recibir requests!");
    }
} 