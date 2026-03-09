"""뉴스-종목 매핑 동기화"""
import psycopg

print("DB 연결 중...")
local = psycopg.connect('postgresql://wye:wye1234@localhost:5432/whatsyouretf')
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')

local_cur = local.cursor()
remote_cur = remote.cursor()

# 원격 DB의 source_url → id 매핑
print("원격 뉴스 ID 매핑 생성 중...")
remote_cur.execute("SELECT id, source_url FROM news_article")
remote_url_to_id = {row[1]: row[0] for row in remote_cur.fetchall()}
print(f"원격 뉴스: {len(remote_url_to_id)}개")

# 로컬 DB의 id → source_url 매핑
print("로컬 뉴스 ID 매핑 생성 중...")
local_cur.execute("SELECT id, source_url FROM news_article")
local_id_to_url = {row[0]: row[1] for row in local_cur.fetchall()}
print(f"로컬 뉴스: {len(local_id_to_url)}개")

# 원격 기존 매핑
print("원격 기존 매핑 조회 중...")
remote_cur.execute("SELECT news_id, company_id FROM news_stock_mapping")
remote_mappings = set((row[0], row[1]) for row in remote_cur.fetchall())
print(f"원격 기존 매핑: {len(remote_mappings)}건")

# 로컬 매핑 가져오기
print("로컬 매핑 조회 중...")
local_cur.execute("SELECT news_id, company_id FROM news_stock_mapping")
local_mappings = local_cur.fetchall()
print(f"로컬 매핑: {len(local_mappings)}건")

# 동기화
print("\n동기화 중...")
inserted = 0
skipped = 0
for local_news_id, company_id in local_mappings:
    source_url = local_id_to_url.get(local_news_id)
    if not source_url:
        skipped += 1
        continue

    remote_news_id = remote_url_to_id.get(source_url)
    if not remote_news_id:
        skipped += 1
        continue

    if (remote_news_id, company_id) not in remote_mappings:
        try:
            remote_cur.execute("""
                INSERT INTO news_stock_mapping (news_id, company_id)
                VALUES (%s, %s)
                ON CONFLICT DO NOTHING
            """, (remote_news_id, company_id))
            inserted += 1
        except Exception as e:
            print(f"에러: {e}")

remote.commit()
print(f"\n매핑 삽입: {inserted}건")
print(f"스킵: {skipped}건")

# 최종 확인
remote_cur.execute("SELECT COUNT(*) FROM news_stock_mapping")
print(f"원격 DB 최종 매핑 수: {remote_cur.fetchone()[0]}건")

local.close()
remote.close()
print("완료!")
