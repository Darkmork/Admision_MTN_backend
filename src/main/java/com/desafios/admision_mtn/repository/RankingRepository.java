package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Ranking;
import com.desafios.admision_mtn.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByUsuario(Usuario usuario);
    List<Ranking> findAllByOrderByPuntajeDesc();
}
