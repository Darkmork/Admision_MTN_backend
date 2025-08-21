package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Psychologist;
import com.desafios.admision_mtn.model.PsychologySpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PsychologistRepository extends JpaRepository<Psychologist, Long> {
    
    @Query("SELECT p FROM Psychologist p WHERE p.usuario.isActive = true")
    List<Psychologist> findAllActive();
    
    List<Psychologist> findBySpecialty(PsychologySpecialty specialty);
    
    @Query("SELECT p FROM Psychologist p WHERE p.canConductInterviews = true")
    List<Psychologist> findInterviewers();
    
    @Query("SELECT p FROM Psychologist p WHERE p.canPerformPsychologicalEvaluations = true")
    List<Psychologist> findEvaluators();
}