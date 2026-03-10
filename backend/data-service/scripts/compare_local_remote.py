"""로컬 DB vs 원격 DB 스키마 비교"""
import psycopg

local = psycopg.connect(host='localhost', port=5432, user='wye', password='wye1234', dbname='whatsyouretf')
remote = psycopg.connect(host='localhost', port=5433, user='wye', password='wyedbpw!$@#%!', dbname='whatsyouretf')
lcur = local.cursor()
rcur = remote.cursor()

def get_schema(cur):
    cur.execute("""
        SELECT table_name FROM information_schema.tables
        WHERE table_schema = 'public' ORDER BY table_name
    """)
    tables = {}
    for (t,) in cur.fetchall():
        cur.execute("""
            SELECT column_name FROM information_schema.columns
            WHERE table_name = %s ORDER BY ordinal_position
        """, (t,))
        tables[t] = sorted([row[0] for row in cur.fetchall()])
    return tables

local_schema = get_schema(lcur)
remote_schema = get_schema(rcur)

print("=== 로컬 vs 원격 스키마 비교 ===\n")

local_tables = set(local_schema.keys())
remote_tables = set(remote_schema.keys())

print(f"로컬 테이블: {len(local_tables)}개")
print(f"원격 테이블: {len(remote_tables)}개")

only_local = local_tables - remote_tables
only_remote = remote_tables - local_tables

if only_local:
    print(f"\n로컬에만 있는 테이블: {sorted(only_local)}")
if only_remote:
    print(f"\n원격에만 있는 테이블: {sorted(only_remote)}")

# 공통 테이블 컬럼 비교
common = local_tables & remote_tables
diff_count = 0

for t in sorted(common):
    local_cols = set(local_schema[t])
    remote_cols = set(remote_schema[t])

    only_local_cols = local_cols - remote_cols
    only_remote_cols = remote_cols - local_cols

    if only_local_cols or only_remote_cols:
        if diff_count == 0:
            print("\n=== 컬럼 차이 ===")
        diff_count += 1
        print(f"\n[{t}]")
        if only_local_cols:
            print(f"  로컬에만: {sorted(only_local_cols)}")
        if only_remote_cols:
            print(f"  원격에만: {sorted(only_remote_cols)}")

if diff_count == 0:
    print("\n컬럼 차이: 없음 (모든 공통 테이블 일치)")

print(f"\n=== 결과: {len(common)}개 테이블 중 {len(common) - diff_count}개 일치 ===")

local.close()
remote.close()
