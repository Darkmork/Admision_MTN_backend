package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.entity.EmailTemplate;
import com.desafios.admision_mtn.entity.EmailTemplate.TemplateCategory;
import com.desafios.admision_mtn.entity.EmailTemplate.TemplateType;
import com.desafios.admision_mtn.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private static final String VARIABLE_PATTERN = "\\{\\{(\\w+)\\}\\}";
    private final Pattern variableRegex = Pattern.compile(VARIABLE_PATTERN);

    /**
     * Crear un nuevo template de correo
     */
    public EmailTemplate createTemplate(EmailTemplate template) {
        log.info("Creando nuevo template de correo: {}", template.getTemplateKey());
        
        validateTemplate(template);
        
        if (templateRepository.existsByTemplateKey(template.getTemplateKey())) {
            throw new IllegalArgumentException("Ya existe un template con la clave: " + template.getTemplateKey());
        }
        
        // Extraer variables automáticamente del contenido
        extractAndSetVariables(template);
        
        // Manejar template por defecto
        handleDefaultTemplate(template);
        
        EmailTemplate savedTemplate = templateRepository.save(template);
        log.info("Template creado exitosamente con ID: {}", savedTemplate.getId());
        
        return savedTemplate;
    }

    /**
     * Actualizar template existente
     */
    public EmailTemplate updateTemplate(Long id, EmailTemplate updatedTemplate) {
        log.info("Actualizando template con ID: {}", id);
        
        EmailTemplate existingTemplate = templateRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Template no encontrado con ID: " + id));
        
        validateTemplate(updatedTemplate);
        
        // Verificar clave única si cambió
        if (!existingTemplate.getTemplateKey().equals(updatedTemplate.getTemplateKey())) {
            if (templateRepository.existsByTemplateKeyAndIdNot(updatedTemplate.getTemplateKey(), id)) {
                throw new IllegalArgumentException("Ya existe otro template con la clave: " + updatedTemplate.getTemplateKey());
            }
        }
        
        // Actualizar campos
        existingTemplate.setTemplateKey(updatedTemplate.getTemplateKey());
        existingTemplate.setName(updatedTemplate.getName());
        existingTemplate.setDescription(updatedTemplate.getDescription());
        existingTemplate.setType(updatedTemplate.getType());
        existingTemplate.setCategory(updatedTemplate.getCategory());
        existingTemplate.setSubject(updatedTemplate.getSubject());
        existingTemplate.setHtmlContent(updatedTemplate.getHtmlContent());
        existingTemplate.setTextContent(updatedTemplate.getTextContent());
        existingTemplate.setLanguage(updatedTemplate.getLanguage());
        existingTemplate.setUpdatedBy(updatedTemplate.getUpdatedBy());
        
        // Extraer variables automáticamente del contenido actualizado
        extractAndSetVariables(existingTemplate);
        
        // Manejar template por defecto si cambió
        if (updatedTemplate.getIsDefault() != null && updatedTemplate.getIsDefault() != existingTemplate.getIsDefault()) {
            existingTemplate.setIsDefault(updatedTemplate.getIsDefault());
            handleDefaultTemplate(existingTemplate);
        }
        
        EmailTemplate savedTemplate = templateRepository.save(existingTemplate);
        log.info("Template actualizado exitosamente: {}", savedTemplate.getTemplateKey());
        
        return savedTemplate;
    }

    /**
     * Obtener template por ID
     */
    @Transactional(readOnly = true)
    public EmailTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Template no encontrado con ID: " + id));
    }

    /**
     * Obtener template por clave con fallback al default
     */
    @Transactional(readOnly = true)
    public EmailTemplate getTemplateByKey(String templateKey, TemplateCategory category) {
        log.info("Buscando template con clave: {} para categoría: {}", templateKey, category);
        
        Optional<EmailTemplate> template = templateRepository.findByTemplateKey(templateKey);
        if (template.isPresent() && template.get().getActive()) {
            return template.get();
        }
        
        // Fallback al template por defecto de la categoría
        log.warn("Template con clave {} no encontrado, usando template por defecto para categoría {}", templateKey, category);
        return templateRepository.findByCategoryAndIsDefaultTrueAndActiveTrue(category)
            .orElseThrow(() -> new EntityNotFoundException("No hay template disponible para la categoría: " + category));
    }

    /**
     * Obtener todos los templates
     */
    @Transactional(readOnly = true)
    public List<EmailTemplate> getAllTemplates() {
        log.info("Obteniendo todos los templates de correo");
        List<EmailTemplate> templates = templateRepository.findAll();
        log.info("Encontrados {} templates en la base de datos", templates.size());
        return templates;
    }

    /**
     * Obtener templates por categoría
     */
    @Transactional(readOnly = true)
    public List<EmailTemplate> getTemplatesByCategory(TemplateCategory category) {
        log.info("Obteniendo templates para categoría: {}", category);
        return templateRepository.findByCategory(category);
    }

    /**
     * Obtener todos los templates con filtros
     */
    @Transactional(readOnly = true)
    public List<EmailTemplate> getTemplatesWithFilters(TemplateCategory category, TemplateType type, Boolean active, String language) {
        return templateRepository.findWithFilters(category, type, active, language);
    }

    /**
     * Buscar templates con paginación
     */
    @Transactional(readOnly = true)
    public Page<EmailTemplate> searchTemplates(String searchTerm, Pageable pageable) {
        return templateRepository.findBySearchTerm(searchTerm, pageable);
    }

    /**
     * Obtener templates disponibles para una categoría
     */
    @Transactional(readOnly = true)
    public List<EmailTemplate> getAvailableTemplatesForCategory(TemplateCategory category) {
        return templateRepository.findAvailableTemplatesForCategory(category);
    }

    /**
     * Activar template
     */
    public EmailTemplate activateTemplate(Long id) {
        EmailTemplate template = getTemplateById(id);
        template.activate();
        log.info("Template activado: {}", template.getTemplateKey());
        return templateRepository.save(template);
    }

    /**
     * Desactivar template
     */
    public EmailTemplate deactivateTemplate(Long id) {
        EmailTemplate template = getTemplateById(id);
        template.deactivate();
        log.info("Template desactivado: {}", template.getTemplateKey());
        return templateRepository.save(template);
    }

    /**
     * Establecer como template por defecto
     */
    public EmailTemplate setAsDefault(Long id) {
        EmailTemplate template = getTemplateById(id);
        
        // Remover default de otros templates de la misma categoría
        List<EmailTemplate> duplicateDefaults = templateRepository.findDuplicateDefaults(template.getCategory(), id);
        for (EmailTemplate duplicate : duplicateDefaults) {
            duplicate.removeAsDefault();
            templateRepository.save(duplicate);
        }
        
        template.setAsDefault();
        log.info("Template establecido como por defecto: {}", template.getTemplateKey());
        return templateRepository.save(template);
    }

    /**
     * Procesar template con variables
     */
    public String processTemplate(EmailTemplate template, Map<String, Object> variables) {
        log.debug("Procesando template: {} con {} variables", template.getTemplateKey(), variables.size());
        
        String content = template.getHtmlContent();
        
        // Reemplazar variables en el contenido
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String variable = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            content = content.replace(variable, value);
        }
        
        return content;
    }

    /**
     * Procesar asunto del template con variables
     */
    public String processSubject(EmailTemplate template, Map<String, Object> variables) {
        String subject = template.getSubject();
        
        // Reemplazar variables en el asunto
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String variable = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            subject = subject.replace(variable, value);
        }
        
        return subject;
    }

    /**
     * Eliminar template
     */
    public void deleteTemplate(Long id) {
        EmailTemplate template = getTemplateById(id);
        
        if (template.getIsDefault()) {
            throw new IllegalStateException("No se puede eliminar un template por defecto. Primero debe establecer otro template como por defecto.");
        }
        
        log.info("Eliminando template: {}", template.getTemplateKey());
        templateRepository.delete(template);
    }

    /**
     * Obtener estadísticas de templates
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTemplateStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTemplates", templateRepository.count());
        stats.put("activeTemplates", templateRepository.findByActiveTrue().size());
        stats.put("templatesByCategory", convertToMap(templateRepository.findTemplateCountByCategory()));
        stats.put("templatesByType", convertToMap(templateRepository.findTemplateCountByType()));
        stats.put("templatesByLanguage", convertToMap(templateRepository.findTemplateCountByLanguage()));
        
        return stats;
    }

    /**
     * Obtener conteo simple de templates para testing
     */
    @Transactional(readOnly = true)
    public long getTemplateCount() {
        log.info("Obteniendo conteo total de templates");
        long count = templateRepository.count();
        log.info("Conteo obtenido: {}", count);
        return count;
    }

    // Métodos privados de utilidad

    private void validateTemplate(EmailTemplate template) {
        if (template.getTemplateKey() == null || template.getTemplateKey().trim().isEmpty()) {
            throw new IllegalArgumentException("La clave del template es obligatoria");
        }
        
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del template es obligatorio");
        }
        
        if (template.getSubject() == null || template.getSubject().trim().isEmpty()) {
            throw new IllegalArgumentException("El asunto del template es obligatorio");
        }
        
        if (template.getHtmlContent() == null || template.getHtmlContent().trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido HTML del template es obligatorio");
        }
        
        if (template.getCategory() == null) {
            throw new IllegalArgumentException("La categoría del template es obligatoria");
        }
        
        if (template.getType() == null) {
            throw new IllegalArgumentException("El tipo del template es obligatorio");
        }
    }

    private void extractAndSetVariables(EmailTemplate template) {
        Set<String> variables = new HashSet<>();
        
        // Extraer variables del asunto
        Matcher subjectMatcher = variableRegex.matcher(template.getSubject());
        while (subjectMatcher.find()) {
            variables.add(subjectMatcher.group(1));
        }
        
        // Extraer variables del contenido HTML
        Matcher contentMatcher = variableRegex.matcher(template.getHtmlContent());
        while (contentMatcher.find()) {
            variables.add(contentMatcher.group(1));
        }
        
        // Convertir a JSON simple para almacenar
        template.setVariables(String.join(",", variables));
        
        log.debug("Variables extraídas del template {}: {}", template.getTemplateKey(), variables);
    }

    private void handleDefaultTemplate(EmailTemplate template) {
        if (template.getIsDefault() != null && template.getIsDefault()) {
            // Remover default de otros templates de la misma categoría
            List<EmailTemplate> duplicateDefaults = templateRepository.findDuplicateDefaults(
                template.getCategory(), 
                template.getId() != null ? template.getId() : -1L
            );
            
            for (EmailTemplate duplicate : duplicateDefaults) {
                duplicate.removeAsDefault();
                templateRepository.save(duplicate);
            }
        }
    }

    private Map<String, Long> convertToMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put(result[0].toString(), ((Number) result[1]).longValue());
        }
        return map;
    }
}