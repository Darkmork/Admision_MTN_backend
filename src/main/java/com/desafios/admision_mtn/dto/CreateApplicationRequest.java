package com.desafios.admision_mtn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApplicationRequest {

    // Datos del estudiante
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    private String lastName;
    
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;
    
    @NotBlank(message = "La fecha de nacimiento es obligatoria")
    private String birthDate;
    
    private String studentEmail;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String studentAddress;
    
    @NotBlank(message = "El grado es obligatorio")
    private String grade;
    
    private String currentSchool;
    private String additionalNotes;

    // Datos del padre
    @NotBlank(message = "El nombre del padre es obligatorio")
    private String parent1Name;
    
    @NotBlank(message = "El RUT del padre es obligatorio")
    private String parent1Rut;
    
    @NotBlank(message = "El email del padre es obligatorio")
    private String parent1Email;
    
    @NotBlank(message = "El teléfono del padre es obligatorio")
    private String parent1Phone;
    
    @NotBlank(message = "La dirección del padre es obligatoria")
    private String parent1Address;
    
    @NotBlank(message = "La profesión del padre es obligatoria")
    private String parent1Profession;

    // Datos de la madre
    @NotBlank(message = "El nombre de la madre es obligatorio")
    private String parent2Name;
    
    @NotBlank(message = "El RUT de la madre es obligatorio")
    private String parent2Rut;
    
    @NotBlank(message = "El email de la madre es obligatorio")
    private String parent2Email;
    
    @NotBlank(message = "El teléfono de la madre es obligatorio")
    private String parent2Phone;
    
    @NotBlank(message = "La dirección de la madre es obligatoria")
    private String parent2Address;
    
    @NotBlank(message = "La profesión de la madre es obligatoria")
    private String parent2Profession;

    // Datos del sostenedor
    @NotBlank(message = "El nombre del sostenedor es obligatorio")
    private String supporterName;
    
    @NotBlank(message = "El RUT del sostenedor es obligatorio")
    private String supporterRut;
    
    @NotBlank(message = "El email del sostenedor es obligatorio")
    private String supporterEmail;
    
    @NotBlank(message = "El teléfono del sostenedor es obligatorio")
    private String supporterPhone;
    
    @NotBlank(message = "La relación del sostenedor es obligatoria")
    private String supporterRelation;

    // Datos del apoderado
    @NotBlank(message = "El nombre del apoderado es obligatorio")
    private String guardianName;
    
    @NotBlank(message = "El RUT del apoderado es obligatorio")
    private String guardianRut;
    
    @NotBlank(message = "El email del apoderado es obligatorio")
    private String guardianEmail;
    
    @NotBlank(message = "El teléfono del apoderado es obligatorio")
    private String guardianPhone;
    
    @NotBlank(message = "La relación del apoderado es obligatoria")
    private String guardianRelation;
}