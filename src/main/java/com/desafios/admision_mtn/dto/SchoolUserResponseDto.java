package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.model.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SchoolUserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private RolUsuario role;
    private String phone;
    private boolean isActive;
    private LocalDateTime fechaRegistro;
    private LocalDateTime updatedAt;
    
    // Campos específicos para profesores
    private List<Professor.Subject> subjects;
    private List<String> assignedGrades;
    private String department;
    private Integer yearsOfExperience;
    private List<String> qualifications;
    private Boolean isAdmin;
    
    // Campos específicos para personal de kinder
    private KinderLevel assignedLevel;
    private List<String> specializations;
    
    // Campos específicos para psicólogos
    private PsychologySpecialty specialty;
    private String licenseNumber;
    private Boolean canConductInterviews;
    private Boolean canPerformPsychologicalEvaluations;
    private List<String> specializedAreas;
    
    // Campos específicos para personal de apoyo
    private SupportStaffType staffType;
    private List<String> responsibilities;
    private Boolean canAccessReports;
    private Boolean canManageSchedules;
}