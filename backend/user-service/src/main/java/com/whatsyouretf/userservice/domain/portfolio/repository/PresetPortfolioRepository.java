package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.PresetPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 꾸러미 Repository
 */
@Repository
public interface PresetPortfolioRepository extends JpaRepository<PresetPortfolio, Long> {

    /**
     * 활성 꾸러미 목록 조회 (노출 순서대로)
     */
    List<PresetPortfolio> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 카테고리별 활성 꾸러미 조회
     */
    @Query("SELECT p FROM PresetPortfolio p WHERE p.category.code = :categoryCode AND p.isActive = true ORDER BY p.displayOrder ASC")
    List<PresetPortfolio> findByCategoryCodeAndActive(String categoryCode);
}
