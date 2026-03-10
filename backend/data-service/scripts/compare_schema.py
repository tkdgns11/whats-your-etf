"""원격 DB vs ERD.sql 스키마 비교"""
import psycopg

remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')
cur = remote.cursor()

print("=== 원격 DB 전체 테이블 및 컬럼 ===\n")

# 모든 테이블 조회
cur.execute("""
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public'
    ORDER BY table_name
""")
tables = [row[0] for row in cur.fetchall()]

print(f"테이블 수: {len(tables)}")
print(f"테이블 목록: {', '.join(tables)}\n")

# 각 테이블의 컬럼 정보
for table in tables:
    cur.execute("""
        SELECT column_name, data_type, is_nullable, column_default
        FROM information_schema.columns
        WHERE table_name = %s
        ORDER BY ordinal_position
    """, (table,))
    cols = cur.fetchall()

    print(f"\n### {table} ({len(cols)} columns)")
    for col in cols:
        nullable = "NULL" if col[2] == 'YES' else "NOT NULL"
        default = f" DEFAULT {col[3]}" if col[3] else ""
        print(f"  {col[0]}: {col[1]} {nullable}{default}")

remote.close()
