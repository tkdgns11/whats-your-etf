package com.d102.wye.data.repository

import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.RelatedStock
import com.d102.wye.domain.model.Stock
import com.d102.wye.domain.model.StockEtf
import com.d102.wye.domain.repository.StockRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor() : StockRepository {

    override suspend fun getStock(ticker: String): BaseResult<Stock> {
        return BaseResult.Success(mockStock(ticker))
    }
}

private fun mockStock(ticker: String) = when (ticker) {
    "000660" -> Stock(
        ticker = "000660",
        name = "SK하이닉스",
        englishName = "SK Hynix Inc.",
        tags = listOf("KOSPI", "IT", "반도체"),
        currentPrice = 162_100,
        changeAmount = -1_900,
        changeRate = -1.2,
        marketCap = 117_000_000_000_000L,
        description = "SK하이닉스는 D램과 낸드플래시를 주력으로 하는 반도체 기업입니다. HBM(고대역폭 메모리) 시장에서 선도적인 위치를 차지하며 AI 반도체 수요 확대의 수혜를 받고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",        "삼성자산운용",      5.8,  35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("266370", "TIGER 반도체",      "미래에셋자산운용",  18.4, 28_760L, 2.87,   680_000_000_000L),
            StockEtf("091160", "KODEX 반도체",      "삼성자산운용",     20.1,  28_650L, 3.12,   412_500_000_000L),
            StockEtf("455880", "SOL 반도체소부장",  "신한자산운용",     12.3,  15_340L, 2.10,   220_000_000_000L),
            StockEtf("411420", "ACE 테크TOP10",     "한국투자신탁운용",  9.5,  12_180L, 1.95,   185_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("005930", "삼성전자",  "동종 업계 (반도체)",      74_200, 2.8),
            RelatedStock("042700", "한미반도체", "서플라이 체인 (장비)",    98_300, 1.4),
            RelatedStock("ASML",   "ASML",      "서플라이 체인 (노광기)", 860_000, -0.6),
        ),
    )
    "005930" -> Stock(
        ticker = "005930",
        name = "삼성전자",
        englishName = "Samsung Electronics Co., Ltd.",
        tags = listOf("KOSPI", "IT", "반도체"),
        currentPrice = 72_400,
        changeAmount = 1_200,
        changeRate = 2.8,
        marketCap = 432_000_000_000_000L,
        description = "삼성전자는 글로벌 반도체 및 전자 기기 시장을 선도하는 대한민국 대표 기업입니다. 메모리 반도체 세계 1위 점유율을 바탕으로 스마트폰, 가전 분야에서 혁신을 주도하고 있으며 최근 AI 반도체 및 파운드리 역량 강화에 집중하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",    "삼성자산운용",    24.5, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("266370", "TIGER 반도체",  "미래에셋자산운용", 15.2, 28_760L, 2.87,   680_000_000_000L),
            StockEtf("091160", "KODEX 반도체",  "삼성자산운용",   12.4,  28_650L, 3.12,   412_500_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("000660", "SK하이닉스", "동종 업계 (반도체)",     162_100, -1.2),
            RelatedStock("042700", "한미반도체", "서플라이 체인 (장비)",    98_300,  1.4),
        ),
    )
    "035420" -> Stock(
        ticker = "035420",
        name = "NAVER",
        englishName = "NAVER Corporation",
        tags = listOf("KOSPI", "IT", "플랫폼"),
        currentPrice = 198_500,
        changeAmount = 1_000,
        changeRate = 0.5,
        marketCap = 32_500_000_000_000L,
        description = "NAVER는 국내 1위 검색 포털을 운영하며 클라우드·커머스·핀테크·콘텐츠 등 다양한 사업을 영위합니다. 라인(LINE)을 통해 일본·동남아 시장에도 강한 입지를 보유하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",       "삼성자산운용",     3.2, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("411420", "ACE 테크TOP10",    "한국투자신탁운용", 8.5, 12_180L, 1.95,   185_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("035720", "카카오",   "동종 업계 (플랫폼)",  35_450, -1.1),
            RelatedStock("259960", "크래프톤", "동종 업계 (게임)",   210_000,  0.8),
        ),
    )
    "035720" -> Stock(
        ticker = "035720",
        name = "카카오",
        englishName = "Kakao Corp.",
        tags = listOf("KOSPI", "IT", "플랫폼"),
        currentPrice = 35_450,
        changeAmount = -400,
        changeRate = -1.1,
        marketCap = 15_800_000_000_000L,
        description = "카카오는 카카오톡을 기반으로 모빌리티·페이·뱅크·엔터테인먼트 등 다양한 플랫폼 사업을 운영합니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",    "삼성자산운용",    2.1, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("411420", "ACE 테크TOP10", "한국투자신탁운용", 6.8, 12_180L, 1.95,   185_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("035420", "NAVER",  "동종 업계 (플랫폼)", 198_500,  0.5),
            RelatedStock("122870", "와이지엔터", "파트너 (엔터)",  42_350, -0.7),
        ),
    )
    "005380" -> Stock(
        ticker = "005380",
        name = "현대차",
        englishName = "Hyundai Motor Company",
        tags = listOf("KOSPI", "자동차"),
        currentPrice = 215_000,
        changeAmount = -700,
        changeRate = -0.3,
        marketCap = 45_900_000_000_000L,
        description = "현대자동차는 승용·상용차 제조 및 판매, 금융·서비스 사업을 영위합니다. 전기차 아이오닉 시리즈로 글로벌 전동화 전환을 선도하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",   "삼성자산운용",    2.9, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("091180", "KODEX 자동차", "삼성자산운용",   18.2, 14_300L, 0.85,   98_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("000270", "기아",     "동종 업계 (자동차)",  98_300,  0.4),
            RelatedStock("012330", "현대모비스", "서플라이 체인",     250_500, -0.2),
        ),
    )
    "000270" -> Stock(
        ticker = "000270",
        name = "기아",
        englishName = "Kia Corporation",
        tags = listOf("KOSPI", "자동차"),
        currentPrice = 98_300,
        changeAmount = 400,
        changeRate = 0.4,
        marketCap = 39_300_000_000_000L,
        description = "기아는 현대차그룹 산하의 완성차 제조사로, EV6·EV9 등 전기차 라인업을 확대하며 글로벌 전동화 전략을 추진하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",   "삼성자산운용",    2.4, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("091180", "KODEX 자동차", "삼성자산운용",   15.5, 14_300L, 0.85,   98_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("005380", "현대차",    "동종 업계 (자동차)", 215_000, -0.3),
            RelatedStock("012330", "현대모비스", "서플라이 체인",    250_500, -0.2),
        ),
    )
    "105560" -> Stock(
        ticker = "105560",
        name = "KB금융",
        englishName = "KB Financial Group Inc.",
        tags = listOf("KOSPI", "금융", "은행"),
        currentPrice = 82_400,
        changeAmount = 600,
        changeRate = 0.7,
        marketCap = 33_800_000_000_000L,
        description = "KB금융그룹은 국민은행·KB증권·KB손해보험 등을 계열사로 두는 국내 최대 금융그룹입니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",  "삼성자산운용",    2.2, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("140710", "KODEX 은행",  "삼성자산운용",   22.5, 7_800L,  1.15,  142_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("055550", "신한지주",   "동종 업계 (금융)",  48_750,  0.3),
            RelatedStock("086790", "하나금융지주", "동종 업계 (금융)", 65_200, -0.5),
        ),
    )
    "055550" -> Stock(
        ticker = "055550",
        name = "신한지주",
        englishName = "Shinhan Financial Group Co., Ltd.",
        tags = listOf("KOSPI", "금융", "은행"),
        currentPrice = 48_750,
        changeAmount = 150,
        changeRate = 0.3,
        marketCap = 23_200_000_000_000L,
        description = "신한금융그룹은 신한은행·신한카드·신한투자증권 등을 보유한 종합 금융그룹입니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200", "삼성자산운용",    1.8, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("140710", "KODEX 은행", "삼성자산운용",   18.4,  7_800L, 1.15,  142_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("105560", "KB금융",     "동종 업계 (금융)",  82_400,  0.7),
            RelatedStock("086790", "하나금융지주", "동종 업계 (금융)", 65_200, -0.5),
        ),
    )
    "373220" -> Stock(
        ticker = "373220",
        name = "LG에너지솔루션",
        englishName = "LG Energy Solution, Ltd.",
        tags = listOf("KOSPI", "2차전지", "에너지"),
        currentPrice = 342_000,
        changeAmount = -3_000,
        changeRate = -0.9,
        marketCap = 80_200_000_000_000L,
        description = "LG에너지솔루션은 전기차·ESS용 배터리를 제조하는 글로벌 2위 배터리 업체입니다. GM·현대차·폭스바겐 등 주요 완성차 업체와 장기 공급 계약을 체결하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",    "삼성자산운용",    3.8, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("305720", "KODEX 2차전지", "삼성자산운용",   15.2, 12_650L, 1.45,   380_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("051910", "LG화학",   "모회사 (배터리)",  320_000, -0.5),
            RelatedStock("000660", "SK하이닉스", "동종 업계",      162_100, -1.2),
        ),
    )
    "207940" -> Stock(
        ticker = "207940",
        name = "삼성바이오로직스",
        englishName = "Samsung Biologics Co., Ltd.",
        tags = listOf("KOSPI", "바이오", "헬스케어"),
        currentPrice = 890_000,
        changeAmount = 5_000,
        changeRate = 0.6,
        marketCap = 63_400_000_000_000L,
        description = "삼성바이오로직스는 바이오의약품 위탁개발생산(CDMO) 분야 글로벌 선두 기업입니다. 인천 송도에 세계 최대 규모의 바이오 공장을 보유하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",     "삼성자산운용",    2.1, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("227550", "TIGER 헬스케어", "미래에셋자산운용", 12.4,  9_840L, 0.95,  115_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("068270", "셀트리온", "동종 업계 (바이오)", 158_000,  1.1),
            RelatedStock("128940", "한미약품",  "동종 업계 (제약)",  310_000, -0.3),
        ),
    )
    "012330" -> Stock(
        ticker = "012330",
        name = "현대모비스",
        englishName = "Hyundai Mobis Co., Ltd.",
        tags = listOf("KOSPI", "자동차부품"),
        currentPrice = 250_500,
        changeAmount = -500,
        changeRate = -0.2,
        marketCap = 23_700_000_000_000L,
        description = "현대모비스는 현대차·기아의 핵심 부품 계열사로, 전동화 부품(전기차 구동 시스템·배터리 시스템) 및 자율주행 모듈 사업을 확대하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",   "삼성자산운용",    1.6, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("091180", "KODEX 자동차", "삼성자산운용",   12.8, 14_300L, 0.85,  98_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("005380", "현대차", "고객사 (완성차)", 215_000, -0.3),
            RelatedStock("000270", "기아",   "고객사 (완성차)",  98_300,  0.4),
        ),
    )
    "086790" -> Stock(
        ticker = "086790",
        name = "하나금융지주",
        englishName = "Hana Financial Group Inc.",
        tags = listOf("KOSPI", "금융", "은행"),
        currentPrice = 65_200,
        changeAmount = -300,
        changeRate = -0.5,
        marketCap = 19_500_000_000_000L,
        description = "하나금융그룹은 하나은행·하나증권·하나카드 등을 계열사로 보유한 종합 금융그룹입니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200", "삼성자산운용",    1.5, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("140710", "KODEX 은행", "삼성자산운용",   16.8,  7_800L, 1.15,  142_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("105560", "KB금융",   "동종 업계 (금융)", 82_400,  0.7),
            RelatedStock("055550", "신한지주",  "동종 업계 (금융)", 48_750,  0.3),
        ),
    )
    "051910" -> Stock(
        ticker = "051910",
        name = "LG화학",
        englishName = "LG Chem, Ltd.",
        tags = listOf("KOSPI", "화학", "소재"),
        currentPrice = 320_000,
        changeAmount = -1_500,
        changeRate = -0.5,
        marketCap = 22_600_000_000_000L,
        description = "LG화학은 석유화학·첨단소재·생명과학 사업을 영위하며, LG에너지솔루션의 모회사입니다. 배터리 소재·OLED 소재 분야에서 글로벌 경쟁력을 보유합니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",  "삼성자산운용",    1.4, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("117460", "KODEX 화학",  "삼성자산운용",   22.5,  5_230L, 0.55,  45_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("373220", "LG에너지솔루션", "자회사 (배터리)", 342_000, -0.9),
            RelatedStock("011170", "롯데케미칼",     "동종 업계 (화학)",  68_400, -1.3),
        ),
    )
    "011170" -> Stock(
        ticker = "011170",
        name = "롯데케미칼",
        englishName = "Lotte Chemical Corporation",
        tags = listOf("KOSPI", "화학"),
        currentPrice = 68_400,
        changeAmount = -900,
        changeRate = -1.3,
        marketCap = 2_350_000_000_000L,
        description = "롯데케미칼은 에틸렌·폴리에틸렌 등 기초 석유화학 제품과 첨단소재를 생산합니다. 글로벌 유가 및 나프타 가격 변동에 실적이 민감하게 반응합니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200", "삼성자산운용",    0.8, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("117460", "KODEX 화학", "삼성자산운용",   14.2,  5_230L, 0.55,  45_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("051910", "LG화학", "동종 업계 (화학)", 320_000, -0.5),
            RelatedStock("010950", "S-Oil",  "동종 업계 (정유)",  68_000,  0.2),
        ),
    )
    else -> Stock(
        ticker = ticker,
        name = "삼성전자",
        englishName = "Samsung Electronics Co., Ltd.",
        tags = listOf("KOSPI", "IT", "반도체"),
        currentPrice = 72_400,
        changeAmount = 1_200,
        changeRate = 2.8,
        marketCap = 456_456_456_555L,
        description = "삼성전자는 글로벌 반도체 및 전자 기기 시장을 선도하는 대한민국 대표 기업입니다. 메모리 반도체 세계 1위 점유율을 바탕으로 스마트폰, 가전 분야에서 혁신을 주도하고 있으며 최근 AI 반도체 및 파운드리 역량 강화에 집중하고 있습니다.",
        containedEtfs = listOf(
            StockEtf("069500", "KODEX 200",        "삼성자산운용",      24.5, 35_420L, 1.61, 2_451_200_000_000L),
            StockEtf("266370", "TIGER 반도체",      "미래에셋자산운용",  15.2, 28_760L, 2.87,   680_000_000_000L),
            StockEtf("091160", "KODEX 반도체",      "삼성자산운용",     12.4,  28_650L, 3.12,   412_500_000_000L),
            StockEtf("455880", "SOL 반도체소부장",  "신한자산운용",     10.8,  15_340L, 2.10,   220_000_000_000L),
            StockEtf("411420", "ACE 테크TOP10",     "한국투자신탁운용",  9.5,  12_180L, 1.95,   185_000_000_000L),
            StockEtf("293180", "HANARO 코스피",     "NH-Amundi",         8.2,  11_880L, 1.20,   750_000_000_000L),
        ),
        relatedStocks = listOf(
            RelatedStock("000660", "SK하이닉스",  "동종 업계 (반도체)",      162_100, -1.2),
            RelatedStock("042700", "한미반도체",  "서플라이 체인 (장비)",     98_300,  1.4),
            RelatedStock("ASML",   "ASML",        "서플라이 체인 (노광기)",  860_000, -0.6),
        ),
    )
}
