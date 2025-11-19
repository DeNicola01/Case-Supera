package br.com.supera.case_supera.repository;

import br.com.supera.case_supera.entity.AccessRequest;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.RequestStatus;
import br.com.supera.case_supera.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    Optional<AccessRequest> findByProtocol(String protocol);
    
    Page<AccessRequest> findByUser(User user, Pageable pageable);
    
    @Query("SELECT ar FROM AccessRequest ar WHERE ar.user = :user " +
           "AND (:searchText IS NULL OR ar.protocol LIKE %:searchText% OR EXISTS " +
           "(SELECT m FROM ar.requestedModules m WHERE m.name LIKE %:searchText%)) " +
           "AND (:status IS NULL OR ar.status = :status) " +
           "AND (:urgent IS NULL OR ar.urgent = :urgent) " +
           "AND (:startDate IS NULL OR ar.requestDate >= :startDate) " +
           "AND (:endDate IS NULL OR ar.requestDate <= :endDate) " +
           "ORDER BY ar.requestDate DESC")
    Page<AccessRequest> findByUserWithFilters(
        @Param("user") User user,
        @Param("searchText") String searchText,
        @Param("status") RequestStatus status,
        @Param("urgent") Boolean urgent,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT ar FROM AccessRequest ar WHERE ar.user = :user " +
           "AND ar.status = 'ATIVO' " +
           "AND EXISTS (SELECT m FROM ar.requestedModules m WHERE m = :module)")
    List<AccessRequest> findActiveRequestsByUserAndModule(@Param("user") User user, @Param("module") Module module);
    
    @Query("SELECT ar FROM AccessRequest ar WHERE ar.user = :user " +
           "AND ar.status = 'ATIVO' " +
           "AND ar.expirationDate IS NOT NULL " +
           "AND ar.expirationDate <= :thresholdDate")
    List<AccessRequest> findExpiringRequests(@Param("user") User user, @Param("thresholdDate") LocalDateTime thresholdDate);
}

