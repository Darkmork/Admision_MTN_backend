package e2e;

import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * E2E tests for notification endpoints
 * Flujo b4) Notificar: invocar endpoint que dispare notificación → verificar 202/registro
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationE2ETest extends BaseE2ETest {
    
    @Test
    @Order(1)
    @DisplayName("b4) Enviar email básico como admin")
    void testSendBasicEmailAsAdmin() {
        String adminToken = loginAsAdmin();
        
        String emailPayload = """
            {
                "to": "test@example.com",
                "subject": "Prueba E2E - Email básico",
                "content": "Este es un email de prueba enviado desde las pruebas E2E del sistema de admisión MTN.",
                "type": "TEXT"
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(emailPayload)
        .when()
            .post("/api/emails/send")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("timestamp", notNullValue())
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(2)
    @DisplayName("b4) Enviar email institucional - Postulación recibida")
    void testSendInstitutionalEmailApplicationReceived() {
        String adminToken = loginAsAdmin();
        
        String emailPayload = """
            {
                "recipientEmail": "familia@test.com",
                "recipientName": "María López",
                "studentName": "Juan Pérez",
                "applicationId": "12345",
                "submissionDate": "2024-01-15",
                "targetSchool": "Monte Tabor",
                "grade": "Kinder"
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(emailPayload)
        .when()
            .post("/api/institutional-emails/application-received")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("timestamp", notNullValue())
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(3)
    @DisplayName("b4) Enviar email institucional - Actualización de estado")
    void testSendInstitutionalEmailStatusUpdate() {
        String adminToken = loginAsAdmin();
        
        String emailPayload = """
            {
                "recipientEmail": "familia@test.com",
                "recipientName": "María López",
                "studentName": "Juan Pérez",
                "applicationId": "12345",
                "previousStatus": "PENDING",
                "newStatus": "UNDER_REVIEW",
                "statusMessage": "Su postulación ha pasado a revisión por nuestro equipo académico.",
                "nextSteps": "En los próximos días nos pondremos en contacto para coordinar la entrevista."
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(emailPayload)
        .when()
            .post("/api/institutional-emails/status-update")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("timestamp", notNullValue())
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(4)
    @DisplayName("b4) Enviar email institucional - Invitación a entrevista")
    void testSendInstitutionalEmailInterviewInvitation() {
        String adminToken = loginAsAdmin();
        
        String emailPayload = """
            {
                "recipientEmail": "familia@test.com",
                "recipientName": "María López",
                "studentName": "Juan Pérez",
                "interviewDate": "2024-02-15",
                "interviewTime": "10:00",
                "interviewLocation": "Oficina de Admisiones - Monte Tabor",
                "interviewerName": "Directora Ana Martínez",
                "confirmationToken": "abc123def456",
                "rescheduleToken": "xyz789uvw123",
                "instructions": "Por favor llegue 15 minutos antes con la documentación solicitada."
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(emailPayload)
        .when()
            .post("/api/institutional-emails/interview-invitation")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("timestamp", notNullValue())
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(5)
    @DisplayName("b4) Enviar email institucional - Recordatorio de documentos")
    void testSendInstitutionalEmailDocumentReminder() {
        String adminToken = loginAsAdmin();
        
        String emailPayload = """
            {
                "recipientEmail": "familia@test.com",
                "recipientName": "María López",
                "studentName": "Juan Pérez",
                "applicationId": "12345",
                "missingDocuments": [
                    "Certificado de nacimiento",
                    "Informe de notas del colegio anterior"
                ],
                "deadline": "2024-02-01",
                "uploadInstructions": "Puede subir los documentos desde su panel de usuario en nuestro sitio web."
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(emailPayload)
        .when()
            .post("/api/institutional-emails/document-reminder")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("timestamp", notNullValue())
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(6)
    @DisplayName("b4) Obtener plantillas de email disponibles")
    void testGetEmailTemplates() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/emails/templates")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].id", notNullValue())
            .body("[0].name", notNullValue())
            .body("[0].subject", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(7)
    @DisplayName("b4) Obtener plantilla institucional específica")
    void testGetInstitutionalTemplate() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/institutional-emails/templates/APPLICATION_RECEIVED")
        .then()
            .statusCode(200)
            .body("type", equalTo("APPLICATION_RECEIVED"))
            .body("subject", notNullValue())
            .body("htmlContent", notNullValue())
            .body("variables", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(8)
    @DisplayName("b4) Obtener estado del sistema de email")
    void testGetEmailSystemStatus() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/admin/email-management/status")
        .then()
            .statusCode(200)
            .body("emailSystemActive", notNullValue())
            .body("mockMode", equalTo(true)) // Should be in mock mode for tests
            .body("smtpConfiguration", notNullValue())
            .body("lastEmailSent", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(9)
    @DisplayName("b4) Enviar email de prueba")
    void testSendTestEmail() {
        String adminToken = loginAsAdmin();
        
        String testEmailPayload = """
            {
                "recipientEmail": "test@example.com",
                "testType": "SYSTEM_CHECK"
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(testEmailPayload)
        .when()
            .post("/api/admin/email-management/test")
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("enviado"))
            .body("success", equalTo(true))
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(10)
    @DisplayName("b4) Obtener estadísticas de emails")
    void testGetEmailStatistics() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/admin/email-management/statistics")
        .then()
            .statusCode(200)
            .body("totalEmailsSent", greaterThanOrEqualTo(0))
            .body("emailsSentToday", greaterThanOrEqualTo(0))
            .body("emailsByType", notNullValue())
            .body("deliveryRate", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(11)
    @DisplayName("b4) Error al enviar email sin autenticación")
    void testSendEmailUnauthorized() {
        String emailPayload = """
            {
                "to": "test@example.com",
                "subject": "Test",
                "content": "Test content"
            }
            """;
        
        given()
            .body(emailPayload)
        .when()
            .post("/api/emails/send")
        .then()
            .statusCode(401)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(12)
    @DisplayName("b4) Error al enviar email con datos inválidos")
    void testSendEmailInvalidData() {
        String adminToken = loginAsAdmin();
        
        String invalidEmailPayload = """
            {
                "to": "invalid-email",
                "subject": "",
                "content": ""
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(invalidEmailPayload)
        .when()
            .post("/api/emails/send")
        .then()
            .statusCode(400)
            .body("message", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(13)
    @DisplayName("b4) Usuario sin permisos no puede enviar emails")
    void testSendEmailForbidden() {
        String apoderadoToken = loginAsApoderado();
        
        String emailPayload = """
            {
                "to": "test@example.com",
                "subject": "Test",
                "content": "Test content"
            }
            """;
        
        given()
            .header("Authorization", authHeader(apoderadoToken))
            .body(emailPayload)
        .when()
            .post("/api/emails/send")
        .then()
            .statusCode(403)
            .time(lessThan(2000L));
    }
}