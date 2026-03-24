package com.whatsyouretf.userservice.domain.etf.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whatsyouretf.userservice.domain.etf.dto.EtfSummary;
import com.whatsyouretf.userservice.domain.etf.entity.EtfSector;
import com.whatsyouretf.userservice.domain.etf.entity.QEtf;
import com.whatsyouretf.userservice.domain.etf.entity.RiskType;
import com.whatsyouretf.userservice.domain.etf.service.EtfQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EtfQueryDslReaderImpl implements EtfQueryDslReader {

    private static final QEtf etf = QEtf.etf;
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EtfSummary> readEtfList(EtfQuery query, Pageable pageable) {
        List<EtfSummary> content = queryFactory
                .select(Projections.constructor(EtfSummary.class,
                        etf.id,
                        etf.stockCode,
                        etf.name,
                        Expressions.constant(false),
                        etf.riskType.stringValue()
                ))
                .from(etf)
                .where(
                        etf.isActive.isTrue(),
                        riskTypeEq(query.ristType()),
                        strategyEq(query.strategy()),
                        sectorEq(query.sector()),
                        dividendYieldGoe(query.dividendYield()),
                        dividendFreqEq(query.dividendFrequency()),
                        isDerivativesEq(query.isDerivatives()),
                        isLeverageEq(query.isLeverage()),
                        isInverseEq(query.isInverse()),
                        perBetween(query.perLow(), query.perHigh()),
                        pbrBetween(query.pbrLow(), query.pbrHigh()),
                        roeBetween(query.roeLow(), query.roeHigh()),
                        commissionLoe(query.commission()),
                        aumGoe(query.aum())
                )
                .orderBy(orderBy(query.sortedBy()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(etf.count())
                .from(etf)
                .where(
                        etf.isActive.isTrue(),
                        riskTypeEq(query.ristType()),
                        strategyEq(query.strategy()),
                        sectorEq(query.sector()),
                        dividendYieldGoe(query.dividendYield()),
                        dividendFreqEq(query.dividendFrequency()),
                        isDerivativesEq(query.isDerivatives()),
                        isLeverageEq(query.isLeverage()),
                        isInverseEq(query.isInverse()),
                        perBetween(query.perLow(), query.perHigh()),
                        pbrBetween(query.pbrLow(), query.pbrHigh()),
                        roeBetween(query.roeLow(), query.roeHigh()),
                        commissionLoe(query.commission()),
                        aumGoe(query.aum())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression riskTypeEq(String riskType) {
        if (riskType == null) return null;
        try {
            return etf.riskType.eq(RiskType.valueOf(riskType));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BooleanExpression strategyEq(String strategy) {
        return strategy != null ? etf.strategyType.eq(strategy) : null;
    }

    private BooleanExpression sectorEq(String sector) {
        if (sector == null) return null;
        try {
            return etf.sector.eq(EtfSector.valueOf(sector));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private BooleanExpression dividendYieldGoe(BigDecimal dividendYield) {
        return dividendYield != null ? etf.dividendYield.goe(dividendYield) : null;
    }

    private BooleanExpression dividendFreqEq(String dividendFrequency) {
        return dividendFrequency != null ? etf.dividendFreq.eq(dividendFrequency) : null;
    }

    private BooleanExpression isDerivativesEq(Boolean isDerivatives) {
        return isDerivatives != null ? etf.isDerivatives.eq(isDerivatives) : null;
    }

    private BooleanExpression isLeverageEq(Boolean isLeverage) {
        return isLeverage != null ? etf.isLeveraged.eq(isLeverage) : null;
    }

    private BooleanExpression isInverseEq(Boolean isInverse) {
        return isInverse != null ? etf.isInverse.eq(isInverse) : null;
    }

    private BooleanExpression perBetween(BigDecimal perLow, BigDecimal perHigh) {
        if (perLow != null && perHigh != null) return etf.fundamental.per.between(perLow.doubleValue(), perHigh.doubleValue());
        if (perLow != null) return etf.fundamental.per.goe(perLow.doubleValue());
        if (perHigh != null) return etf.fundamental.per.loe(perHigh.doubleValue());
        return null;
    }

    private BooleanExpression pbrBetween(BigDecimal pbrLow, BigDecimal pbrHigh) {
        if (pbrLow != null && pbrHigh != null) return etf.fundamental.pbr.between(pbrLow.doubleValue(), pbrHigh.doubleValue());
        if (pbrLow != null) return etf.fundamental.pbr.goe(pbrLow.doubleValue());
        if (pbrHigh != null) return etf.fundamental.pbr.loe(pbrHigh.doubleValue());
        return null;
    }

    private BooleanExpression roeBetween(BigDecimal roeLow, BigDecimal roeHigh) {
        if (roeLow != null && roeHigh != null) return etf.fundamental.roe.between(roeLow.doubleValue(), roeHigh.doubleValue());
        if (roeLow != null) return etf.fundamental.roe.goe(roeLow.doubleValue());
        if (roeHigh != null) return etf.fundamental.roe.loe(roeHigh.doubleValue());
        return null;
    }

    private BooleanExpression commissionLoe(BigDecimal commission) {
        return commission != null ? etf.expenseRatio.loe(commission) : null;
    }

    private BooleanExpression aumGoe(BigDecimal aum) {
        return aum != null ? etf.aum.goe(aum.longValue()) : null;
    }

    private OrderSpecifier<?> orderBy(String sortedBy) {
        if (sortedBy == null) return etf.id.asc();
        return switch (sortedBy) {
            case "aum" -> etf.aum.desc();
            case "dividend_yield" -> etf.dividendYield.desc();
            case "per" -> etf.fundamental.per.asc();
            case "expense_ratio" -> etf.expenseRatio.asc();
            default -> etf.id.asc();
        };
    }
}
