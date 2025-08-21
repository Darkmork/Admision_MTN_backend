package com.desafios.admision_mtn.validation;

import com.desafios.admision_mtn.util.RutUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación @ValidRut
 */
public class RutValidator implements ConstraintValidator<ValidRut, String> {
    
    private boolean allowEmpty;
    
    @Override
    public void initialize(ValidRut constraintAnnotation) {
        this.allowEmpty = constraintAnnotation.allowEmpty();
    }
    
    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        // Si permite valores vacíos y el valor es null o vacío
        if (allowEmpty && (rut == null || rut.trim().isEmpty())) {
            return true;
        }
        
        // Si no permite vacíos y el valor es null o vacío
        if (!allowEmpty && (rut == null || rut.trim().isEmpty())) {
            return false;
        }
        
        // Validar el RUT usando la utilidad
        return RutUtil.isValidRut(rut);
    }
}