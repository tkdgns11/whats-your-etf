package com.whatsyouretf.userservice.domain.etf.repository;

import com.whatsyouretf.userservice.domain.etf.entity.IndustryClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 산업 분류 Repository
 */
@Repository
public interface IndustryClassificationRepository extends JpaRepository<IndustryClassification, String> {

    /**
     * 코드로 산업 분류 조회
     */
    Optional<IndustryClassification> findByCode(String code);
}
