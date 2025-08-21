package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByApplicationOrderByCreatedAtDesc(Application application);
    
    List<Document> findByApplication_IdOrderByCreatedAtDesc(Long applicationId);
    
    @Query("SELECT d FROM Document d WHERE d.application.id = :applicationId AND d.documentType = :documentType")
    Optional<Document> findByApplicationIdAndDocumentType(@Param("applicationId") Long applicationId, 
                                                          @Param("documentType") Document.DocumentType documentType);
    
    @Query("SELECT d FROM Document d WHERE d.application.applicantUser.email = :userEmail ORDER BY d.createdAt DESC")
    List<Document> findByUserEmailOrderByCreatedAtDesc(@Param("userEmail") String userEmail);
    
    Long countByApplication_Id(Long applicationId);
}