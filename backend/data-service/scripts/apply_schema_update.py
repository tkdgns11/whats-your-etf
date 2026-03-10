"""원격 DB에 스키마 업데이트 적용"""
import psycopg
from pathlib import Path

sql_path = Path(__file__).parent / 'remote_schema_update.sql'

# 원격 DB 연결
conn = psycopg.connect(
    host='localhost', port=5433,
    user='wye', password='wyedbpw!$@#%!',
    dbname='whatsyouretf'
)
conn.autocommit = True
cur = conn.cursor()

print("=== 원격 DB 스키마 업데이트 시작 ===\n")

# SQL 파일 읽기 및 실행
with open(sql_path, 'r', encoding='utf-8') as f:
    sql_content = f.read()

# 각 명령어 실행 (주석 제외)
statements = []
current = []

for line in sql_content.split('\n'):
    line = line.strip()
    if line.startswith('--') or not line:
        continue
    current.append(line)
    if line.endswith(';'):
        statements.append(' '.join(current))
        current = []

success = 0
errors = []

for stmt in statements:
    try:
        cur.execute(stmt)
        print(f"✓ {stmt[:60]}...")
        success += 1
    except Exception as e:
        errors.append((stmt[:60], str(e)))
        print(f"✗ {stmt[:60]}... - {e}")

print(f"\n=== 결과: {success} 성공, {len(errors)} 실패 ===")

# 확인
print("\n=== 생성된 테이블 확인 ===")
for table in ['alert_message_template', 'etf_disclosure', 'etf_sector_ai_history']:
    cur.execute("""
        SELECT EXISTS (
            SELECT FROM information_schema.tables
            WHERE table_name = %s
        )
    """, (table,))
    exists = cur.fetchone()[0]
    print(f"  {table}: {'존재' if exists else '없음'}")

print("\n=== etf 테이블 컬럼 확인 ===")
cur.execute("""
    SELECT column_name FROM information_schema.columns
    WHERE table_name = 'etf' AND column_name IN ('avg_per', 'avg_pbr', 'avg_roe')
""")
cols = [row[0] for row in cur.fetchall()]
print(f"  밸류에이션 컬럼: {cols}")

conn.close()
print("\n완료!")
