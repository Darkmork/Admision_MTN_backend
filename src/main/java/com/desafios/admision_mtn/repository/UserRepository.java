package com.desafios.admision_mtn.repository;

import com.desafios.admision_mtn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByRut(String rut);
    
    boolean existsByEmail(String email);
    
    boolean existsByRut(String rut);
    
    List<User> findByRoleAndActiveTrue(User.UserRole role);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByActiveTrue();
    
    // Métodos para estadísticas
    long countByActiveTrue();
    
    long countByActiveFalse();
    
    long countByEmailVerifiedTrue();
    
    long countByEmailVerifiedFalse();
    
    long countByRole(User.UserRole role);
    
    long countByRoleAndActiveTrue(User.UserRole role);
    
    // Método nativo para bypasear problemas de mapeo Hibernate
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmailNative(String email);
    
    // Debug method to check if user exists
    @Query(value = "SELECT COUNT(*) FROM users WHERE email = ?1", nativeQuery = true)
    Long countByEmailNative(String email);
}