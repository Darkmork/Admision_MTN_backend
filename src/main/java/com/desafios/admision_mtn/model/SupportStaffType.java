package com.desafios.admision_mtn.model;

public enum SupportStaffType {
    ADMINISTRATIVE("Administrativo"),
    TECHNICAL("Técnico"),
    ACADEMIC_COORDINATOR("Coordinador Académico"),
    STUDENT_SERVICES("Servicios Estudiantiles"),
    IT_SUPPORT("Soporte TI");

    private final String displayName;

    SupportStaffType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}