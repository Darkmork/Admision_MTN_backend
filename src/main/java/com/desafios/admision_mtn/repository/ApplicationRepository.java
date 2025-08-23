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
}