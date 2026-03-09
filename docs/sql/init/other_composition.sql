-- etf_other_composition 데이터 (선물/파생상품/우선주 기반 ETF)
-- 생성일: 2026-03-09

-- 기존 데이터 삭제
DELETE FROM etf_other_composition;

-- 114800 KODEX 인버스
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, -166050000
FROM etf WHERE stock_code = '114800';

-- 123310 TIGER 인버스
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, -186550000
FROM etf WHERE stock_code = '123310';

-- 132030 KODEX 골드선물(H)
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '금 선물', 'CME_CODE', 'GCJ6', 0.0, 0
FROM etf WHERE stock_code = '132030';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'BOND', '채권/RP', 'INTERNAL_CODE', '8463V1', 0.0, 0
FROM etf WHERE stock_code = '132030';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'CASH', '현금성 자산', 'INTERNAL_CODE', 'ZZ0000', 0.0, 0
FROM etf WHERE stock_code = '132030';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'USD 선물', 'KRX_CODE', 'A75630', 0.0, -562307680
FROM etf WHERE stock_code = '132030';

-- 250780 TIGER 코스닥150선물인버스
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '코스닥150 선물', 'KRX_CODE', 'A06630', 0.0, -107226700
FROM etf WHERE stock_code = '250780';

-- 251340 KODEX 코스닥150선물인버스
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', '229200', 'STOCK_CODE', '229200', 0.0, 48565575
FROM etf WHERE stock_code = '251340';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '코스닥150 선물', 'KRX_CODE', 'A06630', 0.0, -259357000
FROM etf WHERE stock_code = '251340';

-- 252670 KODEX 200선물인버스2X
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'KODEX 인버스', 'STOCK_CODE', '114800', 0.0, 4927574
FROM etf WHERE stock_code = '252670';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, -53300000
FROM etf WHERE stock_code = '252670';

-- 252710 TIGER 200선물인버스2X
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'TIGER 인버스', 'STOCK_CODE', '123310', 0.0, 5744112
FROM etf WHERE stock_code = '252710';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, -51250000
FROM etf WHERE stock_code = '252710';

-- 261140 TIGER 우선주
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '삼성전자우', 'STOCK_CODE', '005935', 25.8, 123198800
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '현대차2우B', 'STOCK_CODE', '005387', 17.04, 79968000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '삼성SDI우', 'STOCK_CODE', '00680K', 12.87, 60153600
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', 'LG화학우', 'STOCK_CODE', '051915', 5.66, 26846400
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '삼성화재우', 'STOCK_CODE', '000815', 4.72, 22201500
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '두산우', 'STOCK_CODE', '000155', 4.37, 20320000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', 'LG전자우', 'STOCK_CODE', '066575', 4.34, 20358000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '현대모비스우', 'STOCK_CODE', '071055', 4.25, 19926000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '신한지주우', 'STOCK_CODE', '00088K', 3.83, 17826500
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '대한항공우', 'STOCK_CODE', '003545', 2.4, 11414000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '현대건설우', 'STOCK_CODE', '005945', 2.17, 10097850
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '경방우', 'STOCK_CODE', '009155', 2.16, 10136700
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '아모레퍼시픽우', 'STOCK_CODE', '090435', 1.95, 9071850
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '삼성SDI우', 'STOCK_CODE', '006405', 1.63, 7536000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', 'CJ제일제당우', 'STOCK_CODE', '00104K', 1.34, 6436000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', 'LG생활건강우', 'STOCK_CODE', '051905', 1.05, 4940400
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '대한항공우2B', 'STOCK_CODE', '003555', 0.99, 4738800
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', 'S-Oil우', 'STOCK_CODE', '010955', 0.95, 4512200
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '한양증권우', 'STOCK_CODE', '011785', 0.86, 4026000
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'PREFERRED_STOCK', '미래에셋증권우', 'STOCK_CODE', '097955', 0.8, 3801900
FROM etf WHERE stock_code = '261140';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'CASH', '현금성 자산', 'INTERNAL_CODE', '010010', 0.82, 0
FROM etf WHERE stock_code = '261140';

-- 267770 TIGER 200선물레버리지
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'TIGER 200', 'STOCK_CODE', '102110', 0.0, 1230759200
FROM etf WHERE stock_code = '267770';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'TIGER 레버리지', 'STOCK_CODE', '123320', 0.0, 363343715
FROM etf WHERE stock_code = '267770';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'KBSTAR 200', 'STOCK_CODE', '252400', 0.0, 286041400
FROM etf WHERE stock_code = '267770';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, 10196700000
FROM etf WHERE stock_code = '267770';

-- 280940 KODEX 골드선물인버스(H)
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '금 선물', 'CME_CODE', 'BQJ6', 0.0, 0
FROM etf WHERE stock_code = '280940';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'BOND', '채권/RP', 'INTERNAL_CODE', '4347Y6', 0.0, 0
FROM etf WHERE stock_code = '280940';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'USD 선물', 'KRX_CODE', 'A75630', 0.0, -34924120
FROM etf WHERE stock_code = '280940';

-- 319640 TIGER 골드선물(H)
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '금 선물', 'CME_CODE', 'GCJ6', 0.0, 1334270793
FROM etf WHERE stock_code = '319640';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'BOND', '채권/RP', 'INTERNAL_CODE', '8463V1', 0.0, 0
FROM etf WHERE stock_code = '319640';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'USD 선물', 'KRX_CODE', 'A75630', 0.0, -769064340
FROM etf WHERE stock_code = '319640';

-- 360150 KODEX 코스닥150롱코스피200숏선물
INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'KODEX 코스닥150', 'STOCK_CODE', '229200', 17.81, 106194375
FROM etf WHERE stock_code = '360150';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'ETF', 'KODEX 인버스', 'STOCK_CODE', '114800', 13.26, 82750776
FROM etf WHERE stock_code = '360150';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', '코스닥150 선물', 'KRX_CODE', 'A06630', 0.0, 509036500
FROM etf WHERE stock_code = '360150';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'FUTURES', 'KOSPI200 선물', 'KRX_CODE', 'A01630', 0.0, -537100000
FROM etf WHERE stock_code = '360150';

INSERT INTO etf_other_composition (etf_id, asset_type, asset_name, identifier_type, identifier_value, weight, market_value)
SELECT id, 'CASH', '현금성 자산', 'INTERNAL_CODE', '010010', 68.93, 0
FROM etf WHERE stock_code = '360150';

-- 결과 확인
SELECT
    e.stock_code,
    e.name as etf_name,
    COUNT(*) as composition_count
FROM etf_other_composition eoc
JOIN etf e ON eoc.etf_id = e.id
GROUP BY e.stock_code, e.name
ORDER BY e.stock_code;
