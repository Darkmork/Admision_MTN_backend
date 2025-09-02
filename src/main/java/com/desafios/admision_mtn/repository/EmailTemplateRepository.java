package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.EmailTemplate;
import com.desafios.admision_mtn.entity.EmailTemplate.TemplateCategory;
import com.desafios.admision_mtn.entity.EmailTemplate.TemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    // Find all templates ordered by ID  
    @Query("SELECT et FROM EmailTemplate et ORDER BY et.id ASC")
    List<EmailTemplate> findAllOrderedById();

    // Búsquedas básicas
    Optional<EmailTemplate> findByTemplateKey(String templateKey);
    
    List<EmailTemplate> findByActiveTrue();
    
    List<EmailTemplate> findByActiveTrueOrderByNameAsc();
    
    List<EmailTemplate> findByCategory(TemplateCategory category);
    
    List<EmailTemplate> findByType(TemplateType type);
    
    List<EmailTemplate> findByLanguage(String language);

    // Búsquedas por categoría y estado
    List<EmailTemplate> findByCategoryAndActiveTrue(TemplateCategory category);
    
    List<EmailTemplate> findByTypeAndActiveTrue(TemplateType type);
    
    List<EmailTemplate> findByLanguageAndActiveTrue(String language);

    // Template por defecto para una categoría
    Optional<EmailTemplate> findByCategoryAndIsDefaultTrueAndActiveTrue(TemplateCategory category);
    
    Optional<EmailTemplate> findByTypeAndIsDefaultTrueAndActiveTrue(TemplateType type);

    // Búsquedas combinadas
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "(:category IS NULL OR et.category = :category) AND " +
           "(:type IS NULL OR et.type = :type) AND " +
           "(:active IS NULL OR et.active = :active) AND " +
           "(:language IS NULL OR et.language = :language) " +
           "ORDER BY et.name ASC")
    List<EmailTemplate> findWithFilters(
        @Param("category") TemplateCategory category,
        @Param("type") TemplateType type,
        @Param("active") Boolean active,
        @Param("language") String language
    );

    // Búsqueda con paginación
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "(:searchTerm IS NULL OR " +
           "LOWER(et.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(et.templateKey) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(et.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY et.name ASC")
    Page<EmailTemplate> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Templates disponibles para una categoría específica
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "et.category = :category AND et.active = true " +
           "ORDER BY CASE WHEN et.isDefault = true THEN 0 ELSE 1 END, et.name ASC")
    List<EmailTemplate> findAvailableTemplatesForCategory(@Param("category") TemplateCategory category);

    // Verificar si existe un template con la misma clave
    boolean existsByTemplateKey(String templateKey);
    
    boolean existsByTemplateKeyAndIdNot(String templateKey, Long id);

    // Contar templates por categoría
    @Query("SELECT COUNT(et) FROM EmailTemplate et WHERE et.category = :category AND et.active = true")
    long countActiveByCategoryAndActiveTrue(@Param("category") TemplateCategory category);

    // Contar templates por tipo
    @Query("SELECT COUNT(et) FROM EmailTemplate et WHERE et.type = :type AND et.active = true")
    long countActiveByTypeAndActiveTrue(@Param("type") TemplateType type);

    // Templates que requieren actualización (sin uso reciente)
    @Query("SELECT et FROM EmailTemplate et WHERE et.active = true AND et.updatedAt < :since")
    List<EmailTemplate> findTemplatesRequiringUpdate(@Param("since") java.time.LocalDateTime since);

    // Templates más utilizados (por implementar tracking de uso)
    @Query("SELECT et FROM EmailTemplate et WHERE et.active = true ORDER BY et.name ASC")
    List<EmailTemplate> findMostUsedTemplates();

    // Verificar templates por defecto duplicados
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "et.category = :category AND et.isDefault = true AND et.active = true AND et.id != :excludeId")
    List<EmailTemplate> findDuplicateDefaults(@Param("category") TemplateCategory category, @Param("excludeId") Long excludeId);

    // Templates para reportes y estadísticas
    @Query("SELECT et.category, COUNT(et) FROM EmailTemplate et WHERE et.active = true GROUP BY et.category")
    List<Object[]> findTemplateCountByCategory();

    @Query("SELECT et.type, COUNT(et) FROM EmailTemplate et WHERE et.active = true GROUP BY et.type")
    List<Object[]> findTemplateCountByType();

    @Query("SELECT et.language, COUNT(et) FROM EmailTemplate et WHERE et.active = true GROUP BY et.language")
    List<Object[]> findTemplateCountByLanguage();

    // Templates para entrevistas (categorías relacionadas)
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "et.category IN ('INTERVIEW_ASSIGNMENT', 'INTERVIEW_CONFIRMATION', 'INTERVIEW_REMINDER', 'INTERVIEW_RESCHEDULE') " +
           "AND et.active = true ORDER BY et.category, et.name")
    List<EmailTemplate> findInterviewRelatedTemplates();

    // Templates para admisión (categorías relacionadas)
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "et.category IN ('APPLICATION_STATUS', 'STUDENT_SELECTION', 'STUDENT_REJECTION', 'ADMISSION_RESULTS') " +
           "AND et.active = true ORDER BY et.category, et.name")
    List<EmailTemplate> findAdmissionRelatedTemplates();

    // Template específico con fallback al default
    @Query("SELECT et FROM EmailTemplate et WHERE " +
           "et.templateKey = :templateKey AND et.active = true " +
           "UNION " +
           "SELECT et FROM EmailTemplate et WHERE " +
           "et.category = :category AND et.isDefault = true AND et.active = true " +
           "ORDER BY CASE WHEN templateKey = :templateKey THEN 0 ELSE 1 END")
    Optional<EmailTemplate> findByTemplateKeyWithFallback(@Param("templateKey") String templateKey, @Param("category") TemplateCategory category);
}