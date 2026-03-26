package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.EtfOtherComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtfOtherCompositionRepository extends JpaRepository<EtfOtherComposition, Long> {

    @Query("SELECT o FROM EtfOtherComposition o WHERE o.etf.id = :etfId ORDER BY o.weight DESC")
    List<EtfOtherComposition> findByEtfId(@Param("etfId") Long etfId);
}
