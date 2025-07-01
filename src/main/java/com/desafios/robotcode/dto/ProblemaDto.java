package com.desafios.robotcode.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private String codigoInicial;
    private Long temaId;

    // No se expone solucionCorrecta ni testCasesJson por seguridad/diseño
    // Agrega getters y setters
}
