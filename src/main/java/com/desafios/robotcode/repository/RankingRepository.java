package com.desafios.robotcode.repository;

import com.desafios.robotcode.model.Ranking;
import com.desafios.robotcode.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByUsuario(Usuario usuario);
    List<Ranking> findAllByOrderByPuntajeDesc();
}
