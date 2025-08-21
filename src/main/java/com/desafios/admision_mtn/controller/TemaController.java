package com.desafios.admision_mtn.controller;

import com.desafios.admision_mtn.dto.TemaDto;
import com.desafios.admision_mtn.model.Tema;
import com.desafios.admision_mtn.service.TemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/temas")
public class TemaController {

    private final TemaService temaService;

    public TemaController(TemaService temaService) {
        this.temaService = temaService;
    }

    @GetMapping
    public List<TemaDto> getAllTemas() {
        return temaService.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemaDto> getTema(@PathVariable Long id) {
        Optional<Tema> temaOpt = temaService.findById(id);
        return temaOpt.map(tema -> ResponseEntity.ok(toDto(tema)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TemaDto createTema(@RequestBody TemaDto temaDto) {
        Tema tema = new Tema();
        tema.setNombre(temaDto.getNombre());
        tema.setDescripcion(temaDto.getDescripcion());
        tema.setDificultad(temaDto.getDificultad());
        Tema saved = temaService.save(tema);
        return toDto(saved);
    }

    private TemaDto toDto(Tema tema) {
        TemaDto dto = new TemaDto();
        dto.setId(tema.getId());
        dto.setNombre(tema.getNombre());
        dto.setDescripcion(tema.getDescripcion());
        dto.setDificultad(tema.getDificultad());
        return dto;
    }
}
