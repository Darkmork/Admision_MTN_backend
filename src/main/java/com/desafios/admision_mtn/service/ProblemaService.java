package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Dificultad;
import com.desafios.admision_mtn.model.Problema;
import com.desafios.admision_mtn.model.Tema;
import com.desafios.admision_mtn.repository.ProblemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.desafios.admision_mtn.model.Dificultad.EASY;
import static com.desafios.admision_mtn.model.Dificultad.HARD;

@Service
public class ProblemaService {

    private final ProblemaRepository problemaRepository;

    public ProblemaService(ProblemaRepository problemaRepository) {
        this.problemaRepository = problemaRepository;
    }

    public List<Problema> findAll() {
        return problemaRepository.findAll();
    }

    public Optional<Problema> findById(Long id) {
        return problemaRepository.findById(id);
    }

    public List<Problema> findByTema(Tema tema) {
        return problemaRepository.findByTema(tema);
    }

    public Problema save(Problema problema) {
        return problemaRepository.save(problema);
    }

    public long count() {
        return problemaRepository.count();
    }

    public void deleteAll() {
        problemaRepository.deleteAll();
    }

    public Problema saveWithId(Problema problema) {
        // Forzar guardado con ID específico
        return problemaRepository.save(problema);
    }

    public int obtenerPuntajePorDificultad(Dificultad dificultad) {
        switch (dificultad) {
            case HARD:
                return 100;
            case INTERMEDIATE:
                return 70;
            case EASY:
                return 50;
            default:
                return 0;
        }
    }
}