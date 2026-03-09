"""원격 DB 현황 확인"""
import psycopg

# 원격 DB
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')
cur = remote.cursor()

print('=== 원격 DB 현황 ===')

# 회사 정보
cur.execute('SELECT COUNT(*) FROM company_info')
total = cur.fetchone()[0]
cur.execute('SELECT COUNT(*) FROM company_info WHERE industry_code IS NOT NULL')
with_code = cur.fetchone()[0]
cur.execute('SELECT COUNT(*) FROM company_info WHERE industry_group IS NOT NULL')
with_group = cur.fetchone()[0]
print(f'회사 (company_info): {total}개')
print(f'  - industry_code 있음: {with_code}개 ({with_code/total*100:.1f}%)')
print(f'  - industry_group 있음: {with_group}개 ({with_group/total*100:.1f}%)')

# industry_classification
cur.execute('SELECT level, COUNT(*) FROM industry_classification GROUP BY level ORDER BY level')
print(f'\n산업분류 (industry_classification):')
for row in cur.fetchall():
    print(f'  - Level {row[0]}: {row[1]}개')

# ETF
cur.execute('SELECT COUNT(*) FROM etf')
print(f'\nETF: {cur.fetchone()[0]}개')

# 뉴스
cur.execute('SELECT COUNT(*) FROM news_article')
news = cur.fetchone()[0]
cur.execute('SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL')
analyzed = cur.fetchone()[0]
print(f'뉴스: {news}개 (AI 분석 완료: {analyzed}개)')

# 뉴스-종목 매핑
cur.execute('SELECT COUNT(*) FROM news_stock_mapping')
print(f'뉴스-종목 매핑: {cur.fetchone()[0]}건')

# ETF 영향도
cur.execute('SELECT COUNT(*) FROM news_etf_influence')
print(f'뉴스-ETF 영향도: {cur.fetchone()[0]}건')

# ETF 섹터 클러스터
cur.execute('SELECT COUNT(*) FROM etf_sector_cluster')
print(f'ETF 섹터 클러스터: {cur.fetchone()[0]}건')

remote.close()
