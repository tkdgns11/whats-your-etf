"""뉴스 크롤링 키워드 정의 (산업분류 기반)"""

# =============================================
# 산업분류(group_code) 기반 키워드
# =============================================

# IT_SEMI - 반도체
SEMI_KEYWORDS = [
    "반도체",
    "HBM",
    "파운드리",
    "메모리반도체",
    "시스템반도체",
    "반도체장비",
    "반도체소재",
    "삼성전자 반도체",
    "SK하이닉스",
    "AI반도체",
    "GPU",
    "NPU",
    "DRAM",
    "NAND",
]

# IT_ELEC - 전자/IT
ELEC_KEYWORDS = [
    "전자부품",
    "디스플레이",
    "OLED",
    "LCD",
    "가전",
    "스마트폰",
    "삼성전자",
    "LG전자",
    "삼성디스플레이",
    "LG디스플레이",
    "PCB",
    "MLCC",
]

# IT_SW - 소프트웨어
SW_KEYWORDS = [
    "인공지능",
    "AI",
    "클라우드",
    "SaaS",
    "게임주",
    "플랫폼",
    "메타버스",
    "빅데이터",
    "사이버보안",
    "핀테크",
    "네이버",
    "카카오",
    "엔씨소프트",
    "크래프톤",
]

# BIO - 바이오/의약
BIO_KEYWORDS = [
    "바이오",
    "제약",
    "신약",
    "임상시험",
    "의료기기",
    "바이오시밀러",
    "셀트리온",
    "삼성바이오로직스",
    "SK바이오팜",
    "유한양행",
    "헬스케어",
    "비만치료제",
    "GLP-1",
]

# AUTO - 자동차
AUTO_KEYWORDS = [
    "자동차",
    "전기차",
    "자율주행",
    "현대차",
    "기아",
    "EV",
    "하이브리드",
    "수소차",
    "배터리",
    "2차전지",
    "LG에너지솔루션",
    "삼성SDI",
    "양극재",
    "음극재",
    "테슬라",
]

# CHEM - 화학/소재
CHEM_KEYWORDS = [
    "화학",
    "석유화학",
    "정밀화학",
    "LG화학",
    "롯데케미칼",
    "한화솔루션",
    "화장품",
    "K뷰티",
    "아모레퍼시픽",
    "LG생활건강",
    "코스맥스",
]

# STEEL - 철강/금속
STEEL_KEYWORDS = [
    "철강",
    "포스코",
    "현대제철",
    "고려아연",
    "비철금속",
    "희토류",
    "알루미늄",
    "구리",
    "니켈",
    "리튬",
]

# ENERGY - 에너지/유틸리티
ENERGY_KEYWORDS = [
    "에너지",
    "태양광",
    "풍력",
    "원전",
    "원자력",
    "SMR",
    "수소에너지",
    "신재생에너지",
    "한국전력",
    "한전",
    "두산에너빌리티",
    "ESS",
    "전력설비",
    "송배전",
]

# FINANCE - 금융
FINANCE_KEYWORDS = [
    "은행",
    "금융",
    "KB금융",
    "신한금융",
    "하나금융",
    "우리금융",
    "증권",
    "카드",
    "삼성카드",
    "금융지주",
    "저축은행",
]

# INSURANCE - 보험
INSURANCE_KEYWORDS = [
    "보험",
    "생명보험",
    "손해보험",
    "삼성생명",
    "삼성화재",
    "현대해상",
    "DB손해보험",
    "한화생명",
]

# CONSTRUCT - 건설/부동산
CONSTRUCT_KEYWORDS = [
    "건설",
    "부동산",
    "아파트",
    "리츠",
    "REITs",
    "삼성물산",
    "현대건설",
    "GS건설",
    "대우건설",
    "플랜트",
    "SOC",
    "인프라",
]

# RETAIL - 유통/소매
RETAIL_KEYWORDS = [
    "유통",
    "백화점",
    "이커머스",
    "쿠팡",
    "신세계",
    "롯데쇼핑",
    "이마트",
    "편의점",
    "CU",
    "GS25",
    "면세점",
]

# FOOD - 식품/음료
FOOD_KEYWORDS = [
    "식품",
    "음료",
    "CJ제일제당",
    "농심",
    "오뚜기",
    "하이트진로",
    "오리온",
    "풀무원",
    "삼양식품",
    "HMR",
]

# TELECOM - 통신/미디어
TELECOM_KEYWORDS = [
    "통신",
    "5G",
    "6G",
    "KT",
    "SK텔레콤",
    "LG유플러스",
    "엔터테인먼트",
    "KPOP",
    "하이브",
    "SM엔터",
    "JYP",
    "OTT",
    "넷플릭스",
    "웹툰",
    "카카오엔터",
]

# CONSUMER - 소비재
CONSUMER_KEYWORDS = [
    "소비재",
    "패션",
    "의류",
    "여행",
    "항공",
    "호텔",
    "레저",
    "카지노",
    "면세",
    "대한항공",
    "아시아나",
    "하나투어",
]

# TRANSPORT - 운송
TRANSPORT_KEYWORDS = [
    "운송",
    "해운",
    "물류",
    "택배",
    "HMM",
    "팬오션",
    "CJ대한통운",
    "한진",
    "항공화물",
]

# MACHINERY - 기계/산업재
MACHINERY_KEYWORDS = [
    "기계",
    "산업재",
    "두산",
    "LS",
    "공작기계",
    "로봇",
    "자동화",
    "휴머노이드",
    "협동로봇",
]

# SHIPBUILD - 조선
SHIPBUILD_KEYWORDS = [
    "조선",
    "한화오션",
    "HD현대중공업",
    "삼성중공업",
    "LNG선",
    "컨테이너선",
    "해양플랜트",
    "선박",
]

# DEFENSE - 방산/우주항공
DEFENSE_KEYWORDS = [
    "방산",
    "방위산업",
    "한화에어로스페이스",
    "LIG넥스원",
    "한국항공우주",
    "KAI",
    "미사일",
    "무기",
    "우주항공",
    "위성",
    "누리호",
]

# AGRI - 농업/어업
AGRI_KEYWORDS = [
    "농업",
    "어업",
    "농산물",
    "축산",
    "수산",
    "곡물",
    "비료",
]

# MINING - 광업/원자재
MINING_KEYWORDS = [
    "광업",
    "원자재",
    "금",
    "금값",
    "은",
    "원유",
    "WTI",
    "브렌트유",
    "천연가스",
    "우라늄",
]

# =============================================
# 시장/경제 공통 키워드
# =============================================

# ETF 직접 관련
ETF_KEYWORDS = [
    "ETF",
    "상장지수펀드",
    "ETF 상장",
    "ETF 상장폐지",
    "인덱스펀드",
    "패시브펀드",
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
    "한국은행",
    "금통위",
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
]

# 투자/증권
INVESTMENT_KEYWORDS = [
    "실적발표",
    "어닝",
    "배당",
    "자사주",
    "유상증자",
    "공매도",
    "외국인 매수",
    "외국인 매도",
    "IPO",
]


# =============================================
# 키워드 조회 함수
# =============================================

# 산업분류별 키워드 매핑
INDUSTRY_KEYWORDS = {
    "IT_SEMI": SEMI_KEYWORDS,
    "IT_ELEC": ELEC_KEYWORDS,
    "IT_SW": SW_KEYWORDS,
    "BIO": BIO_KEYWORDS,
    "AUTO": AUTO_KEYWORDS,
    "CHEM": CHEM_KEYWORDS,
    "STEEL": STEEL_KEYWORDS,
    "ENERGY": ENERGY_KEYWORDS,
    "FINANCE": FINANCE_KEYWORDS,
    "INSURANCE": INSURANCE_KEYWORDS,
    "CONSTRUCT": CONSTRUCT_KEYWORDS,
    "RETAIL": RETAIL_KEYWORDS,
    "FOOD": FOOD_KEYWORDS,
    "TELECOM": TELECOM_KEYWORDS,
    "CONSUMER": CONSUMER_KEYWORDS,
    "TRANSPORT": TRANSPORT_KEYWORDS,
    "MACHINERY": MACHINERY_KEYWORDS,
    "SHIPBUILD": SHIPBUILD_KEYWORDS,
    "DEFENSE": DEFENSE_KEYWORDS,
    "AGRI": AGRI_KEYWORDS,
    "MINING": MINING_KEYWORDS,
}


def get_all_keywords() -> list:
    """모든 키워드 반환 (중복 제거)"""
    all_keywords = set()

    # 산업분류 키워드
    for keywords in INDUSTRY_KEYWORDS.values():
        all_keywords.update(keywords)

    # 공통 키워드
    all_keywords.update(ETF_KEYWORDS)
    all_keywords.update(MARKET_KEYWORDS)
    all_keywords.update(INTEREST_RATE_KEYWORDS)
    all_keywords.update(FOREX_KEYWORDS)
    all_keywords.update(ECONOMY_KEYWORDS)
    all_keywords.update(GLOBAL_KEYWORDS)
    all_keywords.update(INVESTMENT_KEYWORDS)

    return list(all_keywords)


def get_priority_keywords() -> list:
    """우선순위 높은 키워드 (10분 배치용)"""
    priority = set()

    # ETF 관련
    priority.update(ETF_KEYWORDS[:3])

    # 시장 지수
    priority.update(MARKET_KEYWORDS[:6])

    # 금리
    priority.update(INTEREST_RATE_KEYWORDS[:5])

    # 주요 산업 (ETF 비중 높은)
    priority.update(SEMI_KEYWORDS[:5])      # 반도체
    priority.update(AUTO_KEYWORDS[:5])       # 자동차/2차전지
    priority.update(BIO_KEYWORDS[:4])        # 바이오
    priority.update(ENERGY_KEYWORDS[:4])     # 에너지
    priority.update(FINANCE_KEYWORDS[:4])    # 금융
    priority.update(SHIPBUILD_KEYWORDS[:3])  # 조선
    priority.update(DEFENSE_KEYWORDS[:3])    # 방산

    return list(priority)


def get_keywords_by_industry(group_code: str) -> list:
    """특정 산업의 키워드 반환"""
    return INDUSTRY_KEYWORDS.get(group_code, [])


def get_keywords_by_category() -> dict:
    """카테고리별 키워드 반환"""
    return {
        # 산업분류
        **INDUSTRY_KEYWORDS,
        # 공통
        "ETF": ETF_KEYWORDS,
        "시장": MARKET_KEYWORDS,
        "금리": INTEREST_RATE_KEYWORDS,
        "환율": FOREX_KEYWORDS,
        "경제": ECONOMY_KEYWORDS,
        "글로벌": GLOBAL_KEYWORDS,
        "투자": INVESTMENT_KEYWORDS,
    }


def get_industry_group_codes() -> list:
    """산업분류 group_code 목록 반환"""
    return list(INDUSTRY_KEYWORDS.keys())


# 총 키워드 수
TOTAL_KEYWORD_COUNT = len(get_all_keywords())


# =============================================
# 뉴스 카테고리 정의 (14개)
# =============================================

# 카테고리 코드 → 이름 매핑
NEWS_CATEGORIES = {
    "NEWS_SEMI": "반도체",
    "NEWS_IT": "IT/전자",
    "NEWS_BIO": "바이오/의약",
    "NEWS_AUTO": "자동차",
    "NEWS_CHEM": "화학/소재",
    "NEWS_ENERGY": "에너지",
    "NEWS_FINANCE": "금융",
    "NEWS_CONSTRUCT": "건설/부동산",
    "NEWS_CONSUMER": "소비재",
    "NEWS_TELECOM": "통신/미디어",
    "NEWS_TRANSPORT": "운송/물류",
    "NEWS_INDUSTRY": "산업재",
    "NEWS_ETC": "기타",
    "NEWS_MARKET": "시장/경제",
}

# 카테고리 → 키워드 매핑 (크롤링 키워드 → 카테고리)
CATEGORY_KEYWORD_MAP = {
    # 투자테마 13개
    "NEWS_SEMI": SEMI_KEYWORDS,
    "NEWS_IT": ELEC_KEYWORDS + SW_KEYWORDS,
    "NEWS_BIO": BIO_KEYWORDS,
    "NEWS_AUTO": AUTO_KEYWORDS,
    "NEWS_CHEM": CHEM_KEYWORDS + STEEL_KEYWORDS,
    "NEWS_ENERGY": ENERGY_KEYWORDS,
    "NEWS_FINANCE": FINANCE_KEYWORDS + INSURANCE_KEYWORDS,
    "NEWS_CONSTRUCT": CONSTRUCT_KEYWORDS,
    "NEWS_CONSUMER": CONSUMER_KEYWORDS + RETAIL_KEYWORDS + FOOD_KEYWORDS,
    "NEWS_TELECOM": TELECOM_KEYWORDS,
    "NEWS_TRANSPORT": TRANSPORT_KEYWORDS + SHIPBUILD_KEYWORDS,
    "NEWS_INDUSTRY": MACHINERY_KEYWORDS + DEFENSE_KEYWORDS,
    "NEWS_ETC": AGRI_KEYWORDS + MINING_KEYWORDS,
    # 시장/경제
    "NEWS_MARKET": (
        ETF_KEYWORDS + MARKET_KEYWORDS + INTEREST_RATE_KEYWORDS +
        FOREX_KEYWORDS + ECONOMY_KEYWORDS + GLOBAL_KEYWORDS + INVESTMENT_KEYWORDS
    ),
}

# 키워드 → 카테고리 역매핑 (빠른 조회용)
KEYWORD_TO_CATEGORY = {}
for category, keywords in CATEGORY_KEYWORD_MAP.items():
    for keyword in keywords:
        # 첫 번째 매칭 카테고리 사용 (우선순위: 투자테마 > 시장/경제)
        if keyword not in KEYWORD_TO_CATEGORY:
            KEYWORD_TO_CATEGORY[keyword] = category


def get_category_by_keyword(keyword: str) -> str:
    """
    키워드로 뉴스 카테고리 조회

    1. 정확히 일치하는 키워드 먼저 찾기
    2. 없으면 검색어에 포함된 키워드로 매칭 (투자테마 우선)
    """
    # 1. 정확히 일치
    if keyword in KEYWORD_TO_CATEGORY:
        return KEYWORD_TO_CATEGORY[keyword]

    # 2. 검색어에 포함된 키워드 찾기
    keyword_lower = keyword.lower()
    matched_categories = []

    for kw, cat in KEYWORD_TO_CATEGORY.items():
        if kw.lower() in keyword_lower:
            matched_categories.append((kw, cat))

    if not matched_categories:
        return "NEWS_ETC"

    # 투자테마(NEWS_MARKET 제외) 우선, 그 다음 가장 긴 키워드 매칭
    for kw, cat in sorted(matched_categories, key=lambda x: (-len(x[0]), x[1] == "NEWS_MARKET")):
        if cat != "NEWS_MARKET":
            return cat

    # 투자테마 없으면 NEWS_MARKET 반환
    return matched_categories[0][1]


def get_category_by_keywords(keywords: list) -> str:
    """
    여러 키워드로 뉴스 카테고리 조회 (가장 많이 매칭된 카테고리)

    Args:
        keywords: 뉴스에서 추출한 키워드 목록

    Returns:
        가장 적합한 카테고리 코드
    """
    if not keywords:
        return "NEWS_ETC"

    category_counts = {}
    for keyword in keywords:
        category = KEYWORD_TO_CATEGORY.get(keyword)
        if category:
            category_counts[category] = category_counts.get(category, 0) + 1

    if not category_counts:
        return "NEWS_ETC"

    # 가장 많이 매칭된 카테고리 반환
    return max(category_counts, key=category_counts.get)


def get_category_by_text(title: str, content: str = "") -> str:
    """
    뉴스 제목/본문에서 카테고리 추출

    Args:
        title: 뉴스 제목
        content: 뉴스 본문 (선택)

    Returns:
        카테고리 코드
    """
    text = f"{title} {content}".lower()

    category_scores = {}
    for category, keywords in CATEGORY_KEYWORD_MAP.items():
        score = 0
        for keyword in keywords:
            if keyword.lower() in text:
                # 제목에 있으면 가중치 2배
                if keyword.lower() in title.lower():
                    score += 2
                else:
                    score += 1
        if score > 0:
            category_scores[category] = score

    if not category_scores:
        return "NEWS_ETC"

    return max(category_scores, key=category_scores.get)


def get_all_news_categories() -> dict:
    """모든 뉴스 카테고리 목록 반환"""
    return NEWS_CATEGORIES.copy()
