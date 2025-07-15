# Solución para Healthcheck de Railway

## Problema
Railway no puede acceder al endpoint `/api/health` y falla el healthcheck.

## Soluciones Implementadas

### 1. Endpoints de Healthcheck Múltiples
Se han creado varios endpoints para asegurar que Railway pueda hacer healthcheck:

- `/health` - Endpoint principal de healthcheck
- `/ping` - Endpoint simple que responde "pong"
- `/ready` - Endpoint de readiness
- `/api/health` - Endpoint original de la API
- `/api/ping` - Ping de la API
- `/api/status` - Status detallado de la API

### 2. Configuración de Railway
Archivo `railway.json` actualizado:
```json
{
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "./mvnw clean install -DskipTests"
  },
  "deploy": {
    "startCommand": "./start.sh",
    "healthcheckPath": "/health",
    "healthcheckTimeout": 300,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

### 3. Script de Inicio Personalizado
Script `start.sh` que:
- Verifica la compilación
- Configura variables de entorno
- Inicia la aplicación con logging detallado

### 4. Logging Mejorado
- Logging específico para healthcheck
- Configuración de logback para producción
- Listener de aplicación para debug

## Pasos para Deploy

1. **Commit y Push de los cambios**
```bash
git add .
git commit -m "Fix Railway healthcheck - multiple endpoints and improved logging"
git push
```

2. **Verificar Variables de Entorno en Railway**
Asegúrate de que estas variables estén configuradas:
- `SPRING_PROFILES_ACTIVE=prod`
- `PORT` (Railway lo proporciona automáticamente)
- `DATABASE_URL` (Railway lo proporciona automáticamente)

3. **Monitorear los Logs**
En Railway, revisa los logs para ver:
- Si la aplicación inicia correctamente
- Si los endpoints de healthcheck responden
- Cualquier error de configuración

## Endpoints de Prueba

Una vez desplegado, puedes probar estos endpoints:

```bash
# Healthcheck principal
curl https://tu-app.railway.app/health

# Ping simple
curl https://tu-app.railway.app/ping

# Status detallado
curl https://tu-app.railway.app/api/status

# Root endpoint
curl https://tu-app.railway.app/
```

## Troubleshooting

### Si el healthcheck sigue fallando:

1. **Verificar logs en Railway**
   - Revisa si la aplicación inicia correctamente
   - Busca errores de base de datos
   - Verifica que el puerto esté configurado correctamente

2. **Probar endpoints manualmente**
   - Usa curl o Postman para probar los endpoints
   - Verifica que respondan con 200 OK

3. **Verificar configuración de base de datos**
   - Asegúrate de que `DATABASE_URL` esté configurada
   - Verifica que la base de datos esté accesible

4. **Revisar variables de entorno**
   - Confirma que `SPRING_PROFILES_ACTIVE=prod`
   - Verifica que no haya conflictos de puertos

### Logs Esperados

Al iniciar correctamente, deberías ver:
```
🚀 RobotCode Backend iniciado exitosamente!
📋 Configuración de la aplicación:
   - Puerto: 8080
   - Dirección: 0.0.0.0
   - Perfil activo: prod
🔗 Endpoints disponibles:
   - Health check: http://localhost:8080/health
   - API root: http://localhost:8080/api/
✅ Aplicación lista para recibir requests!
```

## Notas Importantes

- El healthcheck ahora usa `/health` en lugar de `/api/health`
- Se han agregado múltiples endpoints de respaldo
- El logging está configurado para debug en producción
- El script de inicio maneja tanto JAR como Maven 