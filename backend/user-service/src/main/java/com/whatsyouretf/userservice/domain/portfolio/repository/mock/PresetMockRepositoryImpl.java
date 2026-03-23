package com.whatsyouretf.userservice.domain.portfolio.repository.mock;

import com.whatsyouretf.userservice.domain.portfolio.controller.EtfPresetResponse;
import com.whatsyouretf.userservice.domain.portfolio.controller.PresetDetail;
import com.whatsyouretf.userservice.domain.portfolio.repository.PresetRepository;
import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PresetMockRepositoryImpl implements PresetRepository {
    public static final Map<Long, PresetDetail> map = Map.of(1L,
        new PresetDetail(
            1L,
            "안전제일 배당왕",
            "하락장에서도 든든하게 계좌를 지켜주는 고배당",
            "STABLE_INCOME"
            ,
            List.of(
                new EtfPresetResponse("069500", "KODEX 200"),
                new EtfPresetResponse("139290", "TIGER 200 경기소비재"),
                new EtfPresetResponse("266390", "KODEX 경기소비재")
            )),
        2L,
        new PresetDetail(
            2L,
            "로켓 주식",
            "화성 갈끄니까",
            "HIGH_GROWTH",
            List.of(
                new EtfPresetResponse("069500", "KODEX 200"),
                new EtfPresetResponse("139290", "TIGER 200 경기소비재"),
                new EtfPresetResponse("266390", "KODEX 경기소비재")
            )));

    @Override
    public List<PresetSummary> findAll() {
        return List.of(
            new PresetSummary(1L, "안전제일 배당왕", "하락장에서도 든든하게 계좌를 지켜주는 고배당", "배당", "STABLE_INCOME"),
            new PresetSummary(1L, "안전제일 배당왕", "하락장에서도 든든하게 계좌를 지켜주는 고배당", "저변동성", "STABLE_INCOME"),
            new PresetSummary(2L, "로켓 주식", "화성 갈끄니까", "일론머스크", "HIGH_GROWTH"),
            new PresetSummary(2L, "로켓 주식", "화성 갈끄니까", "테슬라는신이야", "HIGH_GROWTH"));
    }

    @Override
    public PresetDetail findByPresetId(Long presetId) {
        return map.get(presetId);
    }
}
