package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.model.Problema;
import com.desafios.admision_mtn.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemaRepository extends JpaRepository<Problema, Long> {
    List<Problema> findByTema(Tema tema);
}
