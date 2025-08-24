package com.desafios.admision_mtn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación interactiva de APIs
 * 
 * Genera documentación automática para todos los endpoints del sistema de admisión
 * incluyendo autenticación JWT, ejemplos de uso y esquemas de datos.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Servidor de desarrollo local"),
                    new Server()
                        .url("https://api-admision.mtn.cl")
                        .description("Servidor de producción (cuando esté disponible)")
                ))
                .components(new Components()
                    .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT obtenido del endpoint /api/auth/login"))
                    .addSecuritySchemes("professorAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT para profesores obtenido del endpoint /api/auth/professor-login"))
                )
                .security(List.of(
                    new SecurityRequirement().addList("bearerAuth"),
                    new SecurityRequirement().addList("professorAuth")
                ))
                .tags(List.of(
                    new Tag().name("Authentication").description("Endpoints de autenticación y autorización"),
                    new Tag().name("Applications").description("Gestión de postulaciones de admisión"),
                    new Tag().name("Users").description("Gestión de usuarios del sistema"),
                    new Tag().name("Students").description("Gestión de información de estudiantes"),
                    new Tag().name("Interviews").description("Sistema de entrevistas del proceso de admisión"),
                    new Tag().name("Evaluations").description("Sistema de evaluaciones académicas y psicológicas"),
                    new Tag().name("Documents").description("Gestión de documentos y archivos"),
                    new Tag().name("Notifications").description("Sistema de notificaciones automatizadas"),
                    new Tag().name("Dashboard").description("Dashboards y reportes administrativos"),
                    new Tag().name("Workflow").description("Flujo automático de estados y transiciones"),
                    new Tag().name("Validation").description("Validaciones de transiciones de estado"),
                    new Tag().name("Analytics").description("Análisis y métricas del sistema")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("API Sistema de Admisión MTN")
                .description("""
                    # Sistema de Admisión - Colegio Monte Tabor y Nazaret
                    
                    API REST completa para la gestión del proceso de admisión escolar.
                    
                    ## Características Principales
                    
                    ### 🔐 Autenticación Dual
                    - **Autenticación Regular**: Para administradores y apoderados
                    - **Autenticación de Profesores**: Sistema separado para evaluadores
                    
                    ### 👥 Roles de Usuario
                    - **ADMIN**: Administradores del sistema con acceso completo
                    - **CYCLE_DIRECTOR**: Directores de ciclo para entrevistas y evaluaciones
                    - **PSYCHOLOGIST**: Psicólogos para evaluaciones especializadas
                    - **TEACHER_LANGUAGE**: Profesores de lenguaje
                    - **TEACHER_MATHEMATICS**: Profesores de matemáticas  
                    - **TEACHER_ENGLISH**: Profesores de inglés
                    - **APODERADO**: Padres/apoderados que postulan estudiantes
                    
                    ### 🔄 Flujo de Estados de Aplicaciones
                    ```
                    PENDING → UNDER_REVIEW → INTERVIEW_SCHEDULED → EXAM_SCHEDULED → APPROVED/REJECTED/WAITLIST
                    ```
                    
                    ### 🚀 Funcionalidades Avanzadas
                    - ✅ Workflow automático de estados
                    - ✅ Sistema de notificaciones por email
                    - ✅ Gestión completa de entrevistas
                    - ✅ Dashboards y reportes administrativos
                    - ✅ Validaciones de transiciones de estado
                    - ✅ Sistema de evaluaciones multi-tipo
                    - ✅ Gestión de documentos y archivos
                    
                    ## Autenticación
                    
                    ### Para Administradores y Apoderados:
                    ```http
                    POST /api/auth/login
                    Content-Type: application/json
                    
                    {
                        "email": "admin@mtn.cl",
                        "password": "password"
                    }
                    ```
                    
                    ### Para Profesores:
                    ```http
                    POST /api/auth/professor-login  
                    Content-Type: application/json
                    
                    {
                        "email": "profesor@mtn.cl",
                        "password": "password"
                    }
                    ```
                    
                    Ambos endpoints retornan un token JWT que debe incluirse en el header Authorization:
                    ```
                    Authorization: Bearer <jwt-token>
                    ```
                    
                    ## Códigos de Estado
                    - **200**: Operación exitosa
                    - **201**: Recurso creado exitosamente
                    - **400**: Error en la petición (datos inválidos)
                    - **401**: No autenticado (token faltante o inválido)
                    - **403**: No autorizado (sin permisos para la operación)
                    - **404**: Recurso no encontrado
                    - **409**: Conflicto (ej: email ya existe)
                    - **500**: Error interno del servidor
                    
                    ## Versionado
                    Actualmente en versión 1.0.0. Cambios futuros mantendrán compatibilidad hacia atrás.
                    
                    ## Soporte
                    Para soporte técnico, contactar al equipo de desarrollo.
                    """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Equipo de Desarrollo MTN")
                        .email("desarrollo@mtn.cl")
                        .url("https://www.mtn.cl"))
                .license(new License()
                        .name("Licencia Propietaria")
                        .url("https://www.mtn.cl/licencia"));
    }
}