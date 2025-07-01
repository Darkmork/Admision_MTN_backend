# Configuración de Email para RobotCode Arena

## Configuración de Gmail

Para que funcione el envío de emails de verificación, necesitas configurar un email de Gmail:

### 1. Crear cuenta de Gmail
- Crea una cuenta específica para la aplicación (ej: robotcode.mtn@gmail.com)
- O usa una cuenta existente

### 2. Habilitar verificación en 2 pasos
1. Ve a https://myaccount.google.com/security
2. Habilita "Verificación en 2 pasos"

### 3. Generar contraseña de aplicación
1. Ve a https://myaccount.google.com/apppasswords
2. Selecciona "Otra (nombre personalizado)"
3. Escribe "RobotCode Arena"
4. Copia la contraseña generada (16 caracteres)

### 4. Configurar variables de entorno

#### Opción A: Variables de entorno del sistema
```bash
export EMAIL_USERNAME=tu-email@gmail.com
export EMAIL_PASSWORD=tu-contrasena-de-aplicacion
export EMAIL_FROM=tu-email@gmail.com
```

#### Opción B: Archivo .env (recomendado para desarrollo)
Crea un archivo `.env` en la raíz del proyecto backend:
```
EMAIL_USERNAME=tu-email@gmail.com
EMAIL_PASSWORD=tu-contrasena-de-aplicacion
EMAIL_FROM=tu-email@gmail.com
```

### 5. Configuración alternativa (modificar application.properties)
Si prefieres hardcodear temporalmente para desarrollo, modifica `application.properties`:
```properties
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-contrasena-de-aplicacion
spring.mail.from=tu-email@gmail.com
```

## Configuración para otros proveedores de email

### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=tu-email@outlook.com
spring.mail.password=tu-contrasena
```

### Yahoo
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=tu-email@yahoo.com
spring.mail.password=tu-contrasena
```

## Prueba de funcionamiento

Una vez configurado, puedes probar el sistema:

1. Inicia la aplicación backend
2. Ve al frontend y registra un usuario con email @mtn.cl o @alumnos.mtn.cl
3. Deberías recibir un email con el código de verificación
4. Ingresa el código para completar el registro

## Solución de problemas

### Error: "Authentication failed"
- Verifica que hayas habilitado la verificación en 2 pasos
- Asegúrate de usar la contraseña de aplicación, no tu contraseña normal
- Revisa que el usuario y contraseña estén correctos

### Error: "Mail server connection failed"
- Verifica tu conexión a internet
- Asegúrate de que el puerto 587 no esté bloqueado por tu firewall
- Prueba con puerto 465 si 587 no funciona

### Email no llega
- Revisa la carpeta de spam/correo no deseado
- Verifica que el email de destino sea válido
- Revisa los logs de la aplicación para errores

## Logs útiles

Para debugear problemas de email, revisa estos logs:
```
2024-07-01 10:30:00 INFO  EmailService - Verification email sent successfully to: usuario@mtn.cl
2024-07-01 10:30:00 ERROR EmailService - Failed to send verification email to: usuario@mtn.cl
```