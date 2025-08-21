package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    Optional<Tema> findByNombre(String nombre);
}
