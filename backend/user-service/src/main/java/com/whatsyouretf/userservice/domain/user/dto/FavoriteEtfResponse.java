package com.whatsyouretf.userservice.domain.user.dto;

import com.whatsyouretf.userservice.domain.etf.entity.Etf;
import com.whatsyouretf.userservice.domain.etf.entity.EtfPrice;
import com.whatsyouretf.userservice.domain.user.entity.UserFavoriteEtf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 관심 ETF 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteEtfResponse {

    /** ETF 종목코드 */
    private String ticker;

    /** ETF 명칭 */
    private String name;

    /** 카테고리 */
    private String category;

    /** 자산운용사 */
    private String assetManager;

    /** 현재가 (최신 종가) */
    private BigDecimal currentPrice;

    /** 등락률 (%) */
    private BigDecimal changeRate;

    /** 관심 등록일 */
    private LocalDateTime favoritedAt;

    /**
     * Entity -> DTO 변환
     */
    public static FavoriteEtfResponse from(UserFavoriteEtf favorite) {
        Etf etf = favorite.getEtf();
        return FavoriteEtfResponse.builder()
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .assetManager(etf.getAssetManager())
                .favoritedAt(favorite.getCreatedAt())
                .build();
    }

    /**
     * Entity + Price -> DTO 변환
     */
    public static FavoriteEtfResponse from(UserFavoriteEtf favorite, EtfPrice latestPrice) {
        Etf etf = favorite.getEtf();
        return FavoriteEtfResponse.builder()
                .ticker(etf.getStockCode())
                .name(etf.getName())
                .assetManager(etf.getAssetManager())
                .currentPrice(latestPrice != null ? latestPrice.getClose() : null)
                .changeRate(latestPrice != null ? latestPrice.getChangeRate() : null)
                .favoritedAt(favorite.getCreatedAt())
                .build();
    }
}
