"""뉴스 데이터 로컬 → 원격 동기화 (간단 버전)"""
import json
import psycopg

print("DB 연결 중...")
local = psycopg.connect('postgresql://wye:wye1234@localhost:5432/whatsyouretf')
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')

local_cur = local.cursor()
remote_cur = remote.cursor()

print("원격 URL 조회 중...")
remote_cur.execute("SELECT source_url FROM news_article")
remote_urls = set(row[0] for row in remote_cur.fetchall())
print(f"원격 기존: {len(remote_urls)}개")

print("로컬 뉴스 조회 중...")
local_cur.execute("SELECT source_url FROM news_article")
local_urls = set(row[0] for row in local_cur.fetchall())
print(f"로컬 전체: {len(local_urls)}개")

# 동기화 필요한 URL
to_sync_urls = local_urls - remote_urls
print(f"동기화 필요: {len(to_sync_urls)}개")

if not to_sync_urls:
    print("동기화할 뉴스가 없습니다.")
else:
    # 한 번에 100개씩 처리
    to_sync_list = list(to_sync_urls)
    batch_size = 100
    total_inserted = 0

    for i in range(0, len(to_sync_list), batch_size):
        batch = to_sync_list[i:i+batch_size]
        placeholders = ','.join(['%s'] * len(batch))

        local_cur.execute(f"""
            SELECT title, content, source, source_url, thumbnail_url,
                   category_code, content_summary, keywords,
                   published_at, created_at
            FROM news_article
            WHERE source_url IN ({placeholders})
        """, batch)

        rows = local_cur.fetchall()

        for row in rows:
            (title, content, source, source_url, thumbnail_url,
             category_code, content_summary, keywords,
             published_at, created_at) = row

            try:
                if keywords and not isinstance(keywords, str):
                    keywords = json.dumps(keywords, ensure_ascii=False)
                if content_summary and not isinstance(content_summary, str):
                    content_summary = json.dumps(content_summary, ensure_ascii=False)

                remote_cur.execute("""
                    INSERT INTO news_article
                    (title, content, source, source_url, thumbnail_url,
                     category_code, content_summary, keywords,
                     published_at, created_at)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON CONFLICT (source_url) DO NOTHING
                """, (title, content, source, source_url, thumbnail_url,
                      category_code, content_summary, keywords,
                      published_at, created_at))
                total_inserted += 1
            except Exception as e:
                print(f"에러: {e}")

        remote.commit()
        print(f"진행: {min(i+batch_size, len(to_sync_list))}/{len(to_sync_list)}")

    print(f"\n뉴스 삽입 완료: {total_inserted}개")

# 최종 확인
remote_cur.execute("SELECT COUNT(*) FROM news_article")
print(f"원격 DB 최종 뉴스 수: {remote_cur.fetchone()[0]}개")

local.close()
remote.close()
print("완료!")
