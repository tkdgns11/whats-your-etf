"""로컬 AI 분석 결과를 원격 서버에 동기화"""
import psycopg
import json

# DB 연결
local = psycopg.connect('postgresql://wye:wye1234@localhost:5432/whatsyouretf')
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')

local_cur = local.cursor()
remote_cur = remote.cursor()

print("=== AI 분석 결과 동기화 ===\n")

# 1. 로컬에서 AI 분석 완료된 뉴스 가져오기
local_cur.execute("""
    SELECT source_url, content_summary, keywords
    FROM news_article
    WHERE content_summary IS NOT NULL
""")
analyzed = local_cur.fetchall()
print(f"로컬 AI 분석 완료: {len(analyzed)}개")

# 2. 원격에 업데이트
updated = 0
for source_url, summary, keywords in analyzed:
    # dict/list는 JSON 문자열로 변환
    if isinstance(summary, (dict, list)):
        summary_str = json.dumps(summary, ensure_ascii=False)
    else:
        summary_str = summary
        
    if isinstance(keywords, (dict, list)):
        keywords_str = json.dumps(keywords, ensure_ascii=False)
    else:
        keywords_str = keywords
    
    remote_cur.execute("""
        UPDATE news_article
        SET content_summary = %s::jsonb, keywords = %s::jsonb
        WHERE source_url = %s AND content_summary IS NULL
    """, (summary_str, keywords_str, source_url))
    if remote_cur.rowcount > 0:
        updated += 1

remote.commit()
print(f"원격 서버 업데이트: {updated}개")

# 3. 현황 확인
remote_cur.execute("SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL")
remote_analyzed = remote_cur.fetchone()[0]
print(f"\n원격 서버 AI 분석 완료 현황: {remote_analyzed}개")

local.close()
remote.close()
print("\n동기화 완료!")
