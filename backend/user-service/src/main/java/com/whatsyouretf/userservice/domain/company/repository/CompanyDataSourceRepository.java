package com.whatsyouretf.userservice.domain.company.repository;

import com.whatsyouretf.userservice.domain.company.entity.CompanyDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 회사 데이터 소스 매핑 Repository
 */
@Repository
public interface CompanyDataSourceRepository extends JpaRepository<CompanyDataSource, Long> {

    /**
     * 회사 ID로 데이터 소스 매핑 조회
     */
    List<CompanyDataSource> findByCompanyInfoId(Long companyInfoId);
}
