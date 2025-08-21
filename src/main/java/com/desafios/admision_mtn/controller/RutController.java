package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.util.RutUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para utilidades de RUT
 */
@RestController
@RequestMapping("/api/rut")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
@Slf4j
public class RutController {

    /**
     * Valida un RUT chileno
     * @param rut RUT a validar
     * @return resultado de la validación
     */
    @GetMapping("/validate/{rut}")
    public ResponseEntity<Map<String, Object>> validateRut(@PathVariable String rut) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = RutUtil.isValidRut(rut);
            String formattedRut = isValid ? RutUtil.formatRut(rut) : null;
            String cleanRut = RutUtil.cleanRut(rut);
            
            response.put("rut", rut);
            response.put("cleanRut", cleanRut);
            response.put("isValid", isValid);
            response.put("formattedRut", formattedRut);
            
            if (isValid) {
                String rutNumber = cleanRut.substring(0, cleanRut.length() - 1);
                char calculatedDigit = RutUtil.calculateVerificationDigit(rutNumber);
                response.put("calculatedDigit", String.valueOf(calculatedDigit));
            }
            
            log.info("Validación de RUT: {} -> {}", rut, isValid);
            
        } catch (Exception e) {
            log.error("Error validando RUT: {}", rut, e);
            response.put("rut", rut);
            response.put("isValid", false);
            response.put("error", "Error al validar el RUT: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Formatea un RUT al formato estándar
     * @param rut RUT a formatear
     * @return RUT formateado
     */
    @GetMapping("/format/{rut}")
    public ResponseEntity<Map<String, Object>> formatRut(@PathVariable String rut) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String formatted = RutUtil.formatRut(rut);
            boolean isValid = RutUtil.isValidRut(rut);
            
            response.put("originalRut", rut);
            response.put("formattedRut", formatted);
            response.put("isValid", isValid);
            
        } catch (Exception e) {
            log.error("Error formateando RUT: {}", rut, e);
            response.put("originalRut", rut);
            response.put("error", "Error al formatear el RUT: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Calcula el dígito verificador para un número de RUT
     * @param rutNumber número del RUT sin dígito verificador
     * @return dígito verificador calculado
     */
    @GetMapping("/calculate-digit/{rutNumber}")
    public ResponseEntity<Map<String, Object>> calculateDigit(@PathVariable String rutNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar que solo contenga números
            if (!rutNumber.matches("^[0-9]+$")) {
                response.put("error", "El número de RUT debe contener solo dígitos");
                return ResponseEntity.badRequest().body(response);
            }
            
            char digit = RutUtil.calculateVerificationDigit(rutNumber);
            String completeRut = rutNumber + digit;
            String formattedRut = RutUtil.formatRut(completeRut);
            
            response.put("rutNumber", rutNumber);
            response.put("calculatedDigit", String.valueOf(digit));
            response.put("completeRut", completeRut);
            response.put("formattedRut", formattedRut);
            
        } catch (Exception e) {
            log.error("Error calculando dígito para: {}", rutNumber, e);
            response.put("rutNumber", rutNumber);
            response.put("error", "Error al calcular el dígito verificador: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene ejemplos de RUTs válidos para testing
     * @return lista de RUTs de ejemplo
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> response = new HashMap<>();
        
        String[] examples = {
            "12345678-5",
            "9876543-2", 
            "11111111-1",
            "22222222-K",
            "18765432-1",
            "8765432-6"
        };
        
        response.put("examples", examples);
        response.put("note", "Estos son RUTs de ejemplo válidos para testing");
        
        return ResponseEntity.ok(response);
    }
}