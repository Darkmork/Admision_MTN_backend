package com.desafios.admision_mtn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Anotación para validar RUT chileno
 * Valida formato y dígito verificador
 */
@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRut {
    String message() default "RUT inválido. Debe tener el formato correcto y dígito verificador válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Si es true, permite valores null o vacíos
     */
    boolean allowEmpty() default false;
}