package com.whatsyouretf.userservice.domain.news.repository;

import com.whatsyouretf.userservice.domain.news.entity.NewsEtfInfluence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 뉴스-ETF 영향도 Repository
 */
@Repository
public interface NewsEtfInfluenceRepository extends JpaRepository<NewsEtfInfluence, Long> {

    /**
     * 뉴스 ID로 ETF 영향도 조회 (ETF 정보 포함)
     */
    @Query("SELECT nei FROM NewsEtfInfluence nei " +
           "JOIN FETCH nei.etf e " +
           "WHERE nei.newsArticle.id = :newsId " +
           "ORDER BY nei.influenceScore DESC")
    List<NewsEtfInfluence> findByNewsIdWithEtf(@Param("newsId") Long newsId);
}
