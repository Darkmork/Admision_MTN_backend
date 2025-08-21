package com.desafios.admision_mtn.model;

public enum KinderLevel {
    PREKINDER("prekinder", "Pre-Kinder"),
    KINDER("kinder", "Kinder");

    private final String code;
    private final String displayName;

    KinderLevel(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}