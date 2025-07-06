# 📧 Solución de Problemas del Email

## 🔍 Diagnóstico Actual

**Estado:** El formulario de contacto funciona técnicamente, pero los emails no llegan al destinatario.

**Backend responde:** ✅ Endpoint `/api/contact/send` retorna success  
**Configuración:** ✅ Gmail SMTP configurado  
**Problema:** ❌ Emails no llegan a `contacto@jgangale.cl`

## 🛠️ Pasos para Solucionar

### 1. **Verificar configuración de Gmail**

#### A. Revisar App Password:
- Ve a [Google Account Settings](https://myaccount.google.com/)
- Busca "App passwords" o "Contraseñas de aplicaciones"
- Genera una nueva App Password para "Mail"
- Reemplaza `eiyv rmur unbh jynh` en `application.properties`

#### B. Verificar autenticación en dos pasos:
- Debe estar **HABILITADA** para usar App Passwords
- Si no está habilitada, habilitarla primero

### 2. **Revisar logs del backend**

```bash
# En el directorio del backend, ejecutar:
tail -f logs/spring.log

# O revisar logs en tiempo real:
./mvnw spring-boot:run | grep -i mail
```

### 3. **Verificar carpeta de spam**
- Revisar bandeja de spam de `contacto@jgangale.cl`
- Los correos automáticos a veces van a spam inicialmente

### 4. **Probar con email de prueba**

Cambiar temporalmente en `application.properties`:
```properties
# Cambiar el destinatario para pruebas
# En EmailService.java línea 134, cambiar:
# message.setTo("contacto@jgangale.cl");
# por:
# message.setTo("jorge.gangale@mtn.cl"); // Email que sí existe
```

### 5. **Configuración alternativa: Usar otro proveedor**

Si Gmail sigue fallando, cambiar a otro proveedor SMTP:

#### Opción A: Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=tu-email@outlook.com
spring.mail.password=tu-password
```

#### Opción B: SendGrid (recomendado para producción)
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=tu-sendgrid-api-key
```

### 6. **Verificar firewall/proxy**
- Algunos firewalls bloquean SMTP saliente (puerto 587)
- Verificar si hay proxy corporativo

### 7. **Test rápido con código**

Crear un endpoint de prueba simple:

```java
@GetMapping("/test-email")
public ResponseEntity<String> testEmail() {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("jorge.gangale@mtn.cl");
        message.setTo("jorge.gangale@mtn.cl"); // Mismo email
        message.setSubject("Test directo");
        message.setText("Este es un test directo del email.");
        
        mailSender.send(message);
        return ResponseEntity.ok("Email enviado exitosamente");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}
```

## 🚨 Errores Comunes

### Error 1: "Authentication failed"
**Causa:** App Password incorrecto o 2FA no habilitado  
**Solución:** Regenerar App Password con 2FA habilitado

### Error 2: "Connection timeout"
**Causa:** Puerto 587 bloqueado  
**Solución:** Probar puerto 465 con SSL

### Error 3: "Relay access denied"
**Causa:** Email de origen no autorizado  
**Solución:** Usar email que coincida con la cuenta Gmail

## ✅ Solución Rápida Recomendada

1. **Verificar que `contacto@jgangale.cl` existe y es accesible**
2. **Generar nuevo App Password en Gmail**
3. **Cambiar destinatario temporalmente a email conocido para testing**
4. **Revisar logs con debugging habilitado**

## 📞 ¿Siguiente paso?

Una vez aplicados estos pasos, ejecutar:
```bash
curl -X POST "http://localhost:8080/api/contact/send" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Debug",
    "email": "test@example.com",
    "subject": "Debug email",
    "message": "Test con debugging habilitado"
  }'
```

Y revisar logs detallados para ver exactamente dónde falla.