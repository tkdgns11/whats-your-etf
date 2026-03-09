"""
원격 서버용 데이터 내보내기

원격 서버 스키마에 맞게 SQL INSERT 문 생성
"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import text
from app.database import SessionLocal

def export_data():
    db = SessionLocal()

    output_lines = []
    output_lines.append("-- Remote Server Data Import")
    output_lines.append("-- Generated for j14d102.p.ssafy.io")
    output_lines.append("")

    # 1. AI Prompt
    output_lines.append("-- AI Prompts")
    result = db.execute(text("SELECT id, name, version, prompt_template, description, is_active, created_at FROM ai_prompt"))
    for row in result:
        prompt_template = row[3].replace("'", "''") if row[3] else ""
        description = row[4].replace("'", "''") if row[4] else ""
        output_lines.append(f"""INSERT INTO ai_prompt (id, name, version, prompt_template, description, is_active, created_at) VALUES ({row[0]}, '{row[1]}', '{row[2]}', '{prompt_template}', '{description}', {row[5]}, '{row[6]}') ON CONFLICT (id) DO NOTHING;""")

    # 2. ETF
    output_lines.append("")
    output_lines.append("-- ETF")
    result = db.execute(text("""
        SELECT id, stock_code, name, asset_manager, category, sector, listing_date,
               expense_ratio, dividend_yield, aum, nav, is_active, asset_class,
               strategy_type, is_leveraged, is_inverse, is_hedged, risk_grade,
               dividend_freq, volatility_1y, created_at, updated_at
        FROM etf
    """))
    for row in result:
        values = []
        for i, val in enumerate(row):
            if val is None:
                values.append("NULL")
            elif isinstance(val, bool):
                values.append("true" if val else "false")
            elif isinstance(val, (int, float)):
                values.append(str(val))
            else:
                values.append(f"'{str(val).replace(chr(39), chr(39)+chr(39))}'")
        output_lines.append(f"""INSERT INTO etf (id, stock_code, name, asset_manager, category, sector, listing_date, expense_ratio, dividend_yield, aum, nav, is_active, asset_class, strategy_type, is_leveraged, is_inverse, is_hedged, risk_grade, dividend_freq, volatility_1y, created_at, updated_at) VALUES ({', '.join(values)}) ON CONFLICT (id) DO NOTHING;""")

    # 3. Company Info (without aliases column)
    output_lines.append("")
    output_lines.append("-- Company Info")
    result = db.execute(text("""
        SELECT id, stock_code, stock_name, company_name, industry_code, industry_group,
               industry_name, ceo_name, homepage, region, description, market_type,
               listing_date, fiscal_month, face_value, listed_shares, is_active,
               data_source, created_at, updated_at
        FROM company_info
    """))
    for row in result:
        values = []
        for i, val in enumerate(row):
            if val is None:
                values.append("NULL")
            elif isinstance(val, bool):
                values.append("true" if val else "false")
            elif isinstance(val, (int, float)):
                values.append(str(val))
            else:
                values.append(f"'{str(val).replace(chr(39), chr(39)+chr(39))}'")
        output_lines.append(f"""INSERT INTO company_info (id, stock_code, stock_name, company_name, industry_code, industry_group, industry_name, ceo_name, homepage, region, description, market_type, listing_date, fiscal_month, face_value, listed_shares, is_active, data_source, created_at, updated_at) VALUES ({', '.join(values)}) ON CONFLICT (id) DO NOTHING;""")

    # 4. Stock
    output_lines.append("")
    output_lines.append("-- Stock")
    result = db.execute(text("SELECT id, ticker, company_id, created_at FROM stock"))
    for row in result:
        created_at = f"'{row[3]}'" if row[3] else "NULL"
        output_lines.append(f"""INSERT INTO stock (id, ticker, company_id, created_at) VALUES ({row[0]}, '{row[1]}', {row[2]}, {created_at}) ON CONFLICT (id) DO NOTHING;""")

    # 5. ETF Stock Composition
    output_lines.append("")
    output_lines.append("-- ETF Stock Composition")
    result = db.execute(text("SELECT id, etf_id, stock_id, weight_pct, base_date, created_at FROM etf_stock_composition"))
    for row in result:
        stock_id = row[2] if row[2] else "NULL"
        weight_pct = row[3] if row[3] else "NULL"
        base_date = f"'{row[4]}'" if row[4] else "NULL"
        created_at = f"'{row[5]}'" if row[5] else "NULL"
        output_lines.append(f"""INSERT INTO etf_stock_composition (id, etf_id, stock_id, weight_pct, base_date, created_at) VALUES ({row[0]}, {row[1]}, {stock_id}, {weight_pct}, {base_date}, {created_at}) ON CONFLICT (id) DO NOTHING;""")

    # 6. ETF Sector Cluster
    output_lines.append("")
    output_lines.append("-- ETF Sector Cluster")
    result = db.execute(text("""
        SELECT id, etf_id, cluster_type, industry_code, industry_name, group_code, group_name,
               sub_sector, weight_pct, stock_count, pos_x, pos_y, radius, distance_to_center,
               ai_analysis, prompt_id, base_date, created_at
        FROM etf_sector_cluster
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
            else:
                values.append(f"'{str(val).replace(chr(39), chr(39)+chr(39))}'")
        output_lines.append(f"""INSERT INTO etf_sector_cluster (id, etf_id, cluster_type, industry_code, industry_name, group_code, group_name, sub_sector, weight_pct, stock_count, pos_x, pos_y, radius, distance_to_center, ai_analysis, prompt_id, base_date, created_at) VALUES ({', '.join(values)}) ON CONFLICT (id) DO NOTHING;""")

    # Sequence updates
    output_lines.append("")
    output_lines.append("-- Update sequences")
    output_lines.append("SELECT setval('ai_prompt_id_seq', (SELECT COALESCE(MAX(id), 1) FROM ai_prompt));")
    output_lines.append("SELECT setval('etf_id_seq', (SELECT COALESCE(MAX(id), 1) FROM etf));")
    output_lines.append("SELECT setval('company_info_id_seq', (SELECT COALESCE(MAX(id), 1) FROM company_info));")
    output_lines.append("SELECT setval('stock_id_seq', (SELECT COALESCE(MAX(id), 1) FROM stock));")
    output_lines.append("SELECT setval('etf_stock_composition_id_seq', (SELECT COALESCE(MAX(id), 1) FROM etf_stock_composition));")
    output_lines.append("SELECT setval('etf_sector_cluster_id_seq', (SELECT COALESCE(MAX(id), 1) FROM etf_sector_cluster));")

    db.close()

    # Write to file
    with open("C:/SSAFY/project2team/remote_data.sql", "w", encoding="utf-8") as f:
        f.write("\n".join(output_lines))

    print(f"Exported {len(output_lines)} lines to remote_data.sql")

if __name__ == "__main__":
    export_data()
