package e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * E2E tests for application management endpoints
 * Flujo b2) Crear postulación: POST /api/applications → recibir id
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApplicationE2ETest extends BaseE2ETest {
    
    private static Long createdApplicationId;
    
    @Test
    @Order(1)
    @DisplayName("b2) Crear postulación como apoderado")
    void testCreateApplicationSuccess() {
        String token = loginAsApoderado();
        
        String applicationPayload = """
            {
                "student": {
                    "firstName": "Juan Carlos",
                    "lastName": "Pérez López",
                    "rut": "20123456-7",
                    "birthDate": "2015-03-15",
                    "grade": "KINDER",
                    "previousSchool": "Jardín Los Pequeños",
                    "hasSpecialNeeds": false,
                    "medicalConditions": ""
                },
                "father": {
                    "firstName": "Carlos",
                    "lastName": "Pérez",
                    "rut": "12345678-9",
                    "email": "carlos.perez@email.com",
                    "phone": "+56912345678",
                    "occupation": "Ingeniero",
                    "workplace": "Empresa ABC"
                },
                "mother": {
                    "firstName": "María",
                    "lastName": "López",
                    "rut": "98765432-1",
                    "email": "maria.lopez@email.com",
                    "phone": "+56987654321",
                    "occupation": "Profesora",
                    "workplace": "Colegio XYZ"
                },
                "guardian": {
                    "firstName": "María",
                    "lastName": "López",
                    "rut": "98765432-1",
                    "relationship": "MOTHER",
                    "email": "maria.lopez@email.com",
                    "phone": "+56987654321",
                    "address": "Av. Providencia 1234, Santiago"
                },
                "supporter": {
                    "firstName": "Carlos",
                    "lastName": "Pérez",
                    "rut": "12345678-9",
                    "relationship": "FATHER",
                    "income": 2500000,
                    "occupation": "Ingeniero"
                },
                "targetSchool": "MONTE_TABOR",
                "comments": "Postulación de prueba E2E"
            }
            """;
        
        Response response = given()
            .header("Authorization", authHeader(token))
            .body(applicationPayload)
        .when()
            .post("/api/applications")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("PENDING"))
            .body("student.firstName", equalTo("Juan Carlos"))
            .body("student.lastName", equalTo("Pérez López"))
            .body("student.rut", equalTo("20123456-7"))
            .body("father.firstName", equalTo("Carlos"))
            .body("mother.firstName", equalTo("María"))
            .body("guardian.relationship", equalTo("MOTHER"))
            .body("supporter.relationship", equalTo("FATHER"))
            .body("targetSchool", equalTo("MONTE_TABOR"))
            .body("submissionDate", notNullValue())
            .body("createdAt", notNullValue())
            .time(lessThan(5000L))
            .extract().response();
        
        createdApplicationId = response.path("id");
        Assertions.assertNotNull(createdApplicationId, "Application ID should not be null");
    }
    
    @Test
    @Order(2)
    @DisplayName("b2) Obtener postulación creada por ID")
    void testGetApplicationById() {
        Assumptions.assumeTrue(createdApplicationId != null, "Application must be created first");
        
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/applications/" + createdApplicationId)
        .then()
            .statusCode(200)
            .body("id", equalTo(createdApplicationId.intValue()))
            .body("status", equalTo("PENDING"))
            .body("student.firstName", equalTo("Juan Carlos"))
            .body("student.grade", equalTo("KINDER"))
            .body("targetSchool", equalTo("MONTE_TABOR"))
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(3)
    @DisplayName("b2) Listar mis postulaciones como apoderado")
    void testGetMyApplications() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/applications/my-applications")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1))
            .body("[0].id", notNullValue())
            .body("[0].status", notNullValue())
            .body("[0].student", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(4)
    @DisplayName("b2) Actualizar postulación como apoderado")
    void testUpdateApplicationSuccess() {
        Assumptions.assumeTrue(createdApplicationId != null, "Application must be created first");
        
        String token = loginAsApoderado();
        
        String updatePayload = """
            {
                "student": {
                    "firstName": "Juan Carlos",
                    "lastName": "Pérez López",
                    "rut": "20123456-7",
                    "birthDate": "2015-03-15",
                    "grade": "KINDER",
                    "previousSchool": "Jardín Los Pequeños",
                    "hasSpecialNeeds": false,
                    "medicalConditions": "Actualizado en E2E"
                },
                "comments": "Comentarios actualizados en prueba E2E"
            }
            """;
        
        given()
            .header("Authorization", authHeader(token))
            .body(updatePayload)
        .when()
            .put("/api/applications/" + createdApplicationId)
        .then()
            .statusCode(200)
            .body("id", equalTo(createdApplicationId.intValue()))
            .body("student.medicalConditions", equalTo("Actualizado en E2E"))
            .body("comments", equalTo("Comentarios actualizados en prueba E2E"))
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(5)
    @DisplayName("b2) Admin puede ver todas las postulaciones")
    void testAdminGetAllApplications() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
            .queryParam("page", 0)
            .queryParam("size", 10)
        .when()
            .get("/api/applications")
        .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", greaterThanOrEqualTo(1))
            .body("totalPages", greaterThanOrEqualTo(1))
            .body("size", equalTo(10))
            .body("number", equalTo(0))
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(6)
    @DisplayName("b2) Admin puede cambiar estado de postulación")
    void testAdminUpdateApplicationStatus() {
        Assumptions.assumeTrue(createdApplicationId != null, "Application must be created first");
        
        String adminToken = loginAsAdmin();
        
        String statusUpdatePayload = """
            {
                "status": "UNDER_REVIEW",
                "comments": "Cambiado a revisión por prueba E2E"
            }
            """;
        
        given()
            .header("Authorization", authHeader(adminToken))
            .body(statusUpdatePayload)
        .when()
            .put("/api/applications/" + createdApplicationId + "/status")
        .then()
            .statusCode(200)
            .body("id", equalTo(createdApplicationId.intValue()))
            .body("status", equalTo("UNDER_REVIEW"))
            .body("updatedAt", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(7)
    @DisplayName("b2) Acceso denegado sin autenticación")
    void testCreateApplicationUnauthorized() {
        String applicationPayload = """
            {
                "student": {
                    "firstName": "Test",
                    "lastName": "Student",
                    "rut": "11111111-1"
                }
            }
            """;
        
        given()
            .body(applicationPayload)
        .when()
            .post("/api/applications")
        .then()
            .statusCode(401)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(8)
    @DisplayName("b2) Error de validación con datos incompletos")
    void testCreateApplicationValidationError() {
        String token = loginAsApoderado();
        
        String invalidPayload = """
            {
                "student": {
                    "firstName": ""
                }
            }
            """;
        
        given()
            .header("Authorization", authHeader(token))
            .body(invalidPayload)
        .when()
            .post("/api/applications")
        .then()
            .statusCode(400)
            .body("message", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(9)
    @DisplayName("b2) Postulación no encontrada")
    void testGetApplicationNotFound() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/applications/99999")
        .then()
            .statusCode(404)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(10)
    @DisplayName("b2) Obtener estadísticas de postulaciones como admin")
    void testGetApplicationStatistics() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/applications/statistics")
        .then()
            .statusCode(200)
            .body("totalApplications", greaterThanOrEqualTo(0))
            .body("pendingApplications", greaterThanOrEqualTo(0))
            .body("approvedApplications", greaterThanOrEqualTo(0))
            .body("rejectedApplications", greaterThanOrEqualTo(0))
            .time(lessThan(3000L));
    }
}