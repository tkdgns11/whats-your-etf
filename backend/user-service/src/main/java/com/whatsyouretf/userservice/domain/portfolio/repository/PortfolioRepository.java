package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.entity.Portfolio;
import com.whatsyouretf.userservice.domain.portfolio.entity.PortfolioEtf;
import com.whatsyouretf.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
        List<Portfolio> findByUserOrderByCreatedAtDesc(User user);


        @Query("""
            SELECT pe
            FROM PortfolioEtf pe
            JOIN FETCH pe.etf
            JOIN FETCH pe.portfolio
            WHERE pe.portfolio.id = :portfolioId
        """)
        List<PortfolioEtf> findByPortfolioId(@Param("portfolioId") Long portfolioId);}
