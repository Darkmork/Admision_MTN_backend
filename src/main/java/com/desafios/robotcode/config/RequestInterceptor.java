package com.desafios.robotcode.config;

import com.desafios.robotcode.service.HeartbeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);
    
    @Autowired
    private HeartbeatService heartbeatService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Registrar la petición en el heartbeat service
        heartbeatService.recordRequest();
        
        // Log de petición (solo en debug)
        logger.debug("📥 Petición recibida: {} {} - {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            LocalDateTime.now());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Log de respuesta (solo en debug)
        logger.debug("📤 Respuesta enviada: {} {} - Status: {} - {}", 
            request.getMethod(), 
            request.getRequestURI(), 
            response.getStatus(),
            LocalDateTime.now());
    }
} 