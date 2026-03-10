"""원격 DB vs ERD.sql 전체 스키마 비교 및 ALTER 생성"""
import psycopg
import re
from pathlib import Path

# ERD.sql 파싱
def parse_erd_sql(erd_path: str) -> dict:
    """ERD.sql에서 테이블 정의 추출"""
    with open(erd_path, 'r', encoding='utf-8') as f:
        content = f.read()

    tables = {}

    # CREATE TABLE 패턴
    pattern = r'CREATE TABLE "(\w+)"\s*\((.*?)\);'
    for match in re.finditer(pattern, content, re.DOTALL):
        table_name = match.group(1)
        body = match.group(2)

        columns = {}
        constraints = []

        for line in body.split('\n'):
            line = line.strip().rstrip(',')
            if not line or line.startswith('--'):
                continue

            # CONSTRAINT 처리
            if line.startswith('CONSTRAINT') or line.startswith('UNIQUE('):
                constraints.append(line)
                continue

            # 컬럼 처리
            col_match = re.match(r'"(\w+)"\s+(.+)', line)
            if col_match:
                col_name = col_match.group(1)
                col_def = col_match.group(2).strip()
                columns[col_name] = col_def

        tables[table_name] = {
            'columns': columns,
            'constraints': constraints
        }

    return tables

# 원격 DB 스키마 조회
def get_remote_schema(conn) -> dict:
    """원격 DB에서 테이블/컬럼 정보 조회"""
    cur = conn.cursor()

    # 테이블 목록
    cur.execute("""
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
        ORDER BY table_name
    """)
    table_names = [row[0] for row in cur.fetchall()]

    tables = {}
    for table in table_names:
        cur.execute("""
            SELECT column_name, data_type, is_nullable, column_default,
                   character_maximum_length, numeric_precision, numeric_scale
            FROM information_schema.columns
            WHERE table_name = %s
            ORDER BY ordinal_position
        """, (table,))

        columns = {}
        for row in cur.fetchall():
            col_name = row[0]
            data_type = row[1]
            is_nullable = row[2] == 'YES'
            default = row[3]
            max_len = row[4]
            precision = row[5]
            scale = row[6]

            columns[col_name] = {
                'type': data_type,
                'nullable': is_nullable,
                'default': default,
                'max_len': max_len,
                'precision': precision,
                'scale': scale
            }

        tables[table] = columns

    return tables

# 타입 정규화 (비교용)
def normalize_type(erd_type: str) -> str:
    """ERD 타입을 PostgreSQL 실제 타입으로 정규화"""
    erd_type = erd_type.upper()

    if 'BIGSERIAL' in erd_type:
        return 'bigint'
    if 'SERIAL' in erd_type:
        return 'integer'
    if 'BIGINT' in erd_type:
        return 'bigint'
    if 'INTEGER' in erd_type:
        return 'integer'
    if 'BOOLEAN' in erd_type:
        return 'boolean'
    if 'JSONB' in erd_type:
        return 'jsonb'
    if 'TEXT' in erd_type:
        return 'text'
    if 'TIMESTAMP' in erd_type:
        return 'timestamp without time zone'
    if 'DATE' in erd_type:
        return 'date'

    # VARCHAR(n)
    match = re.match(r'VARCHAR\((\d+)\)', erd_type)
    if match:
        return f'character varying'

    # DECIMAL(p,s)
    match = re.match(r'DECIMAL\((\d+),\s*(\d+)\)', erd_type)
    if match:
        return 'numeric'

    return erd_type.lower()

def compare_schemas(erd_tables: dict, remote_tables: dict):
    """스키마 비교"""
    print("=" * 60)
    print("ERD.sql vs 원격 DB 스키마 비교")
    print("=" * 60)

    erd_table_names = set(erd_tables.keys())
    remote_table_names = set(remote_tables.keys())

    # 1. ERD에만 있는 테이블 (원격에 생성 필요)
    missing_tables = erd_table_names - remote_table_names
    if missing_tables:
        print("\n### ERD에만 있음 (원격에 CREATE 필요)")
        for t in sorted(missing_tables):
            print(f"  - {t}")

    # 2. 원격에만 있는 테이블 (ERD에 없음)
    extra_tables = remote_table_names - erd_table_names
    if extra_tables:
        print("\n### 원격에만 있음 (ERD에 없음)")
        for t in sorted(extra_tables):
            print(f"  - {t}")

    # 3. 공통 테이블 컬럼 비교
    common_tables = erd_table_names & remote_table_names

    alter_statements = []

    print("\n### 컬럼 차이")
    for table in sorted(common_tables):
        erd_cols = erd_tables[table]['columns']
        remote_cols = remote_tables[table]

        erd_col_names = set(erd_cols.keys())
        remote_col_names = set(remote_cols.keys())

        missing_cols = erd_col_names - remote_col_names
        extra_cols = remote_col_names - erd_col_names

        if missing_cols or extra_cols:
            print(f"\n  [{table}]")

            if missing_cols:
                print(f"    ERD에만 있음 (ADD 필요):")
                for col in sorted(missing_cols):
                    col_def = erd_cols[col]
                    print(f"      - {col}: {col_def}")
                    # ALTER 문 생성
                    alter_statements.append(
                        f"ALTER TABLE \"{table}\" ADD COLUMN \"{col}\" {col_def};"
                    )

            if extra_cols:
                print(f"    원격에만 있음 (ERD에 없음):")
                for col in sorted(extra_cols):
                    info = remote_cols[col]
                    print(f"      - {col}: {info['type']}")

    # ALTER 문 출력
    if alter_statements:
        print("\n" + "=" * 60)
        print("생성된 ALTER 문")
        print("=" * 60)
        for stmt in alter_statements:
            print(stmt)

    return missing_tables, alter_statements

def main():
    erd_path = Path(__file__).parent.parent.parent.parent / 'docs' / 'sql' / 'ERD.sql'
    print(f"ERD.sql 경로: {erd_path}")

    # ERD.sql 파싱
    erd_tables = parse_erd_sql(str(erd_path))
    print(f"ERD.sql 테이블 수: {len(erd_tables)}")
    print(f"테이블 목록: {', '.join(sorted(erd_tables.keys()))}\n")

    # 원격 DB 연결
    remote = psycopg.connect(
        host='localhost', port=5433,
        user='wye', password='wyedbpw!$@#%!',
        dbname='whatsyouretf'
    )

    # 원격 스키마 조회
    remote_tables = get_remote_schema(remote)
    print(f"원격 DB 테이블 수: {len(remote_tables)}")
    print(f"테이블 목록: {', '.join(sorted(remote_tables.keys()))}\n")

    # 비교
    missing_tables, alter_statements = compare_schemas(erd_tables, remote_tables)

    remote.close()

if __name__ == '__main__':
    main()
