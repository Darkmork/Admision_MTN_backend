package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.KinderTeacher;
import com.desafios.admision_mtn.model.KinderLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KinderTeacherRepository extends JpaRepository<KinderTeacher, Long> {
    
    @Query("SELECT k FROM KinderTeacher k WHERE k.usuario.isActive = true")
    List<KinderTeacher> findAllActive();
    
    List<KinderTeacher> findByAssignedLevel(KinderLevel level);
}