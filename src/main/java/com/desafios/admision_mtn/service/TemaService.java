package com.desafios.admision_mtn.service;

import com.desafios.admision_mtn.model.Tema;
import com.desafios.admision_mtn.repository.TemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemaService {

    private final TemaRepository temaRepository;

    public TemaService(TemaRepository temaRepository) {
        this.temaRepository = temaRepository;
    }

    public List<Tema> findAll() {
        return temaRepository.findAll();
    }

    public Optional<Tema> findById(Long id) {
        return temaRepository.findById(id);
    }

    public Tema save(Tema tema) {
        return temaRepository.save(tema);
    }
}
