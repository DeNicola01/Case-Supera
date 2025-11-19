package br.com.supera.case_supera.repository;

import br.com.supera.case_supera.entity.AccessHistory;
import br.com.supera.case_supera.entity.AccessRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessHistoryRepository extends JpaRepository<AccessHistory, Long> {
    List<AccessHistory> findByAccessRequestOrderByChangeDateDesc(AccessRequest accessRequest);
}

