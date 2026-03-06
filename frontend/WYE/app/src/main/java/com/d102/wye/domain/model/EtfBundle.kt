package com.d102.wye.domain.model

data class EtfBundle(
    val id: Int,
    val name: String,
    val summary: String,        // 짧은 요약
    val tags: List<String>
)

// 상세용 (다이얼로그)
data class EtfBundleDetail(
    val id: Int,
    val name: String,
    val description: String,    // 꾸러미 설명 (긴 텍스트)
    val etfItems: List<BundleEtfItem>  // 구성 종목들
)

data class BundleEtfItem(
    val name: String,
    val company: String,
)