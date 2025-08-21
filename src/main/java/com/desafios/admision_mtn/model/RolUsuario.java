package com.desafios.admision_mtn.model;

public enum RolUsuario {
    // Roles del sistema original
    USER, 
    ADMIN,
    
    // Roles específicos del colegio
    PROFESSOR,      // Profesores de Matemática, Lenguaje, Inglés
    KINDER_TEACHER, // Personal de prekinder y kinder
    PSYCHOLOGIST,   // Psicólogos
    SUPPORT_STAFF   // Personal de apoyo
}