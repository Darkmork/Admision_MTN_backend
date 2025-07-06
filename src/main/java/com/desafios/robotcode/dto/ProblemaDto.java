package com.desafios.robotcode.dto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ProblemaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private String codigoInicial;
    private Long temaId;
    private String dificultad; // EASY, INTERMEDIATE, HARD
    private String topic; // Para mapear con el frontend (conditionals, loops, etc.)
    
    // Ejemplos de entrada y salida para mostrar al usuario
    private List<EjemploDto> ejemplos;

    // No se expone solucionCorrecta ni testCasesJson por seguridad/diseño
    // Agrega getters y setters
}
