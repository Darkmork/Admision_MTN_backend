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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progresos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
// 🔒 SEGURIDAD: Sin @CrossOrigin - usa configuración global de SecurityConfig
public class ProgresoUsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(ProgresoUsuarioController.class);

    private final ProgresoUsuarioService progresoUsuarioService;
    private final UsuarioService usuarioService;
    private final ProblemaService problemaService;

    // Constructor eliminado - usando @RequiredArgsConstructor

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
        logger.info("[SUBMIT_SOLUTION] Iniciando procesamiento - Usuario: {}, Problema: {}, Correcto: {}, Dificultad: {}, Tema: {}", 
                   submitDto.getUsuarioId(), submitDto.getProblemaId(), submitDto.isCorrect(), 
                   submitDto.getDificultad(), submitDto.getTemaId());
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(submitDto.getUsuarioId());
        Optional<Problema> problemaOpt = problemaService.findById(submitDto.getProblemaId());
        
        if (usuarioOpt.isEmpty()) {
            logger.warn("[SUBMIT_SOLUTION] Usuario no encontrado con ID: {}", submitDto.getUsuarioId());
            return ResponseEntity.badRequest().build();
        }
        
        if (problemaOpt.isEmpty()) {
            logger.warn("[SUBMIT_SOLUTION] Problema no encontrado con ID: {}", submitDto.getProblemaId());
            if (logger.isDebugEnabled()) {
                logger.debug("[SUBMIT_SOLUTION] Problemas disponibles:");
                problemaService.findAll().forEach(p -> 
                    logger.debug("  - ID: {}, Título: {}, Dificultad: {}", p.getId(), p.getTitulo(), p.getDificultad()));
            }
            return ResponseEntity.badRequest().build();
        }
        
        logger.debug("[SUBMIT_SOLUTION] Usuario encontrado: {}, Problema: {}", 
                    usuarioOpt.get().getUsername(), problemaOpt.get().getTitulo());

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
                
                logger.info("[SUBMIT_SOLUTION] Calculando puntos - Dificultad: {}, DTO: {}, BD: {}", 
                           dificultadParaPuntos, submitDto.getDificultad(), problemaOpt.get().getDificultad());
                
                int puntos = progresoUsuarioService.obtenerPuntajePorDificultad(dificultadParaPuntos);
                logger.info("[SUBMIT_SOLUTION] Puntos calculados: {}", puntos);
                
                // Actualizar puntaje del usuario
                Usuario usuario = usuarioOpt.get();
                int puntajeAnterior = usuario.getPuntaje();
                usuario.setPuntaje(puntajeAnterior + puntos);
                
                logger.info("[SUBMIT_SOLUTION] Actualizando puntaje - Anterior: {}, Nuevo: {}", 
                           puntajeAnterior, usuario.getPuntaje());
                
                Usuario usuarioGuardado = usuarioService.save(usuario);
                logger.info("[SUBMIT_SOLUTION] Usuario guardado con puntaje: {}", usuarioGuardado.getPuntaje());
                
                logger.info("[SUBMIT_SOLUTION] Puntos otorgados exitosamente - Usuario: {}, Puntos: {}, Puntaje Final: {}", 
                           usuario.getUsername(), puntos, usuarioGuardado.getPuntaje());
            } else {
                logger.debug("[SUBMIT_SOLUTION] Problema ya resuelto anteriormente - Usuario: {}, Problema: {}", 
                            usuarioOpt.get().getUsername(), problemaOpt.get().getTitulo());
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
        logger.info("[TEST_PUNTOS] Iniciando test - Usuario: {}, Puntos: {}", 
                   testDto.getUsuarioId(), testDto.getPuntos());
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(testDto.getUsuarioId());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        
        Usuario usuario = usuarioOpt.get();
        logger.info("[TEST_PUNTOS] Usuario encontrado: {}, Puntaje actual: {}", 
                   usuario.getUsername(), usuario.getPuntaje());
        
        int puntajeAnterior = usuario.getPuntaje();
        usuario.setPuntaje(puntajeAnterior + testDto.getPuntos());
        
        try {
            Usuario saved = usuarioService.save(usuario);
            logger.info("[TEST_PUNTOS] Usuario guardado - Puntaje final: {}", saved.getPuntaje());
            return ResponseEntity.ok("Puntos asignados exitosamente. Puntaje anterior: " + puntajeAnterior + ", Puntaje nuevo: " + saved.getPuntaje());
        } catch (Exception e) {
            logger.error("[TEST_PUNTOS] Error guardando usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error guardando: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}/estadisticas")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable Long usuarioId) {
        logger.info("[USER_STATS] Calculando estadísticas para usuario: {}", usuarioId);
        
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ProgresoUsuario> progresos = progresoUsuarioService.findByUsuario(usuarioOpt.get());
        logger.debug("[USER_STATS] Total registros de progreso encontrados: {}", progresos.size());
        
        if (logger.isDebugEnabled()) {
            logger.debug("[USER_STATS] Detalle de progresos:");
            progresos.forEach(p -> {
                logger.debug("  - Problema ID: {}, Estado: {}, Intentos: {}", 
                           p.getProblema().getId(), p.getEstado(), p.getIntentos());
            });
        }
        
        long totalProblemas = progresos.size();
        long problemasSolved = progresos.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.SOLVED)
                .count();
        long problemasInProgress = progresos.stream()
                .filter(p -> p.getEstado() == EstadoProgreso.IN_PROGRESS)
                .count();
                
        logger.info("[USER_STATS] Resultados - Total: {}, Resueltos: {}, En progreso: {}, Puntaje: {}", 
                   totalProblemas, problemasSolved, problemasInProgress, usuarioOpt.get().getPuntaje());

        UserStatsDto stats = new UserStatsDto();
        stats.setUsuarioId(usuarioId);
        stats.setTotalProblemas(totalProblemas);
        stats.setProblemasSolved(problemasSolved);
        stats.setProblemasInProgress(problemasInProgress);
        stats.setPuntaje(usuarioOpt.get().getPuntaje());
        
        logger.debug("[USER_STATS] Cálculo completado para usuario: {}", usuarioId);
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
