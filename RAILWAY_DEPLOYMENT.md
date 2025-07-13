# 🚀 Despliegue en Railway - RobotCode Backend

## Configuración Automática

Este proyecto está configurado para desplegarse automáticamente en Railway con todas las variables de entorno necesarias.

## Variables de Entorno Configuradas

### Base de Datos (Automática)
- `DATABASE_URL`: Proporcionada automáticamente por Railway
- `DB_USERNAME`: postgres (por defecto)
- `DB_PASSWORD`: postgres (por defecto)

### Email (Gmail)
- `EMAIL_USERNAME`: jorge.gangale@mtn.cl
- `EMAIL_PASSWORD`: eiyv rmur unbh jynh
- `EMAIL_FROM`: jorge.gangale@mtn.cl

### API Keys
- `DEEPSEEK_API_KEY`: sk-0834832a545645e88bf30b5e4327777b

### Configuración de la Aplicación
- `SPRING_PROFILES_ACTIVE`: prod
- `APP_BASE_URL`: https://robotcode-arena.vercel.app
- `CORS_ORIGINS`: https://robotcode-arena.vercel.app,https://robotcode-arena.vercel.app/*

## Pasos para Desplegar

1. **Ir a Railway.com** y crear un nuevo proyecto
2. **Conectar el repositorio** `Darkmork/robotcode`
3. **Agregar servicio de base de datos:**
   - New Service → Database → PostgreSQL
4. **Agregar servicio de backend:**
   - New Service → GitHub Repo
   - Root Directory: `RobotCode backend`
   - Build Command: `./mvnw clean install -DskipTests`
   - Start Command: `./mvnw spring-boot:run -Dspring.profiles.active=prod`

## Endpoints Disponibles

- **Health Check:** `GET /api/health`
- **API Root:** `GET /api/`
- **Problemas:** `GET /api/problemas/*`
- **Usuarios:** `GET /api/usuarios/*`
- **Progreso:** `GET /api/progresos/*`
- **Ranking:** `GET /api/ranking`
- **Robotom:** `POST /api/robotom/chat`

## Migración de Datos

Los datos se migrarán automáticamente al iniciar la aplicación con:
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.sql.init.mode=always`

## Monitoreo

- **Health Check:** Railway verificará automáticamente `/api/health`
- **Logs:** Disponibles en el dashboard de Railway
- **Métricas:** Railway proporciona métricas automáticas

## Conexión con Frontend

El backend está configurado para aceptar conexiones desde:
- `https://robotcode-arena.vercel.app`
- `http://localhost:5173` (desarrollo)

## Troubleshooting

### Si la aplicación no inicia:
1. Verificar que la base de datos esté conectada
2. Revisar los logs en Railway
3. Verificar que las variables de entorno estén configuradas

### Si hay errores de CORS:
1. Verificar que `CORS_ORIGINS` incluya la URL del frontend
2. Reiniciar el servicio después de cambiar variables

### Si la base de datos no se conecta:
1. Verificar que el servicio de PostgreSQL esté activo
2. Verificar que `DATABASE_URL` esté configurada correctamente 