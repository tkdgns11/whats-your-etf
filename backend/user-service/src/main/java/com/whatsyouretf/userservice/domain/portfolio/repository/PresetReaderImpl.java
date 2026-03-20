package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.service.PresetReader;
import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PresetReaderImpl implements PresetReader {
    private final PresetRepository presetRepository;

    @Override
    public List<PresetSummary> getPresetList() {
        return presetRepository.findAll();
    }
}
