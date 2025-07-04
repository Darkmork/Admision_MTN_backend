package com.desafios.robotcode.repository;

import com.desafios.robotcode.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {
    Optional<Tema> findByNombre(String nombre);
}
