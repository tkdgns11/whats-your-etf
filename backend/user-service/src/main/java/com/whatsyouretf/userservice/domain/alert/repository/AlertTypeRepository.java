package com.whatsyouretf.userservice.domain.alert.repository;

import com.whatsyouretf.userservice.domain.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 알림 유형 Repository
 */
@Repository
public interface AlertTypeRepository extends JpaRepository<AlertType, String> {

    /**
     * 활성 알림 유형 목록 조회 (정렬순)
     */
    List<AlertType> findByIsActiveTrueOrderByDisplayOrderAsc();
}
