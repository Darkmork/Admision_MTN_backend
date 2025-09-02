# E2E API Tests - Sistema de Admisión MTN

## Descripción

Pruebas End-to-End (E2E) para los endpoints críticos del API REST del Sistema de Admisión MTN usando REST Assured.

## Estructura de Pruebas

### Flujos Críticos Implementados

1. **b1) Login** - `AuthenticationE2ETest.java`
   - Login exitoso como administrador, apoderado y profesor
   - Validación de tokens JWT
   - Manejo de errores de autenticación
   - Acceso a endpoints protegidos

2. **b2) Crear Postulación** - `ApplicationE2ETest.java`
   - Crear postulación como apoderado
   - Obtener postulación por ID
   - Actualizar postulación
   - Cambio de estado por admin
   - Validaciones y errores

3. **b3) Subir Documento** - `DocumentUploadE2ETest.java`
   - Upload de archivos PDF e imágenes
   - Descargar documentos subidos
   - Obtener tipos de documentos
   - Eliminar documentos
   - Validaciones de archivos

4. **b4) Notificaciones** - `NotificationE2ETest.java`
   - Envío de emails básicos
   - Emails institucionales (postulación recibida, cambio estado, entrevista)
   - Obtener plantillas de email
   - Sistema de notificaciones
   - Estadísticas de emails

### Clases de Soporte

- **BaseE2ETest.java** - Clase base con configuración común
  - Setup de REST Assured
  - Métodos de login para diferentes roles
  - Configuración de base de datos de prueba (H2)
  - Utilidades comunes

## Configuración

### Variables de Entorno

```bash
# Opcional - por defecto usa puerto aleatorio
SERVER_PORT=8080

# Base de datos de prueba (usa H2 en memoria)
SPRING_PROFILES_ACTIVE=test
```

### Credenciales de Prueba

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Admin | `admin@mtn.cl` | `admin123` | ADMIN |
| Apoderado | `familia01@test.cl` | `secret` | APODERADO |
| Profesor | `maria.nueva@mtn.cl` | `secret` | TEACHER |
| Psicóloga | `psicologa@test.cl` | `secret` | PSYCHOLOGIST |
| Director | `director@test.cl` | `secret` | CYCLE_DIRECTOR |

## Ejecución

### Ejecutar Todas las Pruebas E2E

```bash
# Desde el directorio raíz del backend
mvn test -Dtest="e2e.**"
```

### Ejecutar Pruebas Específicas

```bash
# Solo pruebas de autenticación
mvn test -Dtest="e2e.AuthenticationE2ETest"

# Solo pruebas de postulaciones
mvn test -Dtest="e2e.ApplicationE2ETest"

# Solo pruebas de documentos
mvn test -Dtest="e2e.DocumentUploadE2ETest"

# Solo pruebas de notificaciones
mvn test -Dtest="e2e.NotificationE2ETest"
```

### Ejecutar con Perfil de Prueba

```bash
mvn test -Dspring.profiles.active=test -Dtest="e2e.**"
```

### Generar Reportes Detallados

```bash
mvn surefire-report:report -Dtest="e2e.**"
# Reporte en: target/site/surefire-report.html
```

## Configuración de Base de Datos

Las pruebas usan H2 en memoria con la configuración:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

### Datos de Prueba

Los usuarios de prueba se cargan automáticamente desde:
- `src/test/resources/e2e-fixtures/test-users.sql`

## Características de las Pruebas

### Validaciones Implementadas

1. **Códigos de Estado HTTP** - 200, 201, 400, 401, 403, 404
2. **Estructura de Respuesta JSON** - Validación de campos requeridos
3. **Autenticación JWT** - Validación de tokens y permisos
4. **Tiempos de Respuesta** - Límites de performance (< 5 segundos)
5. **Datos de Negocio** - Validación de lógica específica del dominio

### Patrones de Prueba

```java
// Ejemplo de estructura típica
given()
    .header("Authorization", authHeader(token))
    .body(requestPayload)
.when()
    .post("/api/endpoint")
.then()
    .statusCode(201)
    .body("id", notNullValue())
    .body("status", equalTo("EXPECTED_STATUS"))
    .time(lessThan(3000L));
```

### Manejo de Errores

- **401 Unauthorized** - Sin token o token inválido
- **403 Forbidden** - Token válido pero sin permisos
- **400 Bad Request** - Datos de entrada inválidos
- **404 Not Found** - Recurso no encontrado

## Integración con CI/CD

### GitHub Actions

```yaml
- name: Run E2E API Tests
  run: |
    mvn test -Dtest="e2e.**" -Dspring.profiles.active=test
```

### Maven Surefire Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*E2ETest.java</include>
        </includes>
        <groups>e2e</groups>
    </configuration>
</plugin>
```

## Troubleshooting

### Problemas Comunes

1. **Puerto ya en uso**
   ```bash
   lsof -ti:8080 | xargs kill -9
   ```

2. **Base de datos no se crea**
   - Verificar que H2 esté en el classpath
   - Revisar configuración de `application-test.yml`

3. **Tokens JWT inválidos**
   - Verificar que los usuarios de prueba existan
   - Validar configuración del JWT secret

4. **Archivos de prueba no se encuentran**
   - Los archivos temporales se crean automáticamente
   - Verificar permisos de escritura en `/tmp`

### Logs de Debugging

```bash
# Habilitar logs detallados
mvn test -Dtest="e2e.**" -Dlogging.level.io.restassured=DEBUG
```

### Verificar Estado del API

```bash
# Health check
curl http://localhost:8080/actuator/health

# OpenAPI docs
curl http://localhost:8080/v3/api-docs
```

## Métricas de Pruebas

### Objetivos de Performance

- **Tiempo de respuesta promedio**: < 2 segundos
- **Tiempo máximo aceptable**: < 5 segundos
- **Tasa de éxito**: > 98%

### Coverage de Endpoints

- **Autenticación**: 4 endpoints críticos
- **Postulaciones**: 6 operaciones principales  
- **Documentos**: 5 operaciones de archivos
- **Notificaciones**: 8 tipos de emails

## Próximos Pasos

1. **Agregar más flujos E2E**:
   - Evaluaciones académicas
   - Entrevistas y programación
   - Workflow completo de admisión

2. **Integración con TestContainers**:
   - PostgreSQL real en pruebas
   - Redis para cache testing

3. **Pruebas de Carga**:
   - JMeter integration
   - Stress testing de endpoints críticos

---

**Generado para Fase 0 Pre-flight**  
**Fecha:** $(date '+%Y-%m-%d %H:%M:%S')  
**Sistema:** Admisión MTN v1.0.0