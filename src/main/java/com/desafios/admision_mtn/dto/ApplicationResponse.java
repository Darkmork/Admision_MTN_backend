package com.desafios.admision_mtn.dto;

import com.desafios.admision_mtn.entity.Application;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    
    private Long id;
    private String studentName;
    private String grade;
    private String status;
    private LocalDateTime submissionDate;
    private String applicantEmail;
    private boolean success;
    private String message;

    // Constructor para respuesta exitosa
    public static ApplicationResponse success(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setSuccess(true);
        response.setMessage("Postulación creada exitosamente");
        response.setId(application.getId());
        response.setStudentName(application.getStudent().getFirstName() + " " + application.getStudent().getLastName());
        response.setGrade(application.getStudent().getGradeApplied());
        response.setStatus(application.getStatus().name());
        response.setSubmissionDate(application.getSubmissionDate());
        response.setApplicantEmail(application.getApplicantUser().getEmail());
        return response;
    }

    // Constructor para respuesta de error
    public static ApplicationResponse error(String message) {
        ApplicationResponse response = new ApplicationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}