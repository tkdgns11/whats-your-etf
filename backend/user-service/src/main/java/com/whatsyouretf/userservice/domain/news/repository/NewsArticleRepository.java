package com.whatsyouretf.userservice.domain.news.repository;

import com.whatsyouretf.userservice.domain.news.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 뉴스 기사 Repository
 */
@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    /**
     * 최신 뉴스 목록 조회 (활성 상태만)
     */
    Page<NewsArticle> findByIsActiveTrueOrderByPublishedAtDesc(Pageable pageable);

    /**
     * 카테고리 코드별 최신 뉴스 목록 조회
     */
    Page<NewsArticle> findByCategory_CodeAndIsActiveTrueOrderByPublishedAtDesc(
            String categoryCode, Pageable pageable);

    /**
     * 키워드 검색 (제목 + 본문)
     */
    @Query("SELECT n FROM NewsArticle n WHERE n.isActive = true " +
           "AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "ORDER BY n.publishedAt DESC")
    Page<NewsArticle> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * ETF 관련 뉴스 조회 (ETF 구성종목의 뉴스)
     * - etf_stock_composition JOIN stock JOIN news_stock_mapping
     */
    @Query("SELECT DISTINCT n FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND n.isActive = true " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByEtfId(@Param("etfId") Long etfId, Pageable pageable);

    /**
     * 회사 ID로 종목 관련 뉴스 조회
     */
    @Query("SELECT n FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * 회사 ID로 종목 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * ETF 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND n.isActive = true")
    long countByEtfId(@Param("etfId") Long etfId);

    /**
     * 원본 URL 존재 여부 확인
     */
    boolean existsBySourceUrl(String sourceUrl);
}
