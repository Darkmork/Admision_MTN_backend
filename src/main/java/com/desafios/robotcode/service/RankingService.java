package com.desafios.robotcode.service;

import com.desafios.robotcode.model.Ranking;
import com.desafios.robotcode.model.Usuario;
import com.desafios.robotcode.repository.RankingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public RankingService(RankingRepository rankingRepository) {
        this.rankingRepository = rankingRepository;
    }

    public Optional<Ranking> findByUsuario(Usuario usuario) {
        return rankingRepository.findByUsuario(usuario);
    }

    public List<Ranking> findAllByOrderByPuntajeDesc() {
        return rankingRepository.findAllByOrderByPuntajeDesc();
    }

    public Ranking save(Ranking ranking) {
        return rankingRepository.save(ranking);
    }
}
