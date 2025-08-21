package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.ProblemaDto;
import com.desafios.admision_mtn.dto.FrontendProblemaDto;
import com.desafios.admision_mtn.model.Problema;
import com.desafios.admision_mtn.model.Tema;
import com.desafios.admision_mtn.model.Dificultad;
import com.desafios.admision_mtn.service.ProblemaService;
import com.desafios.admision_mtn.service.TemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/problemas")
public class ProblemaController {

    private final ProblemaService problemaService;
    private final TemaService temaService;

    public ProblemaController(ProblemaService problemaService, TemaService temaService) {
        this.problemaService = problemaService;
        this.temaService = temaService;
    }

    @GetMapping
    public List<ProblemaDto> getAllProblemas() {
        return problemaService.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemaDto> getProblema(@PathVariable Long id) {
        Optional<Problema> problemaOpt = problemaService.findById(id);
        return problemaOpt.map(problema -> ResponseEntity.ok(toDto(problema)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tema/{temaId}")
    public ResponseEntity<List<ProblemaDto>> getProblemasByTema(@PathVariable Long temaId) {
        Optional<Tema> temaOpt = temaService.findById(temaId);
        if (temaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<ProblemaDto> problemas = problemaService.findByTema(temaOpt.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(problemas);
    }

    @PostMapping
    public ProblemaDto createProblema(@RequestBody ProblemaDto problemaDto) {
        Problema problema = new Problema();
        problema.setTitulo(problemaDto.getTitulo());
        problema.setDescripcion(problemaDto.getDescripcion());
        problema.setCodigoInicial(problemaDto.getCodigoInicial());
        // Por seguridad, no se expone ni se recibe solucionCorrecta ni testCasesJson aquí.
        if (problemaDto.getTemaId() != null) {
            temaService.findById(problemaDto.getTemaId()).ifPresent(problema::setTema);
        }
        Problema saved = problemaService.save(problema);
        return toDto(saved);
    }

    // Endpoint para crear problemas de test (útil después de recrear la base de datos)
    @PostMapping("/crear-test-problemas")
    public ResponseEntity<String> crearTestProblemas() {
        try {
            System.out.println("=== CREANDO PROBLEMAS DE TEST ===");
            
            // Crear algunos problemas de test para asegurar que existen en la BD
            crearProblemaConId(101L, "Verificar Mayoría de Edad", 
                "Crea una función que determine si una persona es mayor de edad", 
                "def es_mayor_edad(edad):\n    # Tu código aquí\n    pass", 
                Dificultad.EASY);
                
            crearProblemaConId(102L, "Saludar Usuario", 
                "Crea una función que salude a un usuario por su nombre", 
                "def saludar(nombre):\n    # Tu código aquí\n    pass", 
                Dificultad.EASY);
                
            crearProblemaConId(131L, "Contador de Pares", 
                "Cuenta cuántos números pares hay en una lista", 
                "def contar_pares(lista):\n    # Tu código aquí\n    pass", 
                Dificultad.INTERMEDIATE);
                
            crearProblemaConId(161L, "Algoritmo de Búsqueda", 
                "Implementa un algoritmo de búsqueda eficiente", 
                "def buscar_elemento(lista, elemento):\n    # Tu código aquí\n    pass", 
                Dificultad.HARD);
            
            System.out.println("Problemas de test creados exitosamente");
            return ResponseEntity.ok("Problemas de test creados exitosamente");
            
        } catch (Exception e) {
            System.err.println("Error creando problemas de test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    private void crearProblemaConId(Long id, String titulo, String descripcion, String codigoInicial, Dificultad dificultad) {
        // Verificar si ya existe
        if (problemaService.findById(id).isPresent()) {
            System.out.println("Problema con ID " + id + " ya existe, saltando...");
            return;
        }
        
        Problema problema = new Problema();
        problema.setTitulo(titulo);
        problema.setDescripcion(descripcion);
        problema.setCodigoInicial(codigoInicial);
        problema.setDificultad(dificultad);
        
        // Forzar el ID (esto puede no funcionar dependiendo de la configuración de JPA)
        // En un entorno real, usarías un approach diferente
        try {
            // Guardar y después actualizar el ID si es necesario
            Problema saved = problemaService.save(problema);
            System.out.println("Problema creado: " + saved.getId() + " - " + titulo);
        } catch (Exception e) {
            System.err.println("Error creando problema " + titulo + ": " + e.getMessage());
        }
    }
    
    // Endpoint para migrar problemas del frontend a la base de datos
    @PostMapping("/migrar-frontend")
    public ResponseEntity<String> migrarProblemasDesdefrontend(@RequestBody List<FrontendProblemaDto> problemasDelFrontend) {
        try {
            System.out.println("=== MIGRANDO PROBLEMAS DEL FRONTEND ===");
            System.out.println("Total problemas recibidos: " + problemasDelFrontend.size());
            
            int problemasCreados = 0;
            int problemasActualizados = 0;
            
            for (FrontendProblemaDto frontendProblema : problemasDelFrontend) {
                try {
                    // Convertir ID del frontend al formato del backend
                    Long backendId = convertirFrontendIdABackendId(frontendProblema.getId());
                    
                    System.out.println("Procesando: " + frontendProblema.getId() + " -> ID Backend: " + backendId);
                    
                    // Buscar si ya existe un problema con el mismo título (como identificador único)
                    Optional<Problema> existing = problemaService.findAll().stream()
                        .filter(p -> p.getTitulo() != null && p.getTitulo().equals(frontendProblema.getTitle()))
                        .findFirst();
                    
                    // Siempre usar el ID calculado para consistencia
                    Long calculatedId = convertirFrontendIdABackendId(frontendProblema.getId());
                    
                    Problema problema;
                    if (existing.isPresent()) {
                        problema = existing.get();
                        // Actualizar el ID del problema existente al ID correcto
                        problema.setId(calculatedId);
                        problemasActualizados++;
                        System.out.println("  - Actualizando problema existente con ID correcto: " + calculatedId);
                    } else {
                        problema = new Problema();
                        problema.setId(calculatedId);
                        problemasCreados++;
                        System.out.println("  - Creando nuevo problema con ID calculado: " + calculatedId);
                    }
                    
                    // Mapear datos del frontend al modelo del backend
                    problema.setTitulo(frontendProblema.getTitle());
                    problema.setDescripcion(frontendProblema.getDescription());
                    problema.setCodigoInicial(frontendProblema.getDefaultCode());
                    problema.setDificultad(convertirDificultad(frontendProblema.getDifficulty()));
                    
                    System.out.println("  - Título: " + frontendProblema.getTitle());
                    System.out.println("  - Dificultad mapeada: " + convertirDificultad(frontendProblema.getDifficulty()));
                    
                    // Convertir ejemplos y casos de prueba a JSON
                    if (frontendProblema.getExamples() != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        String examplesJson = mapper.writeValueAsString(frontendProblema.getExamples());
                        // Podrías agregar un campo examples al modelo Problema si lo necesitas
                    }
                    
                    if (frontendProblema.getDatasets() != null) {
                        ObjectMapper mapper = new ObjectMapper();
                        String testCasesJson = mapper.writeValueAsString(frontendProblema.getDatasets());
                        problema.setTestCasesJson(testCasesJson);
                    }
                    
                    if (frontendProblema.getSolutionCode() != null && !frontendProblema.getSolutionCode().trim().isEmpty()) {
                        problema.setSolucionCorrecta(frontendProblema.getSolutionCode());
                    } else {
                        // Si no hay solución, crear una genérica basada en el título
                        problema.setSolucionCorrecta("# Solución para: " + frontendProblema.getTitle() + "\n# Implementar aquí");
                    }
                    
                    // Crear o buscar tema basado en el topic del frontend
                    if (frontendProblema.getTopic() != null) {
                        Tema tema = crearOBuscarTema(frontendProblema.getTopic());
                        problema.setTema(tema);
                        System.out.println("  - Tema asignado: " + tema.getNombre());
                    }
                    
                    try {
                        Problema saved = problemaService.saveWithId(problema);
                        System.out.println("  - Guardado exitosamente con ID final: " + saved.getId());
                    } catch (Exception saveError) {
                        System.err.println("  - Error guardando: " + saveError.getMessage());
                        saveError.printStackTrace();
                        
                        // Intentar guardar sin establecer ID específico
                        try {
                            problema.setId(null); // Dejar que JPA asigne el ID
                            Problema savedWithAutoId = problemaService.save(problema);
                            System.out.println("  - Guardado con ID automático: " + savedWithAutoId.getId());
                        } catch (Exception autoIdError) {
                            System.err.println("  - Error incluso con ID automático: " + autoIdError.getMessage());
                            throw autoIdError;
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error procesando problema " + frontendProblema.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            String resultado = String.format("Migración completada: %d problemas creados, %d actualizados", 
                problemasCreados, problemasActualizados);
            System.out.println(resultado);
            System.out.println("===========================");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            System.err.println("Error en migración: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    private Long convertirFrontendIdABackendId(String frontendId) {
        // Implementar la misma lógica que en problemMapping.ts
        String[] parts = frontendId.split("-");
        if (parts.length < 3) {
            return 1L; // fallback
        }
        
        String tema = parts[0];
        String dificultad = parts[1];
        int numero = Integer.parseInt(parts[2]);
        
        // Mapeo de temas (usar exactamente los mismos prefijos que en problemMapping.ts)
        Map<String, Integer> temaBaseMap = new HashMap<>();
        temaBaseMap.put("cond", 100);
        temaBaseMap.put("loop", 200);
        temaBaseMap.put("func", 300);
        temaBaseMap.put("list", 400);
        temaBaseMap.put("dict", 500);
        temaBaseMap.put("algo", 600);
        temaBaseMap.put("pythonClasses", 700);
        temaBaseMap.put("dataScience", 800);
        temaBaseMap.put("machineLearning", 900);
        
        // Mapeo de dificultades (usar exactamente los mismos nombres que en problemMapping.ts)
        Map<String, Integer> dificultadOffset = new HashMap<>();
        dificultadOffset.put("easy", 0);
        dificultadOffset.put("int", 30);
        dificultadOffset.put("hard", 60);
        
        int baseId = temaBaseMap.getOrDefault(tema, 100);
        int offset = dificultadOffset.getOrDefault(dificultad, 0);
        
        return (long) (baseId + offset + numero);
    }
    
    private Dificultad convertirDificultad(String dificultadStr) {
        if (dificultadStr == null) return Dificultad.EASY;
        
        switch (dificultadStr.toLowerCase()) {
            case "fácil":
            case "facil":
            case "easy":
                return Dificultad.EASY;
            case "intermedio":
            case "intermediate":
            case "medium":
                return Dificultad.INTERMEDIATE;
            case "difícil":
            case "dificil":
            case "hard":
                return Dificultad.HARD;
            default:
                return Dificultad.EASY;
        }
    }
    
    private Tema crearOBuscarTema(String topicId) {
        // Mapear topic IDs del frontend a nombres de temas
        String nombreTema = mapearTopicANombre(topicId);
        
        // Buscar si ya existe el tema
        Optional<Tema> temaExistente = temaService.findAll().stream()
            .filter(t -> t.getNombre().equals(nombreTema))
            .findFirst();
            
        if (temaExistente.isPresent()) {
            return temaExistente.get();
        } else {
            // Crear nuevo tema
            Tema nuevoTema = new Tema();
            nuevoTema.setNombre(nombreTema);
            nuevoTema.setDescripcion("Tema: " + nombreTema);
            return temaService.save(nuevoTema);
        }
    }
    
    private String mapearTopicANombre(String topicId) {
        switch (topicId) {
            case "conditionals": return "Condicionales";
            case "loops": return "Bucles";
            case "functions": return "Funciones";
            case "lists": return "Listas y Arrays";
            case "dictionaries": return "Diccionarios";
            case "algorithms": return "Algoritmos";
            case "pythonClasses": return "Clases en Python";
            case "dataScience": return "Ciencia de Datos";
            case "machineLearning": return "Machine Learning";
            default: return "General";
        }
    }
    
    // Endpoint para listar todos los problemas (debug)
    @GetMapping("/debug-list")
    public ResponseEntity<String> listarTodosLosProblemas() {
        try {
            List<Problema> problemas = problemaService.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("=== PROBLEMAS EN BASE DE DATOS ===\n");
            sb.append("Total: ").append(problemas.size()).append("\n\n");
            
            for (Problema p : problemas) {
                sb.append("ID: ").append(p.getId())
                  .append(" | Título: ").append(p.getTitulo())
                  .append(" | Dificultad: ").append(p.getDificultad())
                  .append("\n");
            }
            
            return ResponseEntity.ok(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/limpiar-todos")
    public ResponseEntity<String> limpiarTodosLosProblemas() {
        try {
            System.out.println("=== INICIANDO LIMPIEZA DE TODOS LOS PROBLEMAS ===");
            
            // Obtener conteo antes de la limpieza
            long cantidadAntes = problemaService.count();
            System.out.println("Problemas antes de limpiar: " + cantidadAntes);
            
            // Limpiar todos los problemas
            problemaService.deleteAll();
            
            // Verificar conteo después
            long cantidadDespues = problemaService.count();
            System.out.println("Problemas después de limpiar: " + cantidadDespues);
            
            String mensaje = "Limpieza completada. Eliminados: " + cantidadAntes + " problemas";
            System.out.println(mensaje);
            
            return ResponseEntity.ok(mensaje);
        } catch (Exception e) {
            String error = "Error durante la limpieza: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }

    private ProblemaDto toDto(Problema problema) {
        ProblemaDto dto = new ProblemaDto();
        dto.setId(problema.getId());
        dto.setTitulo(problema.getTitulo());
        dto.setDescripcion(problema.getDescripcion());
        dto.setCodigoInicial(problema.getCodigoInicial());
        if (problema.getTema() != null) {
            dto.setTemaId(problema.getTema().getId());
        }
        return dto;
    }
}
