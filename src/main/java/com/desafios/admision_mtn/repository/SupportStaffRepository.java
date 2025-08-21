package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.SupportStaff;
import com.desafios.admision_mtn.model.SupportStaffType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupportStaffRepository extends JpaRepository<SupportStaff, Long> {
    
    @Query("SELECT s FROM SupportStaff s WHERE s.usuario.isActive = true")
    List<SupportStaff> findAllActive();
    
    List<SupportStaff> findByStaffType(SupportStaffType staffType);
    
    List<SupportStaff> findByDepartment(String department);
    
    @Query("SELECT s FROM SupportStaff s WHERE s.canAccessReports = true")
    List<SupportStaff> findWithReportAccess();
}