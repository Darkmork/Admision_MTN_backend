package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.model.*;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateSchoolUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private RolUsuario role;
    private String phone;
    
    // Campos específicos para profesores (solo Matemática, Lenguaje, Inglés)
    private List<Professor.Subject> subjects;
    private List<String> assignedGrades;
    private String department;
    private Integer yearsOfExperience;
    private List<String> qualifications;
    
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