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
     * 최신 뉴스 목록 조회 (활성 상태 + AI 분석 완료)
     */
    Page<NewsArticle> findByIsActiveTrueAndContentSummaryIsNotNullOrderByPublishedAtDesc(Pageable pageable);

    /**
     * 카테고리 코드별 최신 뉴스 목록 조회
     */
    Page<NewsArticle> findByCategory_CodeAndIsActiveTrueAndContentSummaryIsNotNullOrderByPublishedAtDesc(
            String categoryCode, Pageable pageable);

    /**
     * 키워드 검색 (제목 + 본문)
     */
    @Query("SELECT n FROM NewsArticle n WHERE n.isActive = true AND n.contentSummary IS NOT NULL " +
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
           "WHERE ec.etf.id = :etfId AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByEtfId(@Param("etfId") Long etfId, Pageable pageable);

    /**
     * 회사 ID로 종목 관련 뉴스 조회
     */
    @Query("SELECT n FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true AND n.contentSummary IS NOT NULL " +
           "ORDER BY n.publishedAt DESC")
    List<NewsArticle> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * 회사 ID로 종목 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "WHERE nsm.companyInfo.id = :companyId AND n.isActive = true AND n.contentSummary IS NOT NULL")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * ETF 관련 뉴스 개수 조회
     */
    @Query("SELECT COUNT(DISTINCT n) FROM NewsArticle n " +
           "JOIN n.stockMappings nsm " +
           "JOIN EtfStockComposition ec ON ec.stock.company.id = nsm.companyInfo.id " +
           "WHERE ec.etf.id = :etfId AND n.isActive = true AND n.contentSummary IS NOT NULL")
    long countByEtfId(@Param("etfId") Long etfId);

    /**
     * 원본 URL 존재 여부 확인
     */
    boolean existsBySourceUrl(String sourceUrl);

    /**
     * 포트폴리오 관련 뉴스 조회 (포트폴리오 구성 ETF들의 종목 뉴스)
     * - portfolio_etf JOIN etf JOIN etf_stock_composition JOIN stock JOIN news_stock_mapping
     * <p>
     * 2단계 처리:
     * 1. 스코어로 상위 N개 선택: score = relevance × recency_multiplier
     *    - relevance = MAX(portfolio_etf.weight_pct × etf_stock.weight_pct)
     *    - recency_multiplier = 1 / (1 + 경과일수 × 0.3)
     * 2. 선택된 뉴스를 최신순(published_at DESC)으로 정렬하여 반환
     */
    @Query(value = """
            SELECT * FROM (
                SELECT n.* FROM news_article n
                JOIN news_stock_mapping nsm ON nsm.news_id = n.id
                JOIN stock s ON s.company_id = nsm.company_id
                JOIN etf_stock_composition ec ON ec.stock_id = s.id
                JOIN portfolio_etf pe ON pe.etf_id = ec.etf_id
                WHERE pe.portfolio_id = :portfolioId AND n.is_active = true AND n.content_summary IS NOT NULL
                GROUP BY n.id
                ORDER BY (
                    MAX(pe.weight_pct * ec.weight_pct) *
                    (1.0 / (1.0 + EXTRACT(EPOCH FROM (NOW() - n.published_at)) / 86400.0 * 0.3))
                ) DESC
                LIMIT :limit
            ) AS top_news
            ORDER BY published_at DESC
            """, nativeQuery = true)
    List<NewsArticle> findByPortfolioId(@Param("portfolioId") Long portfolioId, @Param("limit") int limit);
}
