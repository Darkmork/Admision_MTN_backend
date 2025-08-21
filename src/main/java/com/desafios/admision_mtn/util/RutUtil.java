package com.desafios.admision_mtn.util;

import java.util.regex.Pattern;

/**
 * Utilidad para validación de RUT chileno
 * Soporta formatos: 12345678-9, 12.345.678-9, 12345678-K
 */
public class RutUtil {
    
    private static final Pattern RUT_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{3})*(\\.[0-9]{3})*-[0-9Kk]$");
    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("^[0-9]+[0-9Kk]$");
    
    /**
     * Valida si un RUT tiene el formato y dígito verificador correcto
     * @param rut RUT en formato 12345678-9 o 12.345.678-9
     * @return true si el RUT es válido
     */
    public static boolean isValidRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        
        rut = rut.trim().toUpperCase();
        
        // Verificar formato básico
        if (!RUT_PATTERN.matcher(rut).matches() && !DIGITS_ONLY_PATTERN.matcher(rut).matches()) {
            return false;
        }
        
        // Limpiar el RUT (quitar puntos y guión)
        String cleanRut = rut.replaceAll("[.\\-]", "");
        
        if (cleanRut.length() < 2) {
            return false;
        }
        
        // Separar número y dígito verificador
        String rutNumber = cleanRut.substring(0, cleanRut.length() - 1);
        char verificationDigit = cleanRut.charAt(cleanRut.length() - 1);
        
        // Validar que el número solo contenga dígitos
        if (!rutNumber.matches("^[0-9]+$")) {
            return false;
        }
        
        // Calcular dígito verificador
        char calculatedDigit = calculateVerificationDigit(rutNumber);
        
        return verificationDigit == calculatedDigit;
    }
    
    /**
     * Calcula el dígito verificador de un RUT
     * @param rutNumber número del RUT sin dígito verificador
     * @return dígito verificador calculado
     */
    public static char calculateVerificationDigit(String rutNumber) {
        int sum = 0;
        int multiplier = 2;
        
        // Recorrer de derecha a izquierda
        for (int i = rutNumber.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(rutNumber.charAt(i)) * multiplier;
            multiplier = multiplier == 7 ? 2 : multiplier + 1;
        }
        
        int remainder = sum % 11;
        int result = 11 - remainder;
        
        if (result == 11) {
            return '0';
        } else if (result == 10) {
            return 'K';
        } else {
            return Character.forDigit(result, 10);
        }
    }
    
    /**
     * Formatea un RUT limpio al formato estándar 12.345.678-9
     * @param rut RUT sin formato
     * @return RUT formateado
     */
    public static String formatRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return rut;
        }
        
        // Limpiar el RUT
        String cleanRut = rut.replaceAll("[.\\-\\s]", "").toUpperCase();
        
        if (cleanRut.length() < 2) {
            return rut;
        }
        
        // Separar número y dígito verificador
        String rutNumber = cleanRut.substring(0, cleanRut.length() - 1);
        char verificationDigit = cleanRut.charAt(cleanRut.length() - 1);
        
        // Formatear con puntos
        StringBuilder formattedRut = new StringBuilder();
        int digitCount = 0;
        
        for (int i = rutNumber.length() - 1; i >= 0; i--) {
            if (digitCount > 0 && digitCount % 3 == 0) {
                formattedRut.insert(0, ".");
            }
            formattedRut.insert(0, rutNumber.charAt(i));
            digitCount++;
        }
        
        formattedRut.append("-").append(verificationDigit);
        
        return formattedRut.toString();
    }
    
    /**
     * Limpia un RUT removiendo puntos y guiones
     * @param rut RUT con formato
     * @return RUT limpio
     */
    public static String cleanRut(String rut) {
        if (rut == null) {
            return null;
        }
        return rut.replaceAll("[.\\-\\s]", "").toUpperCase();
    }
    
    /**
     * Valida y formatea un RUT
     * @param rut RUT a validar y formatear
     * @return RUT formateado si es válido, null si es inválido
     */
    public static String validateAndFormat(String rut) {
        if (isValidRut(rut)) {
            return formatRut(rut);
        }
        return null;
    }
}