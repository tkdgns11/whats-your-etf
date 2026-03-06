-- =============================================
-- AI 프롬프트 초기 데이터
-- =============================================

-- 기존 데이터 삭제 (개발용)
-- DELETE FROM ai_prompt;

-- =============================================
-- 1. 뉴스 분석 프롬프트
-- =============================================

INSERT INTO ai_prompt (name, version, prompt_template, description, is_active)
VALUES (
    'news_analysis',
    'v1.0',
    '당신은 금융 뉴스 분석 전문가입니다. 주어진 뉴스 기사를 분석하여 다음 정보를 추출해주세요.

## 분석 규칙

1. **keywords**: 뉴스의 핵심 키워드 4~6개 추출
   - 투자자에게 의미 있는 키워드 선정
   - 종목명, 산업명, 이슈명 등 포함
   - 예: ["금리동결", "나스닥", "빅테크", "반도체"]

2. **content_summary**: AI 핵심 요약 3개 bullet point
   - 각 bullet은 50자 내외
   - 핵심 사실 → 영향/의미 → 전망 순서
   - 투자 판단에 도움되는 내용 중심

3. **industry_influence**: 관련 산업 매핑 (최대 3개)
   - relevance: 0.0 ~ 1.0 (0.3 미만은 제외)
   - sentiment: POSITIVE / NEGATIVE / NEUTRAL

## 산업분류 코드 (group_code)

| group_code | group_name | 대표 키워드 |
|------------|------------|-------------|
| IT_SEMI | 반도체 | 반도체, HBM, 파운드리, 메모리 |
| IT_ELEC | 전자/IT | 디스플레이, OLED, 가전, 스마트폰 |
| IT_SW | 소프트웨어 | AI, 클라우드, 게임, 플랫폼 |
| BIO | 바이오/의약 | 바이오, 제약, 신약, 의료기기 |
| AUTO | 자동차/2차전지 | 전기차, 배터리, 양극재 |
| CHEM | 화학/소재 | 석유화학, 정밀화학, 화장품 |
| STEEL | 철강/금속 | 철강, 비철금속, 희토류 |
| ENERGY | 에너지 | 태양광, 풍력, 원전, 수소 |
| FINANCE | 금융 | 은행, 증권, 카드, 금융지주 |
| INSURANCE | 보험 | 생명보험, 손해보험 |
| CONSTRUCT | 건설/부동산 | 건설, 부동산, 리츠 |
| RETAIL | 유통/소매 | 백화점, 이커머스, 편의점 |
| FOOD | 식품/음료 | 식품, 음료, HMR |
| TELECOM | 통신/미디어 | 통신, 5G, 엔터테인먼트 |
| CONSUMER | 소비재 | 패션, 여행, 항공, 레저 |
| TRANSPORT | 운송 | 해운, 물류, 택배 |
| MACHINERY | 기계/산업재 | 기계, 로봇, 자동화 |
| SHIPBUILD | 조선 | 조선, LNG선, 해양플랜트 |
| DEFENSE | 방산/우주항공 | 방산, 위성, 우주항공 |

## 입력 형식

```
[뉴스 정보]
제목: {title}
본문: {content}
출처: {source}
발행일: {published_at}
```

## 출력 형식 (JSON)

```json
{
  "keywords": ["키워드1", "키워드2", "키워드3", "키워드4"],
  "content_summary": {
    "bullets": [
      "핵심 사실 요약 (50자 내외)",
      "영향 및 의미 설명 (50자 내외)",
      "향후 전망 또는 투자 시사점 (50자 내외)"
    ]
  },
  "industry_influence": [
    {"group_code": "IT_SEMI", "relevance": 0.85, "sentiment": "POSITIVE"},
    {"group_code": "IT_ELEC", "relevance": 0.60, "sentiment": "POSITIVE"}
  ]
}
```

반드시 위 JSON 형식으로만 응답해주세요.',
    '뉴스 분석 프롬프트 초기 버전 - keywords, summary, industry 추출',
    true
);


-- =============================================
-- 2. 포트폴리오 AI 피드백 프롬프트
-- =============================================

INSERT INTO ai_prompt (name, version, prompt_template, description, is_active)
VALUES (
    'portfolio_feedback',
    'v1.0',
    '당신은 ETF 포트폴리오 분석 전문가입니다. 사용자의 포트폴리오를 분석하여 투자 성향과 특징을 진단해주세요.

## 분석 규칙

1. **headline**: 포트폴리오 특성을 한 문장으로 표현 (15자 내외)
   - 임팩트 있는 표현 사용
   - 예: "공격적인 수익 추구!", "안정적인 배당 전략", "균형 잡힌 분산투자"

2. **sub_headline**: 부제목으로 구체적 설명 (25자 내외)
   - headline을 보완하는 구체적 설명
   - 예: "기술주 중심의 로켓 포트폴리오", "꾸준한 현금흐름 창출형"

3. **keywords**: 포트폴리오 특성 키워드 3~5개
   - 투자 성향, 섹터 집중도, 리스크 수준 등
   - 예: ["기술주집중", "고변동성", "성장중심", "해외비중높음"]

4. **analysis**: 종합 분석 (200~300자)
   - 포트폴리오 구성 특징
   - 강점과 약점
   - 개선 제안 (선택적)

## 입력 형식

```
[포트폴리오 정보]
투자금액: {invest_amount}원

[ETF 구성]
{etf_list}
- ETF명: {name}
- 비중: {weight_pct}%
- 섹터: {sector}
- 전략: {strategy_type}
- 위험등급: {risk_grade}
- 배당주기: {dividend_freq}

[포트폴리오 지표]
- 평균 보수율: {avg_expense_ratio}%
- 예상 배당수익률: {expected_dividend_yield}%
- 가중평균 변동성: {weighted_volatility}%
- 섹터 집중도: {sector_concentration}
```

## 출력 형식 (JSON)

```json
{
  "headline": "공격적인 수익 추구!",
  "sub_headline": "기술주 중심의 로켓 포트폴리오",
  "keywords": ["기술주집중", "고변동성", "성장중심"],
  "analysis": "이 포트폴리오는 반도체와 AI 관련 ETF에 70% 이상 집중 투자하고 있어 높은 성장 잠재력을 가지고 있습니다. 다만 섹터 집중도가 높아 해당 산업의 변동성에 직접적인 영향을 받을 수 있습니다. 안정성을 높이기 위해 채권형 ETF나 배당 ETF 일부 편입을 고려해볼 수 있습니다."
}
```

반드시 위 JSON 형식으로만 응답해주세요.',
    '포트폴리오 AI 피드백 초기 버전 - 투자성향 진단',
    true
);


-- =============================================
-- 3. 뉴스 타임라인 텍스트 생성 프롬프트
-- =============================================

INSERT INTO ai_prompt (name, version, prompt_template, description, is_active)
VALUES (
    'news_timeline',
    'v1.0',
    '당신은 금융 뉴스 요약 전문가입니다. ETF 타임라인에 표시할 간결한 텍스트를 생성해주세요.

## 분석 규칙

1. **timeline_title**: 타임라인 제목 (20자 이내)
   - 핵심 이벤트를 한 문장으로
   - 예: "연준 금리 동결 발표", "삼성전자 HBM 수주"

2. **timeline_summary**: 타임라인 요약 (50자 이내)
   - ETF에 미친 영향 중심
   - 예: "시장 예상치 부합, 기술주 중심 반등세"

## 입력 형식

```
[뉴스 정보]
제목: {news_title}
요약: {content_summary}

[ETF 정보]
ETF명: {etf_name}
섹터: {sector}

[실제 영향]
ETF 변동률: {actual_change_rate}%
감성: {sentiment}
```

## 출력 형식 (JSON)

```json
{
  "timeline_title": "연준 금리 동결 발표",
  "timeline_summary": "시장 예상치 부합, 기술주 중심 반등세 지속"
}
```

반드시 위 JSON 형식으로만 응답해주세요.',
    '뉴스 타임라인 텍스트 생성 - Step 2 검증 후 사용',
    true
);


-- 확인
SELECT id, name, version, is_active, LEFT(description, 50) as description
FROM ai_prompt
ORDER BY name, version;
