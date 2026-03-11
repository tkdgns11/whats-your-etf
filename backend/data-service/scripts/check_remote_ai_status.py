"""원격 서버 AI 분석 현황 확인"""
import psycopg

remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')
cur = remote.cursor()

print("=== 원격 서버 AI 분석 현황 ===\n")

# 뉴스 총계
cur.execute("SELECT COUNT(*) FROM news_article")
total = cur.fetchone()[0]

# AI 분석 완료 (content_summary가 있는 것)
cur.execute("SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL")
analyzed = cur.fetchone()[0]

print(f"뉴스 총계: {total}개")
print(f"AI 분석 완료: {analyzed}개 ({analyzed/total*100:.1f}%)")
print(f"미분석: {total - analyzed}개")

# ETF 영향도
cur.execute("SELECT COUNT(*) FROM news_etf_influence")
influence = cur.fetchone()[0]
print(f"\n뉴스-ETF 영향도: {influence}건")

# 최근 분석된 뉴스 샘플
print("\n=== 최근 AI 분석된 뉴스 (최신 5개) ===")
cur.execute("""
    SELECT id, title, created_at
    FROM news_article
    WHERE content_summary IS NOT NULL
    ORDER BY created_at DESC
    LIMIT 5
""")
for row in cur.fetchall():
    print(f"  [{row[0]}] {row[1][:40]}... ({row[2]})")

remote.close()
