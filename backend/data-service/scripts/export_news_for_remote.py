"""
원격 서버용 뉴스 데이터 내보내기
"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import text
from app.database import SessionLocal


def export_news():
    db = SessionLocal()
    output_lines = []
    output_lines.append("-- News Data Export")
    output_lines.append("")

    # 1. News Article
    output_lines.append("-- News Articles")
    result = db.execute(text("""
        SELECT id, title, content, content_summary, source, source_url, thumbnail_url,
               category_code, keywords, published_at, view_count, is_active, created_at
        FROM news_article
    """))

    for row in result:
        values = []
        for val in row:
            if val is None:
                values.append("NULL")
            elif isinstance(val, bool):
                values.append("true" if val else "false")
            elif isinstance(val, (int, float)):
                values.append(str(val))
            elif isinstance(val, dict) or isinstance(val, list):
                import json
                json_str = json.dumps(val, ensure_ascii=False).replace("'", "''")
                values.append(f"'{json_str}'")
            else:
                values.append(f"'{str(val).replace(chr(39), chr(39)+chr(39))}'")
        output_lines.append(f"""INSERT INTO news_article (id, title, content, content_summary, source, source_url, thumbnail_url, category_code, keywords, published_at, view_count, is_active, created_at) VALUES ({', '.join(values)}) ON CONFLICT (id) DO NOTHING;""")

    # 2. News Stock Mapping
    output_lines.append("")
    output_lines.append("-- News Stock Mapping")
    result = db.execute(text("SELECT id, news_id, company_id, created_at FROM news_stock_mapping"))

    for row in result:
        created_at = f"'{row[3]}'" if row[3] else "NULL"
        output_lines.append(f"""INSERT INTO news_stock_mapping (id, news_id, company_id, created_at) VALUES ({row[0]}, {row[1]}, {row[2]}, {created_at}) ON CONFLICT (id) DO NOTHING;""")

    # Sequence updates
    output_lines.append("")
    output_lines.append("-- Update sequences")
    output_lines.append("SELECT setval('news_article_id_seq', (SELECT COALESCE(MAX(id), 1) FROM news_article));")
    output_lines.append("SELECT setval('news_stock_mapping_id_seq', (SELECT COALESCE(MAX(id), 1) FROM news_stock_mapping));")

    db.close()

    # Write to file
    with open("C:/SSAFY/project2team/remote_news.sql", "w", encoding="utf-8") as f:
        f.write("\n".join(output_lines))

    print(f"Exported {len(output_lines)} lines to remote_news.sql")


if __name__ == "__main__":
    export_news()
