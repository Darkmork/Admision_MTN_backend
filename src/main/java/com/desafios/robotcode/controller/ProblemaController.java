package com.desafios.robotcode.controller;

import com.desafios.robotcode.dto.ProblemaDto;
import com.desafios.robotcode.dto.FrontendProblemaDto;
import com.desafios.robotcode.dto.EjemploDto;
import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.model.Tema;
import com.desafios.robotcode.model.Dificultad;
import com.desafios.robotcode.service.ProblemaService;
import com.desafios.robotcode.service.TemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import com.fasterxml.jackson.core.type.TypeReference;

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

    @GetMapping("/condicionales")
    public ResponseEntity<List<ProblemaDto>> getProblemasCondicionales() {
        try {
            // Buscar el tema de condicionales por nombre
            Optional<Tema> temaCondicionales = temaService.findAll().stream()
                .filter(t -> "Condicionales".equals(t.getNombre()))
                .findFirst();
            
            if (temaCondicionales.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Condicionales");
                nuevoTema.setDescripcion("Problemas de condicionales en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaCondicionales = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaCondicionales.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/loops")
    public ResponseEntity<List<ProblemaDto>> getProblemasLoops() {
        try {
            // Buscar el tema de loops por nombre
            Optional<Tema> temaLoops = temaService.findAll().stream()
                .filter(t -> "Bucles".equals(t.getNombre()))
                .findFirst();
            
            if (temaLoops.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Bucles");
                nuevoTema.setDescripcion("Problemas de bucles y estructuras de repetición en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaLoops = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaLoops.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/funciones")
    public ResponseEntity<List<ProblemaDto>> getProblemasFunciones() {
        try {
            // Buscar el tema de funciones por nombre
            Optional<Tema> temaFunciones = temaService.findAll().stream()
                .filter(t -> "Funciones".equals(t.getNombre()))
                .findFirst();
            
            if (temaFunciones.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Funciones");
                nuevoTema.setDescripcion("Problemas de funciones y programación modular en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaFunciones = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaFunciones.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/listas")
    public ResponseEntity<List<ProblemaDto>> getProblemasListas() {
        try {
            // Buscar el tema de listas por nombre
            Optional<Tema> temaListas = temaService.findAll().stream()
                .filter(t -> "Listas y Arrays".equals(t.getNombre()))
                .findFirst();
            
            if (temaListas.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Listas y Arrays");
                nuevoTema.setDescripcion("Problemas de listas y arrays en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaListas = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaListas.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/diccionarios")
    public ResponseEntity<List<ProblemaDto>> getProblemasDiccionarios() {
        try {
            // Buscar el tema de diccionarios por nombre
            Optional<Tema> temaDiccionarios = temaService.findAll().stream()
                .filter(t -> "Diccionarios".equals(t.getNombre()))
                .findFirst();
            
            if (temaDiccionarios.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Diccionarios");
                nuevoTema.setDescripcion("Problemas de diccionarios y estructuras de datos clave-valor en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaDiccionarios = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaDiccionarios.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/algoritmos")
    public ResponseEntity<List<ProblemaDto>> getProblemasAlgoritmos() {
        try {
            // Buscar el tema de algoritmos por nombre
            Optional<Tema> temaAlgoritmos = temaService.findAll().stream()
                .filter(t -> "Algoritmos".equals(t.getNombre()))
                .findFirst();
            
            if (temaAlgoritmos.isEmpty()) {
                // Si no existe el tema, crear uno
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Algoritmos");
                nuevoTema.setDescripcion("Problemas de algoritmos y estructuras de datos en Python");
                nuevoTema.setDificultad(Dificultad.EASY);
                temaAlgoritmos = Optional.of(temaService.save(nuevoTema));
            }
            
            List<ProblemaDto> problemas = problemaService.findByTema(temaAlgoritmos.get()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/poo")
    public ResponseEntity<List<ProblemaDto>> getProblemasPOO() {
        try {
            // Buscar problemas de POO por rango de IDs (701-767)
            List<ProblemaDto> problemas = problemaService.findAll().stream()
                .filter(p -> p.getId() >= 701 && p.getId() <= 767)
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/data-science")
    public ResponseEntity<List<ProblemaDto>> getProblemasDataScience() {
        try {
            // Buscar problemas de ciencia de datos por rango de IDs (801-867)
            List<ProblemaDto> problemas = problemaService.findAll().stream()
                .filter(p -> p.getId() >= 801 && p.getId() <= 867)
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/machine-learning")
    public ResponseEntity<List<ProblemaDto>> getProblemasMachineLearning() {
        try {
            // Buscar problemas de machine learning por rango de IDs (901-967)
            List<ProblemaDto> problemas = problemaService.findAll().stream()
                .filter(p -> p.getId() >= 901 && p.getId() <= 967)
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(problemas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
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
            
            for (FrontendProblemaDto frontendProblema : problemasDelFrontend) {
                try {
                    // Convertir ID del frontend al formato del backend
                    Long backendId = convertirFrontendIdABackendId(frontendProblema.getId());
                    
                    System.out.println("Procesando: " + frontendProblema.getId() + " -> ID Backend: " + backendId);
                    
                    // Siempre crear un nuevo problema (no actualizar existentes)
                    Long calculatedId = convertirFrontendIdABackendId(frontendProblema.getId());
                    
                    Problema problema = new Problema();
                    problema.setId(calculatedId);
                    problemasCreados++;
                    System.out.println("  - Creando nuevo problema con ID calculado: " + calculatedId);
                    
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
            
            String resultado = String.format("Migración completada: %d problemas creados", 
                problemasCreados);
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

    // Endpoint temporal para debuggear problema 401
    @GetMapping("/debug/401")
    public ResponseEntity<Map<String, Object>> debugProblema401() {
        try {
            Optional<Problema> problemaOpt = problemaService.findById(401L);
            if (problemaOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "error", "Problema 401 no encontrado"
                ));
            }
            
            Problema problema = problemaOpt.get();
            Map<String, Object> debugInfo = new HashMap<>();
            
            debugInfo.put("id", problema.getId());
            debugInfo.put("titulo", problema.getTitulo());
            debugInfo.put("descripcion", problema.getDescripcion());
            debugInfo.put("testCasesJson", problema.getTestCasesJson());
            debugInfo.put("solucionCorrecta", problema.getSolucionCorrecta());
            
            // Parsear test cases
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> testCases = mapper.readValue(
                    problema.getTestCasesJson(), 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                debugInfo.put("testCasesParsed", testCases);
                debugInfo.put("testCasesCount", testCases.size());
            } catch (Exception e) {
                debugInfo.put("parseError", e.getMessage());
            }
            
            // Generar ejemplos
            List<EjemploDto> ejemplos = generarEjemplosDesdeTestCases(problema);
            debugInfo.put("ejemplos", ejemplos);
            debugInfo.put("ejemplosCount", ejemplos.size());
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error debuggeando problema 401: " + e.getMessage()
            ));
        }
    }

    // Endpoint para migrar problemas de listas
    @PostMapping("/migrar-listas")
    public ResponseEntity<String> migrarProblemasListas() {
        try {
            System.out.println("=== MIGRANDO PROBLEMAS DE LISTAS ===");
            
            // Crear o encontrar el tema 'Listas y Arrays'
            Optional<Tema> temaListasOpt = temaService.findAll().stream()
                .filter(t -> "Listas y Arrays".equals(t.getNombre()))
                .findFirst();
            
            Tema temaListas;
            if (temaListasOpt.isEmpty()) {
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Listas y Arrays");
                nuevoTema.setDescripcion("Problemas de listas y arrays en Python");
                temaListas = temaService.save(nuevoTema);
            } else {
                temaListas = temaListasOpt.get();
            }
            
            // Corregir específicamente el problema 401
            Optional<Problema> problema401Opt = problemaService.findById(401L);
            if (problema401Opt.isPresent()) {
                Problema problema401 = problema401Opt.get();
                problema401.setTestCasesJson("[{\"input\":\"acceder_lista()\",\"expectedOutput\":\"2\"}]");
                problemaService.save(problema401);
                System.out.println("✅ Problema 401 corregido");
            } else {
                // Crear el problema 401 si no existe
                Problema problema401 = new Problema();
                problema401.setId(401L);
                problema401.setTitulo("Crear y Acceder");
                problema401.setDescripcion("Crea una lista con los números 1, 2, 3. Luego, devuelve el segundo elemento.");
                problema401.setCodigoInicial("def acceder_lista():\n  mi_lista = [1, 2, 3]\n  # Tu código aquí\n  return None");
                problema401.setSolucionCorrecta("def acceder_lista():\n  mi_lista = [1, 2, 3]\n  return mi_lista[1]");
                problema401.setTestCasesJson("[{\"input\":\"acceder_lista()\",\"expectedOutput\":\"2\"}]");
                problema401.setTema(temaListas);
                problema401.setDificultad(Dificultad.EASY);
                problemaService.save(problema401);
                System.out.println("✅ Problema 401 creado");
            }
            
            return ResponseEntity.ok("Migración de problemas de listas completada. Problema 401 corregido.");
            
        } catch (Exception e) {
            String error = "Error durante la migración: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }

    // Endpoint para corregir JSON malformado en todos los problemas
    @PostMapping("/corregir-json")
    public ResponseEntity<Map<String, Object>> corregirJsonMalformado() {
        try {
            System.out.println("=== CORRIGIENDO JSON MALFORMADO ===");
            
            List<Problema> todosLosProblemas = problemaService.findAll();
            Map<String, Object> resultado = new HashMap<>();
            int corregidos = 0;
            int errores = 0;
            List<String> problemasCorregidos = new ArrayList<>();
            
            for (Problema problema : todosLosProblemas) {
                try {
                    String testCasesJson = problema.getTestCasesJson();
                    if (testCasesJson != null && !testCasesJson.isEmpty()) {
                        
                        // Corregir valores booleanos sin comillas
                        String jsonCorregido = testCasesJson
                            .replaceAll(":\\s*True\\s*([,}])", ": \"True\"$1")
                            .replaceAll(":\\s*False\\s*([,}])", ": \"False\"$1")
                            .replaceAll(":\\s*None\\s*([,}])", ": \"None\"$1");
                        
                        // Verificar si el JSON es válido
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.readValue(jsonCorregido, new TypeReference<List<Map<String, Object>>>() {});
                            
                            // Si llegamos aquí, el JSON es válido
                            if (!jsonCorregido.equals(testCasesJson)) {
                                problema.setTestCasesJson(jsonCorregido);
                                problemaService.save(problema);
                                corregidos++;
                                problemasCorregidos.add("ID " + problema.getId() + ": " + problema.getTitulo());
                                System.out.println("✅ Corregido problema " + problema.getId() + ": " + problema.getTitulo());
                            }
                            
                        } catch (Exception e) {
                            System.err.println("❌ Error en problema " + problema.getId() + ": " + e.getMessage());
                            errores++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error procesando problema " + problema.getId() + ": " + e.getMessage());
                    errores++;
                }
            }
            
            resultado.put("totalProblemas", todosLosProblemas.size());
            resultado.put("corregidos", corregidos);
            resultado.put("errores", errores);
            resultado.put("problemasCorregidos", problemasCorregidos);
            
            System.out.println("=== RESUMEN ===");
            System.out.println("Total problemas: " + todosLosProblemas.size());
            System.out.println("Corregidos: " + corregidos);
            System.out.println("Errores: " + errores);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            String error = "Error durante la corrección: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", error));
        }
    }

    // Endpoint para migrar problemas de diccionarios
    @PostMapping("/migrar-diccionarios")
    public ResponseEntity<String> migrarProblemasDiccionarios() {
        try {
            System.out.println("=== MIGRANDO PROBLEMAS DE DICCIONARIOS ===");
            
            // Crear o encontrar el tema 'Diccionarios'
            Optional<Tema> temaDiccionariosOpt = temaService.findAll().stream()
                .filter(t -> "Diccionarios".equals(t.getNombre()))
                .findFirst();
            
            Tema temaDiccionarios;
            if (temaDiccionariosOpt.isEmpty()) {
                Tema nuevoTema = new Tema();
                nuevoTema.setNombre("Diccionarios");
                nuevoTema.setDescripcion("Problemas de diccionarios y estructuras de datos clave-valor en Python");
                temaDiccionarios = temaService.save(nuevoTema);
            } else {
                temaDiccionarios = temaDiccionariosOpt.get();
            }
            
            // Crear algunos problemas de diccionarios de ejemplo
            crearProblemaDiccionario(501L, "Acceder a Valor en Diccionario",
                "Dado un diccionario y una clave, devuelve el valor asociado a esa clave.",
                "def acceder_valor_diccionario(diccionario, clave):\n  # Tu código aquí\n  return None",
                "def acceder_valor_diccionario(diccionario, clave):\n  return diccionario[clave]",
                "[{\"input\":\"{\\\"nombre\\\":\\\"Robot\\\"}, \\\"nombre\\\"\",\"expectedOutput\":\"Robot\"}, {\"input\":\"{\\\"edad\\\":25}, \\\"edad\\\"\",\"expectedOutput\":\"25\"}, {\"input\":\"{\\\"ciudad\\\":\\\"Madrid\\\"}, \\\"ciudad\\\"\",\"expectedOutput\":\"Madrid\"}]",
                temaDiccionarios, Dificultad.EASY);
            
            return ResponseEntity.ok("Migración de problemas de diccionarios completada exitosamente!");
            
        } catch (Exception e) {
            String error = "Error durante la migración: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return ResponseEntity.status(500).body(error);
        }
    }
    
    private void crearProblemaDiccionario(Long id, String titulo, String descripcion, String codigoInicial, 
                                        String solucionCorrecta, String testCasesJson, Tema tema, Dificultad dificultad) {
        if (!problemaService.findById(id).isPresent()) {
            Problema problema = new Problema();
            problema.setId(id);
            problema.setTitulo(titulo);
            problema.setDescripcion(descripcion);
            problema.setCodigoInicial(codigoInicial);
            problema.setSolucionCorrecta(solucionCorrecta);
            problema.setTestCasesJson(testCasesJson);
            problema.setTema(tema);
            problema.setDificultad(dificultad);
            problemaService.save(problema);
            System.out.println("✅ Problema de diccionario creado: " + titulo);
        }
    }

    // Endpoint para verificar problemas de diccionarios faltantes
    @GetMapping("/verificar-diccionarios")
    public ResponseEntity<Map<String, Object>> verificarProblemasDiccionarios() {
        try {
            System.out.println("=== VERIFICANDO PROBLEMAS DE DICCIONARIOS ===");
            
            // Buscar el tema de diccionarios
            Optional<Tema> temaDiccionariosOpt = temaService.findAll().stream()
                .filter(t -> "Diccionarios".equals(t.getNombre()))
                .findFirst();
            
            if (temaDiccionariosOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "error", "Tema 'Diccionarios' no encontrado",
                    "totalEsperado", 26,
                    "totalEncontrado", 0
                ));
            }
            
            Tema temaDiccionarios = temaDiccionariosOpt.get();
            List<Problema> problemasExistentes = problemaService.findByTema(temaDiccionarios);
            
            // IDs esperados para diccionarios (501-567)
            List<Long> idsEsperados = new ArrayList<>();
            for (long i = 501; i <= 512; i++) idsEsperados.add(i); // Fácil (12)
            for (long i = 531; i <= 537; i++) idsEsperados.add(i); // Intermedio (7)
            for (long i = 561; i <= 567; i++) idsEsperados.add(i); // Difícil (7)
            
            List<Long> idsExistentes = problemasExistentes.stream()
                .map(Problema::getId)
                .collect(Collectors.toList());
            
            List<Long> idsFaltantes = idsEsperados.stream()
                .filter(id -> !idsExistentes.contains(id))
                .collect(Collectors.toList());
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("totalEsperado", 26);
            resultado.put("totalEncontrado", problemasExistentes.size());
            resultado.put("idsExistentes", idsExistentes);
            resultado.put("idsFaltantes", idsFaltantes);
            resultado.put("problemasFaltantes", idsFaltantes.size());
            
            System.out.println("📊 Problemas de diccionarios encontrados: " + problemasExistentes.size() + "/26");
            if (!idsFaltantes.isEmpty()) {
                System.out.println("❌ IDs faltantes: " + idsFaltantes);
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            String error = "Error durante la verificación: " + e.getMessage();
            System.err.println(error);
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", error));
        }
    }

    // Endpoint para completar problemas de diccionarios faltantes
    @PostMapping("/completar-diccionarios")
    public ResponseEntity<String> completarProblemasDiccionarios() {
        try {
            System.out.println("=== COMPLETANDO PROBLEMAS DE DICCIONARIOS ===");
            
            // Buscar el tema de diccionarios
            Optional<Tema> temaDiccionariosOpt = temaService.findAll().stream()
                .filter(t -> "Diccionarios".equals(t.getNombre()))
                .findFirst();
            
            if (temaDiccionariosOpt.isEmpty()) {
                return ResponseEntity.ok("Tema 'Diccionarios' no encontrado");
            }
            
            Tema temaDiccionarios = temaDiccionariosOpt.get();
            List<Problema> problemasExistentes = problemaService.findByTema(temaDiccionarios);
            
            // IDs esperados para diccionarios
            List<Long> idsEsperados = new ArrayList<>();
            for (long i = 501; i <= 512; i++) idsEsperados.add(i); // Fácil (12)
            for (long i = 531; i <= 537; i++) idsEsperados.add(i); // Intermedio (7)
            for (long i = 561; i <= 567; i++) idsEsperados.add(i); // Difícil (7)
            
            List<Long> idsExistentes = problemasExistentes.stream()
                .map(Problema::getId)
                .collect(Collectors.toList());
            
            List<Long> idsFaltantes = idsEsperados.stream()
                .filter(id -> !idsExistentes.contains(id))
                .collect(Collectors.toList());
            
            System.out.println("📊 Problemas existentes: " + problemasExistentes.size() + "/26");
            System.out.println("❌ IDs faltantes: " + idsFaltantes);
            
            // Crear los problemas faltantes
            int creados = 0;
            for (Long id : idsFaltantes) {
                // Agregar problemas faltantes según sea necesario
                // Por ahora no hay problemas faltantes ya que el rango está corregido
            }
            
            return ResponseEntity.ok("Completados " + creados + " problemas de diccionarios faltantes. Total: " + (problemasExistentes.size() + creados) + "/26");
            
        } catch (Exception e) {
            String error = "Error completando problemas: " + e.getMessage();
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
        if (problema.getDificultad() != null) {
            dto.setDificultad(problema.getDificultad().name());
        }
        // Mapear tema a topic para el frontend
        if (problema.getTema() != null) {
            dto.setTopic(mapearTemaATopic(problema.getTema().getNombre()));
        }
        
        // Generar ejemplos basados en los casos de prueba
        dto.setEjemplos(generarEjemplosDesdeTestCases(problema));
        
        return dto;
    }
    
    private String mapearTemaATopic(String nombreTema) {
        switch (nombreTema) {
            case "Condicionales": return "conditionals";
            case "Bucles": return "loops";
            case "Funciones": return "functions";
            case "Listas y Arrays": return "lists";
            case "Diccionarios": return "dictionaries";
            case "Algoritmos": return "algorithms";
            case "Clases en Python": return "pythonClasses";
            case "Ciencia de Datos": return "dataScience";
            case "Machine Learning": return "machineLearning";
            default: return "general";
        }
    }
    
    private List<EjemploDto> generarEjemplosDesdeTestCases(Problema problema) {
        List<EjemploDto> ejemplos = new ArrayList<>();
        
        try {
            if (problema.getTestCasesJson() != null && !problema.getTestCasesJson().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> testCases = mapper.readValue(
                    problema.getTestCasesJson(), 
                    new TypeReference<List<Map<String, Object>>>() {}
                );
                
                // Tomar los primeros 3 casos de prueba como ejemplos
                int maxEjemplos = Math.min(3, testCases.size());
                for (int i = 0; i < maxEjemplos; i++) {
                    Map<String, Object> testCase = testCases.get(i);
                    
                    EjemploDto ejemplo = new EjemploDto();
                    ejemplo.setEntrada(testCase.get("input").toString());
                    ejemplo.setSalida(testCase.get("expectedOutput").toString());
                    ejemplo.setDescripcion("Ejemplo " + (i + 1));
                    
                    ejemplos.add(ejemplo);
                }
            }
        } catch (Exception e) {
            // Si hay error al parsear, crear ejemplos genéricos basados en el problema
            System.err.println("Error generando ejemplos para problema " + problema.getId() + ": " + e.getMessage());
            ejemplos = generarEjemplosGenericos(problema);
        }
        
        return ejemplos;
    }
    
    private List<EjemploDto> generarEjemplosGenericos(Problema problema) {
        List<EjemploDto> ejemplos = new ArrayList<>();
        
        // Generar ejemplos básicos basados en el título del problema
        String titulo = problema.getTitulo().toLowerCase();
        
        if (titulo.contains("edad") || titulo.contains("mayor")) {
            ejemplos.add(crearEjemplo("18", "Mayor de edad", "Edad de mayoría"));
            ejemplos.add(crearEjemplo("16", "Menor de edad", "Edad menor"));
            ejemplos.add(crearEjemplo("21", "Mayor de edad", "Edad adulta"));
        } else if (titulo.contains("par") || titulo.contains("impar")) {
            ejemplos.add(crearEjemplo("4", "Par", "Número par"));
            ejemplos.add(crearEjemplo("7", "Impar", "Número impar"));
            ejemplos.add(crearEjemplo("10", "Par", "Número par"));
        } else if (titulo.contains("positivo") || titulo.contains("negativo")) {
            ejemplos.add(crearEjemplo("5", "Positivo", "Número positivo"));
            ejemplos.add(crearEjemplo("-3", "Negativo", "Número negativo"));
            ejemplos.add(crearEjemplo("0", "Cero", "Número cero"));
        } else if (titulo.contains("diccionario") || titulo.contains("dict")) {
            ejemplos.add(crearEjemplo("{\"nombre\": \"Robot\"}", "Robot", "Acceso a valor"));
            ejemplos.add(crearEjemplo("{\"edad\": 25}", "25", "Valor numérico"));
            ejemplos.add(crearEjemplo("{\"activo\": true}", "True", "Valor booleano"));
        } else {
            // Ejemplos genéricos
            ejemplos.add(crearEjemplo("entrada1", "salida1", "Ejemplo 1"));
            ejemplos.add(crearEjemplo("entrada2", "salida2", "Ejemplo 2"));
            ejemplos.add(crearEjemplo("entrada3", "salida3", "Ejemplo 3"));
        }
        
        return ejemplos;
    }
    
    private EjemploDto crearEjemplo(String entrada, String salida, String descripcion) {
        EjemploDto ejemplo = new EjemploDto();
        ejemplo.setEntrada(entrada);
        ejemplo.setSalida(salida);
        ejemplo.setDescripcion(descripcion);
        return ejemplo;
    }
}
