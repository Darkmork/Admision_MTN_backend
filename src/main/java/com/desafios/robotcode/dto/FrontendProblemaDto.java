package com.desafios.robotcode.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FrontendProblemaDto {
    private String id; // ID del frontend como "cond-easy-1"
    private String topic; // Tema como "conditionals"
    private String title; // Título
    private String description; // Descripción
    private String difficulty; // "Fácil", "Intermedio", "Difícil" 
    private List<ExampleDto> examples; // Ejemplos de entrada/salida
    private String defaultCode; // Código inicial
    private String solutionCode; // Solución (opcional)
    private List<DatasetDto> datasets; // Casos de test
    private String authorSignature; // Firma del autor
    
    @Getter
    @Setter
    public static class ExampleDto {
        private String input;
        private String output;
        private String explanation;
    }
    
    @Getter
    @Setter
    public static class DatasetDto {
        private String input;
        private String expectedOutput;
    }
}