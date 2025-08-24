package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.Application;
import com.desafios.admision_mtn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByApplicantUserOrderByCreatedAtDesc(User applicantUser);
    
    List<Application> findByStatusOrderByCreatedAtDesc(Application.ApplicationStatus status);
    
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.student " +
           "LEFT JOIN FETCH a.father " +
           "LEFT JOIN FETCH a.mother " +
           "LEFT JOIN FETCH a.supporter " +
           "LEFT JOIN FETCH a.guardian " +
           "JOIN FETCH a.applicantUser " +
           "WHERE a.applicantUser = :user")
    List<Application> findByApplicantUserWithStudent(@Param("user") User user);
    
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.student s " +
           "WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.maternalLastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.rut) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Application> findByStudentNameOrRutContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    Optional<Application> findByStudentRut(String rut);
    
    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.student " +
           "LEFT JOIN FETCH a.father " +
           "LEFT JOIN FETCH a.mother " +
           "LEFT JOIN FETCH a.supporter " +
           "LEFT JOIN FETCH a.guardian " +
           "LEFT JOIN FETCH a.applicantUser " +
           "ORDER BY a.createdAt DESC")
    List<Application> findAllWithRelations();

    // Simple method without complex joins for testing
    List<Application> findAllByOrderByCreatedAtDesc();
    
    // Métodos para el workflow automático
    List<Application> findByStatusIn(List<Application.ApplicationStatus> statuses);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    long countByStatus(@Param("status") Application.ApplicationStatus status);
    
    // Métodos adicionales para el dashboard
    @Query("SELECT a FROM Application a WHERE a.createdAt >= :fromDate ORDER BY a.createdAt DESC")
    List<Application> findFromDate(@Param("fromDate") java.time.LocalDateTime fromDate);
    
    @Query("SELECT a FROM Application a WHERE a.createdAt >= :fromDate ORDER BY a.createdAt DESC")
    List<Application> findRecentApplications(@Param("fromDate") java.time.LocalDateTime fromDate);
    
    @Query("SELECT a FROM Application a WHERE a.status IN ('APPROVED', 'REJECTED', 'WAITLIST') ORDER BY a.updatedAt DESC")
    List<Application> findCompletedApplications();
    
    @Query("SELECT a FROM Application a WHERE a.status = :status AND a.updatedAt <= :cutoffDate ORDER BY a.updatedAt ASC")
    List<Application> findOverdueApplications(@Param("status") Application.ApplicationStatus status, @Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}