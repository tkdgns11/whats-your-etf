"""원격 DB에 industry_code 업데이트"""
import json
import psycopg

# 원격 DB 연결 (SSH 터널 통해)
conn = psycopg.connect(
    host='localhost',
    port=5433,
    user='wye',
    password='wyedbpw!$@#%!',
    dbname='whatsyouretf'
)
cur = conn.cursor()

# 1. 누락된 코드 추가
sql = '''
INSERT INTO industry_classification (code, name, level, parent_code, group_code, group_name) VALUES
('AUTO_PART', '자동차 부품', 4, 'C30300', 'AUTO', '자동차'),
('BIO_PHAR', '제약/바이오', 4, 'C21200', 'BIO', '바이오/의약'),
('CHEM_COS', '화장품/뷰티', 4, 'C20400', 'CHEM', '화학/소재'),
('CHEM_PET', '석유화학', 4, 'C20100', 'CHEM', '화학/소재'),
('CONS_FAS', '패션/의류', 4, 'C14100', 'CONSUMER', '소비재'),
('CONS_SPO', '스포츠/레저', 4, 'R91100', 'CONSUMER', '소비재'),
('CONS_TRV', '여행/관광', 4, 'I55100', 'CONSUMER', '소비재'),
('CON_INFRA', '건설 인프라', 4, 'F41200', 'CONSTRUCT', '건설'),
('CON_REIT', '리츠/부동산', 4, 'L68100', 'CONSTRUCT', '건설'),
('DISP_LED', 'LED/조명', 4, 'C28400', 'IT_ELEC', '전자/IT'),
('FOOD_GEN', '일반 식품', 4, 'C10700', 'FOOD', '식품/음료'),
('IT_PHONE', '스마트폰/휴대폰', 4, 'C26400', 'IT_ELEC', '전자/IT'),
('IT_SEC', 'IT보안/정보보안', 4, 'J58200', 'IT_SW', '소프트웨어'),
('MACH_3D', '3D 프린팅', 4, 'C29200', 'MACHINERY', '기계'),
('RET_CVS', '편의점/소매', 4, 'G47200', 'RETAIL', '유통/소매'),
('SEMI_CXL', 'CXL (Compute Express Link)', 4, 'C26100', 'IT_SEMI', '반도체'),
('SHIP_GEN', '일반 조선', 4, 'C31100', 'SHIPBUILD', '조선'),
('STL_NF', '비철금속', 4, 'C24200', 'STEEL', '철강/금속'),
('SW_SI', 'SI/시스템통합', 4, 'J62000', 'IT_SW', '소프트웨어'),
('TRANS_SEA', '해운/해상운송', 4, 'H50100', 'TRANSPORT', '운송')
ON CONFLICT (code) DO NOTHING
'''

cur.execute(sql)
conn.commit()
print(f'원격 DB에 누락 코드 추가: {cur.rowcount}건')

cur.execute('SELECT COUNT(*) FROM industry_classification WHERE level = 4')
print(f'Level 4 총 코드 수: {cur.fetchone()[0]}')

# 2. company_info.industry_code 업데이트
with open('C:/SSAFY/project2team/backend/data-service/data/crawled/company_classification.json', 'r', encoding='utf-8') as f:
    classifications = json.load(f)

print(f'\nJSON 파일 로드: {len(classifications)}개 종목')

updated = 0
skipped = 0
not_found = 0

for stock_code, data in classifications.items():
    industry_code = data.get('industry_code')
    industry_group = data.get('industry_group')

    if not industry_code:
        skipped += 1
        continue

    # 종목코드 6자리로 패딩
    padded_code = stock_code.zfill(6)

    # 회사 존재 확인 및 업데이트
    cur.execute('''
        UPDATE company_info
        SET industry_code = %s, industry_group = %s
        WHERE stock_code = %s
        RETURNING id
    ''', (industry_code, industry_group, padded_code))

    if cur.fetchone():
        updated += 1
    else:
        not_found += 1

conn.commit()
print(f'업데이트 완료: {updated}건')
print(f'industry_code 없어서 스킵: {skipped}건')
print(f'company_info에 없는 종목: {not_found}건')

# 확인
cur.execute('SELECT COUNT(*) FROM company_info WHERE industry_code IS NOT NULL')
print(f'\nindustry_code 설정된 회사 수: {cur.fetchone()[0]}')

cur.execute('SELECT COUNT(*) FROM company_info')
print(f'전체 회사 수: {cur.fetchone()[0]}')

conn.close()
print('\n원격 DB 업데이트 완료!')
