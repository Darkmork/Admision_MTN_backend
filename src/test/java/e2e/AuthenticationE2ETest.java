package e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * E2E tests for authentication endpoints
 * Flujo b1) Login: obtener JWT (usuario de pruebas) → 200
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationE2ETest extends BaseE2ETest {
    
    @Test
    @Order(1)
    @DisplayName("b1) Login exitoso como administrador")
    void testAdminLoginSuccess() {
        given()
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, ADMIN_EMAIL, ADMIN_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]+$")) // JWT pattern
            .body("user.email", equalTo(ADMIN_EMAIL))
            .body("user.role", equalTo("ADMIN"))
            .body("user.firstName", notNullValue())
            .body("user.lastName", notNullValue())
            .body("expiresIn", greaterThan(0))
            .time(lessThan(5000L)); // Response time < 5 seconds
    }
    
    @Test
    @Order(2)
    @DisplayName("b1) Login exitoso como apoderado")
    void testApoderadoLoginSuccess() {
        given()
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, APODERADO_EMAIL, APODERADO_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("token", matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]+$"))
            .body("user.email", equalTo(APODERADO_EMAIL))
            .body("user.role", equalTo("APODERADO"))
            .body("user.emailVerified", equalTo(true))
            .body("user.active", equalTo(true))
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(3)
    @DisplayName("b1) Login exitoso como profesor")
    void testTeacherLoginSuccess() {
        given()
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, TEACHER_EMAIL, TEACHER_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("user.email", equalTo(TEACHER_EMAIL))
            .body("user.role", anyOf(equalTo("TEACHER"), equalTo("COORDINATOR"), equalTo("PSYCHOLOGIST"), equalTo("CYCLE_DIRECTOR")))
            .body("user.educationalLevel", notNullValue())
            .body("user.subject", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(4)
    @DisplayName("Login fallido - credenciales inválidas")
    void testLoginInvalidCredentials() {
        given()
            .body("""
                {
                    "email": "invalid@test.com",
                    "password": "wrongpassword"
                }
                """)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("message", containsStringIgnoringCase("credenciales"))
            .body("timestamp", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(5)
    @DisplayName("Login fallido - email no válido")
    void testLoginInvalidEmail() {
        given()
            .body("""
                {
                    "email": "not-an-email",
                    "password": "somepassword"
                }
                """)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("message", notNullValue())
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(6)
    @DisplayName("Login fallido - campos requeridos faltantes")
    void testLoginMissingFields() {
        given()
            .body("""
                {
                    "email": "test@test.com"
                }
                """)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(400)
            .body("message", notNullValue())
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(7)
    @DisplayName("Acceso a endpoint protegido con token válido")
    void testProtectedEndpointWithValidToken() {
        String token = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/test/protected")
        .then()
            .statusCode(200)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(8)
    @DisplayName("Acceso a endpoint protegido sin token")
    void testProtectedEndpointWithoutToken() {
        given()
        .when()
            .get("/api/test/protected")
        .then()
            .statusCode(401)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(9)
    @DisplayName("Acceso a endpoint protegido con token inválido")
    void testProtectedEndpointWithInvalidToken() {
        given()
            .header("Authorization", "Bearer invalid.jwt.token")
        .when()
            .get("/api/test/protected")
        .then()
            .statusCode(401)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(10)
    @DisplayName("Validar estructura del token JWT")
    void testJwtTokenStructure() {
        Response response = given()
            .body(String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, ADMIN_EMAIL, ADMIN_PASSWORD))
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract().response();
        
        String token = response.path("token");
        
        // Validate JWT has 3 parts separated by dots
        String[] tokenParts = token.split("\\.");
        Assertions.assertEquals(3, tokenParts.length, "JWT token should have 3 parts");
        
        // Each part should be base64 encoded (no spaces or invalid chars)
        for (String part : tokenParts) {
            Assertions.assertTrue(part.matches("^[A-Za-z0-9-_]+$"), 
                "JWT part should be valid base64: " + part);
        }
    }
}