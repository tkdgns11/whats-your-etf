"""
ETF 섹터 클러스터 자동 생성 스크립트

etf_compositions + company_info.industry_group 집계
→ etf_sector_cluster 테이블 자동 생성

기능:
- 구성종목 기반 섹터 비중 집계
- 시각화 좌표 자동 계산 (pos_x, pos_y, radius, distance_to_center)

사용법:
    cd backend/data-service
    python -m scripts.generate_sector_cluster
"""
import sys
import math
from pathlib import Path
from datetime import date
from decimal import Decimal
from collections import defaultdict

sys.path.insert(0, str(Path(__file__).parent.parent))

from sqlalchemy import func
from app.database import SessionLocal
from app.models.etf import ETF, ETFComposition, ETFSectorCluster
from app.models.company import CompanyInfo, IndustryClassification


def get_industry_name(db, group_code: str) -> str:
    """group_code로 산업명 조회"""
    industry = db.query(IndustryClassification).filter(
        IndustryClassification.group_code == group_code
    ).first()
    return industry.group_name if industry else group_code


def calculate_sector_positions(sectors: list) -> list:
    """
    섹터별 버블 위치 계산 (ETF_클러스터_설계.md 섹션 4.2)

    좌표 체계:
    - 정규화: 0.0 ~ 1.0 범위
    - 중심: (0.5, 0.5)
    - 12시 방향부터 시계방향 균등 배치
    - 비중 클수록 중심에 가깝게

    Args:
        sectors: [{"group_code": str, "weight": Decimal, ...}, ...]

    Returns:
        좌표가 추가된 섹터 리스트
    """
    n = len(sectors)
    if n == 0:
        return sectors

    center_x, center_y = 0.5, 0.5
    base_distance = 0.35  # 기본 거리

    for i, sector in enumerate(sectors):
        # 각도 계산 (12시 방향부터 시계방향으로 균등 배치)
        angle = (2 * math.pi * i / n) - (math.pi / 2)

        # 거리 계산 (비중 클수록 중심에 가깝게)
        weight_pct = float(sector["weight"])
        weight_factor = weight_pct / 100
        distance = base_distance * (1 - weight_factor * 0.3)

        # 좌표 계산
        sector["pos_x"] = Decimal(str(round(center_x + distance * math.cos(angle), 6)))
        sector["pos_y"] = Decimal(str(round(center_y + distance * math.sin(angle), 6)))

        # 반지름 (비중에 비례, 0.03 ~ 0.15)
        sector["radius"] = Decimal(str(round(0.03 + (weight_factor * 0.12), 6)))

        # 중심까지 거리
        sector["distance_to_center"] = Decimal(str(round(distance, 6)))

    return sectors


def generate_cluster_for_etf(db, etf: ETF) -> list:
    """
    단일 ETF의 섹터 클러스터 생성

    로직:
    1. etf_compositions에서 해당 ETF의 구성종목 조회
    2. 각 종목의 company_info.industry_group 조회
    3. industry_group별 비중 합산
    """
    # 1. 구성종목 + 회사 정보 조인
    compositions = db.query(
        ETFComposition.weight_pct,
        CompanyInfo.industry_group,
        CompanyInfo.stock_name
    ).join(
        CompanyInfo, ETFComposition.company_id == CompanyInfo.id
    ).filter(
        ETFComposition.etf_id == etf.id,
        CompanyInfo.industry_group != None
    ).all()

    if not compositions:
        print(f"  [!] {etf.name}: 구성종목 없음")
        return []

    # 2. industry_group별 집계
    group_stats = defaultdict(lambda: {"weight": Decimal("0"), "count": 0, "stocks": []})

    for comp in compositions:
        group_code = comp.industry_group
        weight = comp.weight_pct or Decimal("0")

        group_stats[group_code]["weight"] += weight
        group_stats[group_code]["count"] += 1
        group_stats[group_code]["stocks"].append(comp.stock_name)

    # 3. 섹터 리스트 생성 (비중 내림차순 정렬)
    sectors = []
    for group_code, stats in sorted(group_stats.items(), key=lambda x: x[1]["weight"], reverse=True):
        sectors.append({
            "group_code": group_code,
            "group_name": get_industry_name(db, group_code),
            "weight": stats["weight"],
            "count": stats["count"],
            "stocks": stats["stocks"]
        })

    # 4. 좌표 계산
    sectors = calculate_sector_positions(sectors)

    # 5. ETFSectorCluster 레코드 생성
    clusters = []
    today = date.today()

    for sector in sectors:
        cluster = ETFSectorCluster(
            etf_id=etf.id,
            cluster_type="GROUP_CODE",
            group_code=sector["group_code"],
            group_name=sector["group_name"],
            weight_pct=sector["weight"],
            stock_count=sector["count"],
            pos_x=sector["pos_x"],
            pos_y=sector["pos_y"],
            radius=sector["radius"],
            distance_to_center=sector["distance_to_center"],
            base_date=today
        )
        clusters.append(cluster)

        print(f"    {sector['group_code']} ({sector['group_name']}): {sector['weight']:.1f}% ({sector['count']}종목)")
        print(f"      → pos=({sector['pos_x']:.3f}, {sector['pos_y']:.3f}), r={sector['radius']:.3f}")
        print(f"      → {', '.join(sector['stocks'][:5])}{'...' if len(sector['stocks']) > 5 else ''}")

    return clusters


def generate_all_clusters(db, clear_existing: bool = True) -> dict:
    """
    모든 ETF에 대해 섹터 클러스터 생성

    Args:
        clear_existing: 기존 데이터 삭제 여부

    Returns:
        {"total_etfs": int, "generated": int, "clusters": int}
    """
    stats = {"total_etfs": 0, "generated": 0, "clusters": 0}

    # 기존 데이터 삭제
    if clear_existing:
        deleted = db.query(ETFSectorCluster).filter(
            ETFSectorCluster.cluster_type == "GROUP_CODE"
        ).delete()
        print(f"기존 GROUP_CODE 데이터 삭제: {deleted}건")

    # 모든 활성 ETF 조회
    etfs = db.query(ETF).filter(ETF.is_active == True).all()
    stats["total_etfs"] = len(etfs)

    print(f"\n총 {len(etfs)}개 ETF 처리 시작\n")
    print("=" * 60)

    for etf in etfs:
        print(f"\n[{etf.stock_code}] {etf.name}")

        clusters = generate_cluster_for_etf(db, etf)

        if clusters:
            for c in clusters:
                db.add(c)
            stats["generated"] += 1
            stats["clusters"] += len(clusters)

    db.commit()

    print("\n" + "=" * 60)
    print(f"\n완료!")
    print(f"  ETF: {stats['generated']}/{stats['total_etfs']}개 처리")
    print(f"  섹터 클러스터: {stats['clusters']}건 생성")

    return stats


def show_summary(db):
    """생성된 데이터 요약 표시"""
    print("\n" + "=" * 60)
    print("ETF별 섹터 클러스터 요약")
    print("=" * 60)

    # ETF별 섹터 클러스터 조회
    results = db.query(
        ETF.name,
        ETFSectorCluster.group_code,
        ETFSectorCluster.group_name,
        ETFSectorCluster.weight_pct,
        ETFSectorCluster.stock_count
    ).join(
        ETFSectorCluster, ETF.id == ETFSectorCluster.etf_id
    ).order_by(
        ETF.name, ETFSectorCluster.weight_pct.desc()
    ).all()

    current_etf = None
    for r in results:
        if r.name != current_etf:
            print(f"\n[{r.name}]")
            current_etf = r.name
        print(f"  {r.group_code}: {r.weight_pct:.1f}% ({r.stock_count}종목)")


def main():
    print("=" * 60)
    print("ETF 섹터 클러스터 자동 생성")
    print("=" * 60)
    print("\n로직: etf_compositions + company_info.industry_group 집계")

    db = SessionLocal()

    try:
        # 섹터 클러스터 생성
        stats = generate_all_clusters(db)

        # 요약 표시
        show_summary(db)

    finally:
        db.close()


if __name__ == "__main__":
    main()
