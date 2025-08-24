package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.entity.Document;
import com.desafios.admision_mtn.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{applicationId}")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "isRequired", defaultValue = "false") boolean isRequired) {
        
        try {
            // Obtener usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            log.info("Usuario {} subiendo documento {} para aplicación {}", userEmail, documentType, applicationId);

            // Convertir string a enum
            Document.DocumentType docType = Document.DocumentType.valueOf(documentType.toUpperCase());
            
            // Subir documento
            Document savedDocument = documentService.uploadDocument(applicationId, docType, file, isRequired);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Documento subido exitosamente");
            response.put("document", createDocumentResponse(savedDocument));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error de validación al subir documento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error interno al subir documento", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error interno del servidor"));
        }
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsByApplication(@PathVariable Long applicationId) {
        try {
            List<Document> documents = documentService.getDocumentsByApplication(applicationId);
            List<Map<String, Object>> response = documents.stream()
                    .map(this::createDocumentResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo documentos para aplicación {}", applicationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/my-documents")
    public ResponseEntity<List<Map<String, Object>>> getMyDocuments() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            List<Document> documents = documentService.getDocumentsByUserEmail(userEmail);
            List<Map<String, Object>> response = documents.stream()
                    .map(this::createDocumentResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo documentos del usuario", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/view/{documentId}")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long documentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            if (!documentService.userOwnsDocument(documentId, userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Document document = documentService.getDocumentById(documentId);
            Resource resource = documentService.loadFileAsResource(documentId);
            
            String contentType = document.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getOriginalName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error visualizando documento {}", documentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            if (!documentService.userOwnsDocument(documentId, userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Document document = documentService.getDocumentById(documentId);
            Resource resource = documentService.loadFileAsResource(documentId);
            
            String contentType = document.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getOriginalName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error descargando documento {}", documentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable Long documentId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            if (!documentService.userOwnsDocument(documentId, userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("No tienes permiso para eliminar este documento"));
            }

            log.info("Usuario {} eliminando documento {}", userEmail, documentId);

            documentService.deleteDocument(documentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Documento eliminado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error eliminando documento {}", documentId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error eliminando documento"));
        }
    }

    @GetMapping("/public/types")
    public ResponseEntity<Map<String, Object>> getDocumentTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("documentTypes", Document.DocumentType.values());
        response.put("allowedFormats", new String[]{"PDF", "JPG", "PNG"});
        response.put("maxFileSize", "10MB");
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createDocumentResponse(Document document) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put("id", document.getId());
        docMap.put("fileName", document.getFileName());
        docMap.put("originalName", document.getOriginalName());
        docMap.put("fileSize", document.getFileSize());
        docMap.put("contentType", document.getContentType());
        docMap.put("documentType", document.getDocumentType());
        docMap.put("isRequired", document.getIsRequired());
        docMap.put("createdAt", document.getCreatedAt());
        return docMap;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}