package br.com.supera.case_supera.repository;

import br.com.supera.case_supera.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByName(String name);
    List<Module> findByActiveTrue();
    boolean existsByName(String name);
}

