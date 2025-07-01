package com.desafios.robotcode.repository;

import com.desafios.robotcode.model.Problema;
import com.desafios.robotcode.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemaRepository extends JpaRepository<Problema, Long> {
    List<Problema> findByTema(Tema tema);
}
