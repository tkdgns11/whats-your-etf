"""뉴스 크롤링 키워드 정의"""

# ETF 직접 관련
ETF_KEYWORDS = [
    "ETF",
    "상장지수펀드",
    "ETF 상장",
    "ETF 상장폐지",
    "인덱스펀드",
]

# 시장/지수
MARKET_KEYWORDS = [
    "코스피",
    "코스닥",
    "KOSPI",
    "KOSDAQ",
    "나스닥",
    "다우지수",
    "S&P500",
    "니케이",
    "상해종합",
    "항셍지수",
]

# 금리/통화정책
INTEREST_RATE_KEYWORDS = [
    "기준금리",
    "금리 인상",
    "금리 인하",
    "금리 동결",
    "연준",
    "FOMC",
    "한국은행 금통위",
    "통화정책",
    "양적완화",
    "양적긴축",
]

# 환율/외환
FOREX_KEYWORDS = [
    "환율",
    "원달러",
    "달러",
    "엔화",
    "위안화",
    "유로",
    "외환시장",
]

# 원자재/상품
COMMODITY_KEYWORDS = [
    "국제유가",
    "원유",
    "WTI",
    "브렌트유",
    "금값",
    "금 시세",
    "은 시세",
    "구리",
    "리튬",
    "천연가스",
]

# 섹터별 키워드
SECTOR_KEYWORDS = [
    # 반도체
    "반도체",
    "HBM",
    "파운드리",
    "메모리",
    "시스템반도체",
    # 2차전지/전기차
    "2차전지",
    "배터리",
    "전기차",
    "리튬이온",
    "양극재",
    "음극재",
    # AI/IT
    "인공지능",
    "AI",
    "빅테크",
    "클라우드",
    "데이터센터",
    # 바이오
    "바이오",
    "제약",
    "신약",
    "임상시험",
    # 기타 섹터
    "조선",
    "방산",
    "원전",
    "신재생에너지",
    "태양광",
]

# 주요 기업 (ETF 구성 비중 높은)
COMPANY_KEYWORDS = [
    # 한국
    "삼성전자",
    "SK하이닉스",
    "현대차",
    "기아",
    "LG에너지솔루션",
    "삼성SDI",
    "POSCO홀딩스",
    "네이버",
    "카카오",
    "셀트리온",
    # 미국
    "애플",
    "엔비디아",
    "마이크로소프트",
    "테슬라",
    "구글",
    "아마존",
    "메타",
    "AMD",
    "인텔",
    "브로드컴",
]

# 경제 이슈/정책
ECONOMY_KEYWORDS = [
    "인플레이션",
    "디플레이션",
    "경기침체",
    "GDP",
    "실업률",
    "고용지표",
    "소비자물가",
    "생산자물가",
    "무역수지",
    "경상수지",
]

# 글로벌 이슈
GLOBAL_KEYWORDS = [
    "트럼프",
    "관세",
    "무역전쟁",
    "미중",
    "러시아",
    "우크라이나",
    "중동",
    "OPEC",
    "G20",
    "IMF",
]

# 투자/증권
INVESTMENT_KEYWORDS = [
    "실적발표",
    "어닝",
    "배당",
    "자사주",
    "유상증자",
    "무상증자",
    "공매도",
    "외국인 매수",
    "외국인 매도",
    "기관 매수",
    "IPO",
    "상장",
]


def get_all_keywords() -> list:
    """모든 키워드 반환"""
    all_keywords = []
    all_keywords.extend(ETF_KEYWORDS)
    all_keywords.extend(MARKET_KEYWORDS)
    all_keywords.extend(INTEREST_RATE_KEYWORDS)
    all_keywords.extend(FOREX_KEYWORDS)
    all_keywords.extend(COMMODITY_KEYWORDS)
    all_keywords.extend(SECTOR_KEYWORDS)
    all_keywords.extend(COMPANY_KEYWORDS)
    all_keywords.extend(ECONOMY_KEYWORDS)
    all_keywords.extend(GLOBAL_KEYWORDS)
    all_keywords.extend(INVESTMENT_KEYWORDS)
    return all_keywords


def get_priority_keywords() -> list:
    """우선순위 높은 키워드 (자주 수집)"""
    priority = []
    priority.extend(ETF_KEYWORDS[:3])
    priority.extend(MARKET_KEYWORDS[:6])
    priority.extend(INTEREST_RATE_KEYWORDS[:4])
    priority.extend(COMPANY_KEYWORDS[:10])
    return priority


def get_keywords_by_category() -> dict:
    """카테고리별 키워드 반환"""
    return {
        "ETF": ETF_KEYWORDS,
        "시장": MARKET_KEYWORDS,
        "금리": INTEREST_RATE_KEYWORDS,
        "환율": FOREX_KEYWORDS,
        "원자재": COMMODITY_KEYWORDS,
        "섹터": SECTOR_KEYWORDS,
        "기업": COMPANY_KEYWORDS,
        "경제": ECONOMY_KEYWORDS,
        "글로벌": GLOBAL_KEYWORDS,
        "투자": INVESTMENT_KEYWORDS,
    }


# 총 키워드 수
TOTAL_KEYWORD_COUNT = len(get_all_keywords())  # 약 100개
