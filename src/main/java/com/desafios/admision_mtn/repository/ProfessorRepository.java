package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    
    @Query("SELECT p FROM Professor p WHERE p.usuario.isActive = true")
    List<Professor> findAllActive();
    
    @Query("SELECT p FROM Professor p WHERE p.subjects LIKE %:subject%")
    List<Professor> findBySubject(Professor.Subject subject);
    
    @Query("SELECT p FROM Professor p WHERE :grade MEMBER OF p.assignedGrades")
    List<Professor> findByAssignedGrade(String grade);
}