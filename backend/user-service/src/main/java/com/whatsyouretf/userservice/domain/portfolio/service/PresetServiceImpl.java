package com.whatsyouretf.userservice.domain.portfolio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetService {
    private final PresetReader presetReader;
    @Override
    @Transactional(readOnly = true)
    public List<PresetSummary> getPresets() {
        return presetReader.getPresetList();
    }
}
