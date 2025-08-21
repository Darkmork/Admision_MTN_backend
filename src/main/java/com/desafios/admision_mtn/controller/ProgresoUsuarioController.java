package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.ProgresoUsuarioDto;
import com.desafios.admision_mtn.dto.SubmitSolutionDto;
import com.desafios.admision_mtn.dto.UserStatsDto;
import com.desafios.admision_mtn.dto.TestPuntosDto;
import com.desafios.admision_mtn.model.ProgresoUsuario;
import com.desafios.admision_mtn.model.Usuario;
import com.desafios.admision_mtn.model.Problema;
import com.desafios.admision_mtn.model.EstadoProgreso;
import com.desafios.admision_mtn.service.ProgresoUsuarioService;
import com.desafios.admision_mtn.service.UsuarioService;
import com.desafios.admision_mtn.service.ProblemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progresos")
public class ProgresoUsuarioController {

    private final ProgresoUsuarioService progresoUsuarioService;
    private final UsuarioService usuarioService;
    private final ProblemaService problemaService;

    public ProgresoUsuarioController(ProgresoUsuarioService progresoUsuarioService,
                                     UsuarioService usuarioService,
                                     ProblemaService problemaService) {
        this.progresoUsuarioService = progresoUsuarioService;
        this.usuarioService = usuarioService;
        this.problemaService = problemaService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ProgresoUsuarioDto>> getProgresoPorUsuario(@PathVariable Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<ProgresoUsuarioDto> progresos = progresoUsuarioService.findByUsuario(usuarioOpt.get())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(progresos);
    }

    @GetMapping("/usuario/{usuarioId}/problema/{problemaId}")
    public ResponseEntity<ProgresoUsuarioDto> getProgresoPorUsuarioYProblema(@PathVariable Long usuarioId, @PathVariable Long problemaId) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        Optional<Problema> problemaOpt = problemaService.findById(problemaId);
        if (usuarioOpt.isEmpty() || problemaOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<ProgresoUsuario> progresoOpt = progresoUsuarioService.findByUsuarioAndProblema(usuarioOpt.get(), problemaOpt.get());
        return progresoOpt.map(progreso -> ResponseEntity.ok(toDto(progreso)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProgresoUsuarioDto> saveProgreso(@RequestBody ProgresoUsuarioDto dto) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(dto.getUsuarioId());
        Optional<Problema> problemaOpt = problemaService.findById(dto.getProblemaId());
        
        if (usuarioOpt.isEmpty() || problemaOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Buscar progreso existente
        Optional<ProgresoUsuario> existingProgreso = progresoUsuarioService
                .findByUsuarioAndProblema(usuarioOpt.get(), problemaOpt.get());
        
        ProgresoUsuario progreso;
        if (existingProgreso.isPresent()) {
            progreso = existingProgreso.get();
            progreso.setEstado(dto.getEstado());
            progreso.setIntentos(dto.getIntentos());
        } else {
            progreso = new ProgresoUsuario();
            progreso.setUsuario(usuarioOpt.get());
            progreso.setProblema(problemaOpt.get());
            progreso.setEstado(dto.getEstado());
            progreso.setIntentos(dto.getIntentos());
        }

        ProgresoUsuario saved = progresoUsuarioService.save(progreso);
        return ResponseEntity.ok(toDto(saved));
    }

    @PostMapping("/submit")
    public ResponseEntity<ProgresoUsuarioDto> submitSolution(@RequestBody SubmitSolutionDto submitDto) {
        System.out.println("=== SUBMIT SOLUTION LLAMADO ===");
        System.out.println("Usuario ID: " + submitDto.getUsuarioId());
        System.out.println("Problema ID: " + submitDto.getProblemaId());
        System.out.println("Es correcto: " + submitDto.isCorrect());
        System.out.println("Dificultad: " + submitDto.getDificultad());
        System.out.println("Tema ID: " + submitDto.getTemaId());
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(submitDto.getUsuarioId());
        Optional<Problema> problemaOpt = problemaService.findById(submitDto.getProblemaId());
        
        if (usuarioOpt.isEmpty()) {
            System.err.println("ERROR: Usuario no encontrado con ID: " + submitDto.getUsuarioId());
            return ResponseEntity.badRequest().build();
        }
        
        if (problemaOpt.isEmpty()) {
            System.err.println("ERROR: Problema no encontrado con ID: " + submitDto.getProblemaId());
            System.err.println("Problemas disponibles en la BD:");
            problemaService.findAll().forEach(p -> 
                System.err.println("  - ID: " + p.getId() + ", Título: " + p.getTitulo() + ", Dificultad: " + p.getDificultad()));
            return ResponseEntity.badRequest().build();
        }
        
        System.out.println("Usuario encontrado: " + usuarioOpt.get().getUsername());
        System.out.println("Problema encontrado: " + problemaOpt.get().getTitulo());

        // Buscar progreso existente o crear nuevo
        Optional<ProgresoUsuario> existingProgreso = progresoUsuarioService
                .findByUsuarioAndProblema(usuarioOpt.get(), problemaOpt.get());
        
        ProgresoUsuario progreso;
        if (existingProgreso.isPresent()) {
            progreso = existingProgreso.get();
            progreso.setIntentos(progreso.getIntentos() + 1);
        } else {
            progreso = new ProgresoUsuario();
            progreso.setUsuario(usuarioOpt.get());
            progreso.setProblema(problemaOpt.get());
            progreso.setIntentos(1);
        }
        
        // Verificar si el usuario ya había resuelto este problema antes
        Optional<ProgresoUsuario> progresoExistente = progresoUsuarioService
                .findByUsuarioAndProblema(usuarioOpt.get(), problemaOpt.get());
        
        boolean yaResueltoAntes = progresoExistente
                .map(p -> p.getEstado() == EstadoProgreso.SOLVED)
                .orElse(false);

        // Determinar estado basado en si la solución es correcta
        if (submitDto.isCorrect()) {
            progreso.setEstado(EstadoProgreso.SOLVED);
            
            // Otorgar puntos solo si es la primera vez que resuelve el problema
            if (!yaResueltoAntes) {
                // Usar dificultad del DTO si está disponible, sino usar la del problema
                String dificultadParaPuntos = submitDto.getDificultad() != null ? 
                    submitDto.getDificultad() : 
                    problemaOpt.get().getDificultad().toString();
                
                System.out.println("=== CALCULANDO PUNTOS ===");
                System.out.println("Dificultad para puntos: '" + dificultadParaPuntos + "'");
                System.out.println("Dificultad del DTO: '" + submitDto.getDificultad() + "'");
                System.out.println("Dificultad del problema BD: '" + problemaOpt.get().getDificultad() + "'");
                
                int puntos = progresoUsuarioService.obtenerPuntajePorDificultad(dificultadParaPuntos);
                System.out.println("Puntos calculados: " + puntos);
                
                // Actualizar puntaje del usuario
                Usuario usuario = usuarioOpt.get();
                int puntajeAnterior = usuario.getPuntaje();
                usuario.setPuntaje(puntajeAnterior + puntos);
                
                System.out.println("Puntaje anterior: " + puntajeAnterior);
                System.out.println("Puntaje nuevo: " + usuario.getPuntaje());
                
                Usuario usuarioGuardado = usuarioService.save(usuario); // Guardar el usuario con el nuevo puntaje
                System.out.println("Usuario guardado con puntaje: " + usuarioGuardado.getPuntaje());
                System.out.println("=========================");
                
                System.out.println("Puntos otorgados: " + puntos + " al usuario: " + usuario.getUsername());
            } else {
                System.out.println("Problema ya resuelto antes, no se otorgan puntos.");
            }
        } else {
            progreso.setEstado(EstadoProgreso.IN_PROGRESS);
        }

        ProgresoUsuario saved = progresoUsuarioService.save(progreso);
        return ResponseEntity.ok(toDto(saved));
    }

    // Endpoint de test para asignar puntos directamente
    @PostMapping("/test-puntos")
    public ResponseEntity<String> testAsignarPuntos(@RequestBody TestPuntosDto testDto) {
        System.out.println("=== TEST ASIGNAR PUNTOS ===");
        System.out.println("Usuario ID: " + testDto.getUsuarioId());
        System.out.println("Puntos a asignar: " + testDto.getPuntos());
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(testDto.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        System.out.println("Usuario encontrado: " + usuario.getUsername());
        System.out.println("Puntaje actual: " + usuario.getPuntaje());
        
        int puntajeAnterior = usuario.getPuntaje();
        usuario.setPuntaje(puntajeAnterior + testDto.getPuntos());
        
        try {
            Usuario saved = usuarioService.save(usuario);
            System.out.println("Usuario guardado - Puntaje final: " + saved.getPuntaje());
            return ResponseEntity.ok("Puntos asignados exitosamente. Puntaje anterior: " + puntajeAnterior + ", Puntaje nuevo: " + saved.getPuntaje());
        } catch (Exception e) {
            System.err.println("Error guardando usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error guardando: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}/estadisticas")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable Long usuarioId) {
        System.out.println("=== CALCULANDO ESTADÍSTICAS PARA USUARIO " + usuarioId + " ===");
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProgresoUsuario> progresos = progresoUsuarioService.findByUsuario(usuarioOpt.get());
        System.out.println("Total registros de progreso encontrados: " + progresos.size());
        
        // Debug: mostrar todos los progresos
        progresos.forEach(p -> {
            System.out.println("  - Problema ID: " + p.getProblema().getId() + 
                             ", Estado: " + p.getEstado() + 
                             ", Intentos: " + p.getIntentos());
        });
        
        long totalProblemas = progresos.size();
        long problemasSolved = progresos.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.SOLVED)
                .count();
        long problemasInProgress = progresos.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.IN_PROGRESS)
                .count();
                
        System.out.println("Estadísticas calculadas:");
        System.out.println("  - Total problemas: " + totalProblemas);
        System.out.println("  - Problemas resueltos: " + problemasSolved);
        System.out.println("  - Problemas en progreso: " + problemasInProgress);
        System.out.println("  - Puntaje usuario: " + usuarioOpt.get().getPuntaje());

        UserStatsDto stats = new UserStatsDto();
        stats.setUsuarioId(usuarioId);
        stats.setTotalProblemas(totalProblemas);
        stats.setProblemasSolved(problemasSolved);
        stats.setProblemasInProgress(problemasInProgress);
        stats.setPuntaje(usuarioOpt.get().getPuntaje());
        
        System.out.println("==============================");
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/usuario/{usuarioId}/tema/{temaId}/estadisticas")
    public ResponseEntity<UserStatsDto> getUserStatsByTheme(@PathVariable Long usuarioId, @PathVariable String temaId) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProgresoUsuario> todosProgresos = progresoUsuarioService.findByUsuario(usuarioOpt.get());
        
        // Filtrar por tema usando el mapping de IDs
        List<ProgresoUsuario> progresosPorTema = todosProgresos.stream()
                .filter(p -> {
                    // Simplificado: asumir que los IDs del backend siguen el patrón de mapping
                    long problemaId = p.getProblema().getId();
                    return isProblemaInTema(problemaId, temaId);
                })
                .toList();
        
        long totalProblemas = progresosPorTema.size();
        long problemasSolved = progresosPorTema.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.SOLVED)
                .count();
        long problemasInProgress = progresosPorTema.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.IN_PROGRESS)
                .count();

        UserStatsDto stats = new UserStatsDto();
        stats.setUsuarioId(usuarioId);
        stats.setTotalProblemas(totalProblemas);
        stats.setProblemasSolved(problemasSolved);
        stats.setProblemasInProgress(problemasInProgress);
        stats.setPuntaje(usuarioOpt.get().getPuntaje()); // Puntaje total, no por tema
        
        return ResponseEntity.ok(stats);
    }

    private boolean isProblemaInTema(long problemaId, String temaId) {
        // Mapeo basado en el sistema de IDs que creamos
        int temaBase = switch (temaId) {
            case "conditionals" -> 100;
            case "loops" -> 200;
            case "functions" -> 300;
            case "lists" -> 400;
            case "dictionaries" -> 500;
            case "algorithms" -> 600;
            case "pythonClasses" -> 700;
            case "dataScience" -> 800;
            case "machineLearning" -> 900;
            default -> 100;
        };
        
        return problemaId >= temaBase && problemaId < (temaBase + 100);
    }

    private ProgresoUsuarioDto toDto(ProgresoUsuario progreso) {
        ProgresoUsuarioDto dto = new ProgresoUsuarioDto();
        dto.setId(progreso.getId());
        dto.setUsuarioId(progreso.getUsuario() != null ? progreso.getUsuario().getId() : null);
        dto.setProblemaId(progreso.getProblema() != null ? progreso.getProblema().getId() : null);
        dto.setEstado(progreso.getEstado());
        dto.setIntentos(progreso.getIntentos());
        dto.setUltimaModificacion(progreso.getUltimaModificacion());
        return dto;
    }
}
