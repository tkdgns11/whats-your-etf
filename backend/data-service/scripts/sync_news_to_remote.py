"""뉴스 데이터 로컬 → 원격 동기화"""
import json
import psycopg

# DB 연결
local = psycopg.connect('postgresql://wye:wye1234@localhost:5432/whatsyouretf')
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')

local_cur = local.cursor()
remote_cur = remote.cursor()

# 1. 원격에 없는 뉴스 찾기
print("=== 뉴스 동기화 시작 ===")

# 원격 DB의 기존 source_url 목록
remote_cur.execute("SELECT source_url FROM news_article")
remote_urls = set(row[0] for row in remote_cur.fetchall())
print(f"원격 DB 기존 뉴스: {len(remote_urls)}개")

# 로컬에서 원격에 없는 뉴스 가져오기
local_cur.execute("""
    SELECT id, title, content, source, source_url, thumbnail_url,
           category_code, content_summary, keywords, sentiment_score,
           published_at, created_at
    FROM news_article
""")
local_news = local_cur.fetchall()
print(f"로컬 DB 전체 뉴스: {len(local_news)}개")

# 동기화할 뉴스 필터링
to_sync = []
for row in local_news:
    source_url = row[4]
    if source_url not in remote_urls:
        to_sync.append(row)

print(f"동기화 필요: {len(to_sync)}개")

# 2. 뉴스 삽입
inserted = 0
for row in to_sync:
    (local_id, title, content, source, source_url, thumbnail_url,
     category_code, content_summary, keywords, sentiment_score,
     published_at, created_at) = row

    try:
        # keywords가 dict/list면 JSON 문자열로 변환
        if keywords and not isinstance(keywords, str):
            keywords = json.dumps(keywords, ensure_ascii=False)
        if content_summary and not isinstance(content_summary, str):
            content_summary = json.dumps(content_summary, ensure_ascii=False)

        remote_cur.execute("""
            INSERT INTO news_article
            (title, content, source, source_url, thumbnail_url,
             category_code, content_summary, keywords, sentiment_score,
             published_at, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (source_url) DO NOTHING
            RETURNING id
        """, (title, content, source, source_url, thumbnail_url,
              category_code, content_summary, keywords, sentiment_score,
              published_at, created_at))

        if remote_cur.fetchone():
            inserted += 1

    except Exception as e:
        print(f"에러: {e}")
        continue

remote.commit()
print(f"\n뉴스 삽입 완료: {inserted}개")

# 3. news_stock_mapping 동기화
print("\n=== 뉴스-종목 매핑 동기화 ===")

# 원격 DB의 source_url → id 매핑 생성
remote_cur.execute("SELECT id, source_url FROM news_article")
remote_url_to_id = {row[1]: row[0] for row in remote_cur.fetchall()}

# 로컬 DB의 id → source_url 매핑
local_cur.execute("SELECT id, source_url FROM news_article")
local_id_to_url = {row[0]: row[1] for row in local_cur.fetchall()}

# 로컬 매핑 가져오기
local_cur.execute("""
    SELECT nsm.news_id, nsm.company_id
    FROM news_stock_mapping nsm
""")
local_mappings = local_cur.fetchall()
print(f"로컬 매핑: {len(local_mappings)}건")

# 원격 기존 매핑
remote_cur.execute("SELECT news_id, company_id FROM news_stock_mapping")
remote_mappings = set((row[0], row[1]) for row in remote_cur.fetchall())
print(f"원격 기존 매핑: {len(remote_mappings)}건")

# 동기화
mapping_inserted = 0
for local_news_id, company_id in local_mappings:
    source_url = local_id_to_url.get(local_news_id)
    if not source_url:
        continue

    remote_news_id = remote_url_to_id.get(source_url)
    if not remote_news_id:
        continue

    if (remote_news_id, company_id) not in remote_mappings:
        try:
            remote_cur.execute("""
                INSERT INTO news_stock_mapping (news_id, company_id)
                VALUES (%s, %s)
                ON CONFLICT DO NOTHING
            """, (remote_news_id, company_id))
            mapping_inserted += 1
        except Exception as e:
            continue

remote.commit()
print(f"매핑 삽입 완료: {mapping_inserted}건")

# 최종 확인
remote_cur.execute("SELECT COUNT(*) FROM news_article")
print(f"\n원격 DB 최종 뉴스 수: {remote_cur.fetchone()[0]}개")
remote_cur.execute("SELECT COUNT(*) FROM news_stock_mapping")
print(f"원격 DB 최종 매핑 수: {remote_cur.fetchone()[0]}건")

local.close()
remote.close()
print("\n동기화 완료!")
