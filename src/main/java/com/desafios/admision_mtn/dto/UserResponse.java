package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String rut;
    private String phone;
    private User.UserRole role;
    private String roleDisplayName;
    private User.EducationalLevel educationalLevel;
    private String educationalLevelDisplayName;
    private User.Subject subject;
    private String subjectDisplayName;
    private Boolean emailVerified;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFirstName() + " " + user.getLastName());
        response.setEmail(user.getEmail());
        response.setRut(user.getRut());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setRoleDisplayName(user.getRole().getDisplayName());
        response.setEducationalLevel(user.getEducationalLevel());
        response.setEducationalLevelDisplayName(user.getEducationalLevel() != null ? user.getEducationalLevel().getDisplayName() : null);
        response.setSubject(user.getSubject());
        response.setSubjectDisplayName(user.getSubject() != null ? user.getSubject().getDisplayName() : null);
        response.setEmailVerified(user.getEmailVerified());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        return response;
    }
}