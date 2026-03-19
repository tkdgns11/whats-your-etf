package com.whatsyouretf.userservice.domain.portfolio.repository.mock;

import com.whatsyouretf.userservice.domain.portfolio.repository.PresetRepository;
import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PresetMockRepositoryImpl implements PresetRepository {
    @Override
    public List<PresetSummary> findAll() {
        return List.of(
            new PresetSummary(1L, "안전제일 배당왕", "하락장에서도 든든하게 계좌를 지켜주는 고배당", "배당"),
            new PresetSummary(1L, "안전제일 배당왕", "하락장에서도 든든하게 계좌를 지켜주는 고배당", "저변동성"),
            new PresetSummary(2L, "로켓 주식", "화성 갈끄니까", "일론머스크"),
            new PresetSummary(2L, "로켓 주식", "화성 갈끄니까", "테슬라는신이야"));
    }
}
