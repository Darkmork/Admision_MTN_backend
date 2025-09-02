package e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * E2E tests for document upload endpoints
 * Flujo b3) Subir documento: verificar upload → 200/201
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocumentUploadE2ETest extends BaseE2ETest {
    
    private static Long uploadedDocumentId;
    private static File testPdfFile;
    private static File testImageFile;
    
    @BeforeAll
    static void createTestFiles() throws IOException {
        // Create test PDF file
        testPdfFile = Files.createTempFile("test-document", ".pdf").toFile();
        Files.write(testPdfFile.toPath(), "%PDF-1.4\n1 0 obj\n<<\n/Type /Catalog\n/Pages 2 0 R\n>>\nendobj\n2 0 obj\n<<\n/Type /Pages\n/Kids [3 0 R]\n/Count 1\n>>\nendobj\n3 0 obj\n<<\n/Type /Page\n/Parent 2 0 R\n/Resources <<\n/Font <<\n/F1 4 0 R \n>>\n>>\n/MediaBox [0 0 612 792]\n/Contents 5 0 R\n>>\nendobj\n4 0 obj\n<<\n/Type /Font\n/Subtype /Type1\n/BaseFont /Times-Roman\n>>\nendobj\n5 0 obj\n<<\n/Length 44\n>>\nstream\nBT\n/F1 18 Tf\n0 0 Td\n(Test Document) Tj\nET\nendstream\nendobj\nxref\n0 6\n0000000000 65535 f \n0000000010 00000 n \n0000000079 00000 n \n0000000173 00000 n \n0000000301 00000 n \n0000000380 00000 n \ntrailer\n<<\n/Size 6\n/Root 1 0 R\n>>\nstartxref\n492\n%%EOF".getBytes());
        
        // Create test image file (minimal JPEG)
        testImageFile = Files.createTempFile("test-image", ".jpg").toFile();
        byte[] jpegBytes = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 
            0x49, 0x46, 0x00, 0x01, 0x01, 0x01, 0x00, 0x48, 0x00, 0x48, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xDB, 0x00, 0x43, 0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 
            0x05, 0x08, 0x07, 0x07, 0x07, 0x09, 0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D,
            (byte) 0xFF, (byte) 0xC0, 0x00, 0x11, 0x08, 0x00, 0x01, 0x00, 0x01, 0x01, 
            0x01, 0x11, 0x00, 0x02, 0x11, 0x01, 0x03, 0x11, 0x01,
            (byte) 0xFF, (byte) 0xC4, 0x00, 0x14, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
            (byte) 0xFF, (byte) 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00, 
            (byte) 0xD2, (byte) 0xFF, (byte) 0xD9
        };
        Files.write(testImageFile.toPath(), jpegBytes);
    }
    
    @AfterAll
    static void cleanupTestFiles() {
        if (testPdfFile != null && testPdfFile.exists()) {
            testPdfFile.delete();
        }
        if (testImageFile != null && testImageFile.exists()) {
            testImageFile.delete();
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("b3) Upload documento PDF exitoso")
    void testUploadPdfDocumentSuccess() {
        String token = loginAsApoderado();
        
        Response response = given()
            .header("Authorization", authHeader(token))
            .multiPart("file", testPdfFile, "application/pdf")
            .multiPart("documentType", "BIRTH_CERTIFICATE")
            .multiPart("description", "Certificado de nacimiento - Prueba E2E")
        .when()
            .post("/api/documents/upload")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("filename", containsString("test-document"))
            .body("contentType", equalTo("application/pdf"))
            .body("documentType", equalTo("BIRTH_CERTIFICATE"))
            .body("description", equalTo("Certificado de nacimiento - Prueba E2E"))
            .body("fileSize", greaterThan(0))
            .body("uploadedBy", notNullValue())
            .body("uploadDate", notNullValue())
            .time(lessThan(5000L))
            .extract().response();
        
        uploadedDocumentId = response.path("id");
        Assertions.assertNotNull(uploadedDocumentId, "Document ID should not be null");
    }
    
    @Test
    @Order(2)
    @DisplayName("b3) Upload imagen JPG exitoso")
    void testUploadImageDocumentSuccess() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
            .multiPart("file", testImageFile, "image/jpeg")
            .multiPart("documentType", "SCHOOL_REPORT")
            .multiPart("description", "Informe escolar - Prueba E2E")
        .when()
            .post("/api/documents/upload")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("filename", containsString("test-image"))
            .body("contentType", equalTo("image/jpeg"))
            .body("documentType", equalTo("SCHOOL_REPORT"))
            .body("fileSize", greaterThan(0))
            .time(lessThan(5000L));
    }
    
    @Test
    @Order(3)
    @DisplayName("b3) Descargar documento subido")
    void testDownloadUploadedDocument() {
        Assumptions.assumeTrue(uploadedDocumentId != null, "Document must be uploaded first");
        
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/documents/" + uploadedDocumentId)
        .then()
            .statusCode(200)
            .header("Content-Type", anyOf(equalTo("application/pdf"), startsWith("application/pdf")))
            .body(notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(4)
    @DisplayName("b3) Obtener tipos de documentos disponibles")
    void testGetDocumentTypes() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/documents/types")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].name", notNullValue())
            .body("[0].description", notNullValue())
            .body("[0].required", notNullValue())
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(5)
    @DisplayName("b3) Admin puede ver estadísticas de documentos")
    void testGetDocumentStatisticsAsAdmin() {
        String adminToken = loginAsAdmin();
        
        given()
            .header("Authorization", authHeader(adminToken))
        .when()
            .get("/api/documents/statistics")
        .then()
            .statusCode(200)
            .body("totalDocuments", greaterThanOrEqualTo(0))
            .body("totalSize", greaterThanOrEqualTo(0))
            .body("documentsByType", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(6)
    @DisplayName("b3) Error al subir archivo sin autenticación")
    void testUploadDocumentUnauthorized() {
        given()
            .multiPart("file", testPdfFile, "application/pdf")
            .multiPart("documentType", "BIRTH_CERTIFICATE")
        .when()
            .post("/api/documents/upload")
        .then()
            .statusCode(401)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(7)
    @DisplayName("b3) Error al subir archivo sin tipo de documento")
    void testUploadDocumentMissingType() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
            .multiPart("file", testPdfFile, "application/pdf")
        .when()
            .post("/api/documents/upload")
        .then()
            .statusCode(400)
            .body("message", notNullValue())
            .time(lessThan(3000L));
    }
    
    @Test
    @Order(8)
    @DisplayName("b3) Error al subir archivo vacío")
    void testUploadEmptyFile() throws IOException {
        String token = loginAsApoderado();
        
        // Create empty temporary file
        File emptyFile = Files.createTempFile("empty", ".pdf").toFile();
        try {
            given()
                .header("Authorization", authHeader(token))
                .multiPart("file", emptyFile, "application/pdf")
                .multiPart("documentType", "BIRTH_CERTIFICATE")
            .when()
                .post("/api/documents/upload")
            .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("empty"))
                .time(lessThan(3000L));
        } finally {
            emptyFile.delete();
        }
    }
    
    @Test
    @Order(9)
    @DisplayName("b3) Error al descargar documento no existente")
    void testDownloadNonExistentDocument() {
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .get("/api/documents/99999")
        .then()
            .statusCode(404)
            .time(lessThan(2000L));
    }
    
    @Test
    @Order(10)
    @DisplayName("b3) Eliminar documento subido")
    void testDeleteUploadedDocument() {
        Assumptions.assumeTrue(uploadedDocumentId != null, "Document must be uploaded first");
        
        String token = loginAsApoderado();
        
        given()
            .header("Authorization", authHeader(token))
        .when()
            .delete("/api/documents/" + uploadedDocumentId)
        .then()
            .statusCode(200)
            .body("message", containsStringIgnoringCase("eliminado"))
            .time(lessThan(3000L));
    }
}