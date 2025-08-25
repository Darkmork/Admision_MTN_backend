package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Document;
import com.desafios.admision_mtn.repository.ApplicationRepository;
import com.desafios.admision_mtn.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg", 
            "image/png"
    );

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".pdf", ".jpg", ".jpeg", ".png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public Document uploadDocument(Long applicationId, Document.DocumentType documentType, 
                                 MultipartFile file, boolean isRequired) throws IOException {
        
        // Validaciones básicas
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo no puede exceder 10MB");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se aceptan: PDF, JPG, PNG");
        }

        // Validar extensión
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida: " + fileExtension);
        }

        // Buscar la aplicación
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Aplicación no encontrada"));

        // Verificar si ya existe un documento de este tipo para esta aplicación
        documentRepository.findByApplicationIdAndDocumentType(applicationId, documentType)
                .ifPresent(existingDoc -> {
                    log.info("Reemplazando documento existente: {}", existingDoc.getFileName());
                    deleteFileIfExists(existingDoc.getFilePath());
                    documentRepository.delete(existingDoc);
                });

        // Generar nombre único para el archivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = String.format("%s_%s_%s_%s%s", 
                applicationId, 
                documentType.name(), 
                timestamp,
                UUID.randomUUID().toString().substring(0, 8),
                fileExtension);

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir, "applications", applicationId.toString());
        Files.createDirectories(uploadPath);

        // Guardar archivo
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Crear entidad Document
        Document document = new Document();
        document.setFileName(uniqueFilename);
        document.setOriginalName(originalFilename);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.getSize());
        document.setContentType(contentType);
        document.setDocumentType(documentType);
        document.setIsRequired(isRequired);
        document.setApplication(application);

        Document savedDocument = documentRepository.save(document);
        log.info("Documento guardado: {} para aplicación {}", uniqueFilename, applicationId);

        // Actualizar estado de la aplicación basado en documentos completados
        try {
            applicationService.updateApplicationStatusBasedOnDocuments(applicationId);
        } catch (Exception e) {
            log.warn("Error actualizando estado de aplicación después de subir documento", e);
        }

        return savedDocument;
    }

    public List<Document> getDocumentsByApplication(Long applicationId) {
        return documentRepository.findByApplication_IdOrderByCreatedAtDesc(applicationId);
    }

    public List<Document> getDocumentsByUserEmail(String userEmail) {
        return documentRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }

    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    public Resource loadFileAsResource(Long documentId) throws IOException {
        Document document = getDocumentById(documentId);
        Path filePath = Paths.get(document.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Archivo no encontrado: " + document.getFileName());
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("No se puede leer el archivo: " + document.getFileName());
        }
    }

    public void deleteDocument(Long documentId) throws IOException {
        Document document = getDocumentById(documentId);
        
        // Eliminar archivo físico
        deleteFileIfExists(document.getFilePath());
        
        // Eliminar registro de base de datos
        documentRepository.delete(document);
        
        log.info("Documento eliminado: {}", document.getFileName());
    }

    private void deleteFileIfExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Archivo eliminado: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo: {}", filePath, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }

    public Long getDocumentCountByApplication(Long applicationId) {
        return documentRepository.countByApplication_Id(applicationId);
    }

    public boolean userOwnsDocument(Long documentId, String userEmail) {
        return documentRepository.existsByIdAndApplication_ApplicantUser_Email(documentId, userEmail);
    }
}