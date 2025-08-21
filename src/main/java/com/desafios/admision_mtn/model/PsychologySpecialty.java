package com.desafios.admision_mtn.model;

public enum PsychologySpecialty {
    EDUCATIONAL("Psicología Educacional"),
    CLINICAL("Psicología Clínica"),
    DEVELOPMENTAL("Psicología del Desarrollo"),
    COGNITIVE("Psicología Cognitiva");

    private final String displayName;

    PsychologySpecialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}