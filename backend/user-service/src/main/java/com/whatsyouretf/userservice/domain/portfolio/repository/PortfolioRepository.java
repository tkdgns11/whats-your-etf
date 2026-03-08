package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 포트폴리오 Repository
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 사용자의 포트폴리오 목록 조회 (최신순)
     */
    Page<Portfolio> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID와 포트폴리오 ID로 조회
     */
    Optional<Portfolio> findByIdAndUserId(Long id, Long userId);

    /**
     * 포트폴리오 이름 중복 확인
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * 사용자의 포트폴리오 개수 조회
     */
    long countByUserId(Long userId);
}
