"""원격 DB 스키마 확인"""
import psycopg

remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')
cur = remote.cursor()

# 테이블 존재 여부 확인
tables = ['etf_sector_cluster', 'etf_sector_ai_history', 'news_etf_influence']

print("=== 원격 DB 테이블 확인 ===\n")

for table in tables:
    cur.execute("""
        SELECT EXISTS (
            SELECT FROM information_schema.tables
            WHERE table_name = %s
        )
    """, (table,))
    exists = cur.fetchone()[0]
    print(f"{table}: {'존재' if exists else '없음'}")

    if exists:
        # 컬럼 확인
        cur.execute("""
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = %s
            ORDER BY ordinal_position
        """, (table,))
        cols = cur.fetchall()
        for col in cols:
            print(f"  - {col[0]}: {col[1]}")
    print()

remote.close()
