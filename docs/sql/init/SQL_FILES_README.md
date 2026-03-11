# SQL 데이터 파일 설명

`docs/sql/init/` 폴더에 위치한 SQL 덤프 파일들입니다.

## 파일 목록

| 파일명 | 크기 | 생성일 | 용도 |
|--------|------|--------|------|
| `data_dump.sql` | 2.7MB | 2026-03-09 | 로컬 DB 전체 백업 (pg_dump) |
| `data_dump2.sql` | 4.0MB | 2026-03-09 | 로컬 DB 추가 백업 |
| `remote_data.sql` | 4.3MB | 2026-03-09 | 원격 서버용 정제된 기본 데이터 |
| `remote_news.sql` | 4.9MB | 2026-03-09 | 원격 서버용 뉴스 데이터 |
| `other_composition.sql` | 12KB | 2026-03-09 | 선물/파생상품 ETF 구성 데이터 |

## 상세 설명

### data_dump.sql / data_dump2.sql
- PostgreSQL `pg_dump`로 생성된 로컬 DB 전체 백업
- 모든 테이블 스키마 + 데이터 포함
- 로컬 개발 환경 복원용

### remote_data.sql
원격 서버(j14d102.p.ssafy.io)에 삽입용으로 정제된 데이터

**포함 테이블:**
- `etf` (173건) - ETF 기본 정보
- `company_info` (1,228건) - 회사 정보
- `stock` (1,228건) - 주식 정보
- `etf_stock_composition` (13,463건) - ETF 구성종목
- `etf_sector_cluster` (1,445건) - ETF 섹터 클러스터
- `ai_prompt` - AI 분석 프롬프트

### remote_news.sql
원격 서버에 삽입용 뉴스 데이터

**포함 테이블:**
- `news_article` - 뉴스 기사
- `news_stock_mapping` - 뉴스-종목 매핑

### other_composition.sql
선물/파생상품 기반 ETF의 구성 데이터 (etf_stock_composition에 들어가지 않는 자산)

**대상 ETF (12개):**
- 인버스 ETF: KODEX 인버스, TIGER 인버스
- 레버리지 ETF: TIGER 200선물레버리지
- 선물 ETF: 골드선물, 코스닥150선물 등
- 우선주 ETF: TIGER 우선주

**자산 유형:**
- `FUTURES` - 선물 (KOSPI200, 코스닥150, 금, USD 등)
- `BOND` - 채권/RP
- `CASH` - 현금성 자산
- `ETF` - ETF 내 ETF 보유
- `PREFERRED_STOCK` - 우선주

**총 49건** 데이터

## 사용 방법

### 로컬 DB 복원
```bash
# Docker PostgreSQL 컨테이너에서
docker exec -i wye-postgres psql -U wye -d whatsyouretf < data_dump.sql
```

### 원격 서버 데이터 삽입
```bash
# SSH로 파일 전송
scp -i J14D102T.pem remote_data.sql ubuntu@j14d102.p.ssafy.io:/tmp/

# 원격 서버에서 실행
ssh -i J14D102T.pem ubuntu@j14d102.p.ssafy.io
docker exec -i wye-postgres psql -U wye -d whatsyouretf < /tmp/remote_data.sql
```

## 주의사항
- 이 파일들은 `.gitignore`에 포함되어 있어 Git에 커밋되지 않음
- 민감한 데이터가 포함될 수 있으므로 외부 공유 금지
- 원격 서버 데이터 이전 완료 후 삭제 가능
