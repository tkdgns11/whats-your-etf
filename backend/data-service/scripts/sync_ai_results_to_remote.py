"""로컬 AI 분석 결과 → 원격 DB 동기화"""
import psycopg
import json

# 로컬 DB 연결
local = psycopg.connect(
    host='localhost', port=5432,
    user='wye', password='wye1234',
    dbname='whatsyouretf'
)

# 원격 DB 연결
remote = psycopg.connect(
    host='localhost', port=5433,
    user='wye', password='wyedbpw!$@#%!',
    dbname='whatsyouretf'
)
remote.autocommit = True

local_cur = local.cursor()
remote_cur = remote.cursor()

print("=== AI 분석 결과 원격 동기화 ===\n")

# 로컬에서 AI 분석 완료된 뉴스 조회
local_cur.execute("""
    SELECT id, source_url, content_summary, keywords
    FROM news_article
    WHERE content_summary IS NOT NULL
""")
local_analyzed = local_cur.fetchall()
print(f"로컬 AI 분석 완료: {len(local_analyzed)}건")

# 원격에 업데이트
updated = 0
not_found = 0
errors = []

for row in local_analyzed:
    news_id, source_url, content_summary, keywords = row

    try:
        # source_url로 원격 뉴스 찾기
        remote_cur.execute("""
            SELECT id FROM news_article WHERE source_url = %s
        """, (source_url,))
        result = remote_cur.fetchone()

        if result:
            remote_id = result[0]
            # AI 결과 업데이트
            remote_cur.execute("""
                UPDATE news_article
                SET content_summary = %s, keywords = %s
                WHERE id = %s
            """, (json.dumps(content_summary) if content_summary else None,
                  json.dumps(keywords) if keywords else None,
                  remote_id))
            updated += 1
        else:
            not_found += 1

    except Exception as e:
        errors.append((news_id, str(e)))

    if (updated + not_found) % 500 == 0:
        print(f"  진행: {updated + not_found}/{len(local_analyzed)}")

print(f"\n=== 결과 ===")
print(f"  업데이트: {updated}건")
print(f"  원격에 없음: {not_found}건")
print(f"  에러: {len(errors)}건")

# 검증
remote_cur.execute("""
    SELECT COUNT(*) FROM news_article WHERE content_summary IS NOT NULL
""")
remote_analyzed = remote_cur.fetchone()[0]
print(f"\n원격 AI 분석 완료 뉴스: {remote_analyzed}건")

local.close()
remote.close()
print("\n완료!")
