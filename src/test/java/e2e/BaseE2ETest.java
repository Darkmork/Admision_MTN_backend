package e2e;

import com.desafios.admision_mtn.AdmisionMtnApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Base class for E2E API tests using REST Assured
 * Sistema de Admisión MTN - Fase 0 Pre-flight
 */
@SpringBootTest(
    classes = AdmisionMtnApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.email.mock-mode=true",
    "logging.level.com.desafios.admision_mtn=WARN",
    "logging.level.org.springframework.security=WARN"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseE2ETest {
    
    @LocalServerPort
    protected int serverPort;
    
    protected String baseUrl;
    protected String authToken;
    protected String professorToken;
    
    // Test credentials - must match data in e2e-fixtures
    protected static final String ADMIN_EMAIL = "admin@mtn.cl";
    protected static final String ADMIN_PASSWORD = "admin123";
    protected static final String APODERADO_EMAIL = "familia01@test.cl";
    protected static final String APODERADO_PASSWORD = "secret";
    protected static final String TEACHER_EMAIL = "maria.nueva@mtn.cl";
    protected static final String TEACHER_PASSWORD = "secret";
    
    @BeforeEach
    void setUpBaseE2E() {
        baseUrl = "http://localhost:" + serverPort;
        RestAssured.baseURI = baseUrl;
        RestAssured.port = serverPort;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Default request specification
        RestAssured.requestSpecification = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }
    
    /**
     * Login as admin user and get JWT token
     */
    protected String loginAsAdmin() {
        if (authToken == null) {
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
                .body("token", notNullValue())
                .body("user.email", equalTo(ADMIN_EMAIL))
                .body("user.role", equalTo("ADMIN"))
                .extract().response();
            
            authToken = response.path("token");
        }
        return authToken;
    }
    
    /**
     * Login as apoderado user and get JWT token
     */
    protected String loginAsApoderado() {
        Response response = given()
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
            .body("user.email", equalTo(APODERADO_EMAIL))
            .body("user.role", equalTo("APODERADO"))
            .extract().response();
        
        return response.path("token");
    }
    
    /**
     * Login as teacher/professor and get JWT token
     */
    protected String loginAsTeacher() {
        if (professorToken == null) {
            Response response = given()
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
                .body("user.role", anyOf(equalTo("TEACHER"), equalTo("COORDINATOR"), equalTo("PSYCHOLOGIST")))
                .extract().response();
            
            professorToken = response.path("token");
        }
        return professorToken;
    }
    
    /**
     * Get authorization header with JWT token
     */
    protected String authHeader(String token) {
        return "Bearer " + token;
    }
    
    /**
     * Wait for application to be fully ready
     */
    protected void waitForApplicationReady() {
        int maxRetries = 30;
        int retries = 0;
        
        while (retries < maxRetries) {
            try {
                given()
                    .when()
                    .get("/actuator/health")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("UP"));
                return;
            } catch (Exception e) {
                retries++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for application", ie);
                }
            }
        }
        throw new RuntimeException("Application not ready after " + maxRetries + " retries");
    }
    
    /**
     * Clean up tokens after tests
     */
    protected void cleanupTokens() {
        authToken = null;
        professorToken = null;
    }
}