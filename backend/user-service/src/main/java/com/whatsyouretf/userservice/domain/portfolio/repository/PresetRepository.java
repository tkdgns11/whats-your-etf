package com.whatsyouretf.userservice.domain.portfolio.repository;

import com.whatsyouretf.userservice.domain.portfolio.service.PresetSummary;

import java.util.List;

public interface PresetRepository {
    List<PresetSummary> findAll();
}
