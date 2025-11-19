package br.com.supera.case_supera.repository;

import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.entity.UserModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserModuleRepository extends JpaRepository<UserModule, Long> {
    List<UserModule> findByUserAndActiveTrue(User user);
    
    @Query("SELECT COUNT(um) FROM UserModule um WHERE um.user = :user AND um.active = true")
    long countActiveModulesByUser(@Param("user") User user);
    
    Optional<UserModule> findByUserAndModuleAndActiveTrue(User user, Module module);
    
    @Query("SELECT um.module FROM UserModule um WHERE um.user = :user AND um.active = true")
    List<Module> findActiveModulesByUser(@Param("user") User user);
}

