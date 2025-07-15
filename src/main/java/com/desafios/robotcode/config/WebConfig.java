package com.desafios.robotcode.config;

import com.desafios.robotcode.service.HeartbeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/**") // Aplicar a todas las rutas
                .excludePathPatterns("/health", "/ping", "/ready"); // Excluir endpoints de healthcheck
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 