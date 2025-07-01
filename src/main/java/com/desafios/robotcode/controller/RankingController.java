package com.desafios.robotcode.controller;

import com.desafios.robotcode.dto.RankingDto;
import com.desafios.robotcode.model.Ranking;
import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.service.RankingService;
import com.desafios.robotcode.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;
    private final UsuarioService usuarioService;

    public RankingController(RankingService rankingService, UsuarioService usuarioService) {
        this.rankingService = rankingService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<RankingDto> getRanking() {
        return rankingService.findAllByOrderByPuntajeDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<RankingDto> getRankingPorUsuario(@PathVariable Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioService.findById(usuarioId);
        if (usuarioOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Ranking> rankingOpt = rankingService.findByUsuario(usuarioOpt.get());
        return rankingOpt.map(r -> ResponseEntity.ok(toDto(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RankingDto saveRanking(@RequestBody RankingDto dto) {
        Ranking ranking = new Ranking();
        if (dto.getUsuarioId() != null) {
            usuarioService.findById(dto.getUsuarioId()).ifPresent(ranking::setUsuario);
        }
        ranking.setPuntaje(dto.getPuntaje());

        Ranking saved = rankingService.save(ranking);
        return toDto(saved);
    }

    private RankingDto toDto(Ranking r) {
        RankingDto dto = new RankingDto();
        dto.setId(r.getId());
        dto.setUsuarioId(r.getUsuario() != null ? r.getUsuario().getId() : null);
        dto.setPuntaje(r.getPuntaje());
        dto.setFechaActualizacion(r.getFechaActualizacion());
        return dto;
    }
}
