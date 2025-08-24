package com.desafios.admision_mtn.config;

import com.desafios.admision_mtn.filter.JwtRequestFilter;
import com.desafios.admision_mtn.security.RateLimitingFilter;
import com.desafios.admision_mtn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final RateLimitingFilter rateLimitingFilter;
    
    @Value("${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // 🔒 ENDPOINTS PÚBLICOS ESENCIALES
                .requestMatchers("/api/auth/**").permitAll() // Login, registro
                .requestMatchers("/api/rut/**").permitAll() // Validación RUT chileno
                .requestMatchers("/api/documents/public/types").permitAll() // Solo tipos de documentos
                
                // 📚 DOCUMENTACIÓN API SWAGGER/OpenAPI
                .requestMatchers("/swagger-ui/**").permitAll() // Swagger UI
                .requestMatchers("/swagger-ui.html").permitAll() // Swagger UI HTML
                .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI JSON/YAML
                .requestMatchers("/api-docs/**").permitAll() // Documentos API
                .requestMatchers("/swagger-resources/**").permitAll() // Recursos Swagger
                
                // 📊 MONITOREO Y OBSERVABILIDAD - Actuator
                .requestMatchers("/actuator/health").permitAll() // Health check público
                .requestMatchers("/actuator/info").permitAll() // Información básica
                .requestMatchers("/actuator/prometheus").permitAll() // Métricas Prometheus
                .requestMatchers("/actuator/**").hasRole("ADMIN") // Otros endpoints solo para admins
                
                // 🚨 ENDPOINTS REMOVIDOS POR SEGURIDAD:
                // - /api/test/** (expone contraseñas y datos sensibles)
                // - /api/debug/** (acceso directo a BD y usuarios)
                // - /api/public/** (demasiado genérico)
                // - /api/applications/public/** (expone aplicaciones sin autenticación)
                // - /api/documents/public/** (acceso a documentos privados)
                // - /api/schedules/public/** (horarios privados)
                // - /api/evaluations/public/** (evaluaciones confidenciales)
                // - /api/email/** y /api/emails/** (no encontrados en controllers)
                // - /api/usuario-auth/** (no encontrado en controllers)
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 🔒 SEGURIDAD: Orígenes específicos desde configuración  
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        
        // 🔒 SEGURIDAD: Solo métodos HTTP necesarios
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 🔒 SEGURIDAD: Headers específicos necesarios para JWT y contenido
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 🔒 SEGURIDAD: Headers expuestos para el frontend
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // 🔒 SEGURIDAD: Permitir credenciales solo para orígenes específicos
        configuration.setAllowCredentials(true);
        
        // 🔒 SEGURIDAD: Cache de preflight por 1 hora para performance
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}