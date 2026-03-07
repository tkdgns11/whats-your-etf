package com.d102.wye.presentation.navigation

/**
 * 앱 내 모든 화면의 라우트 정의
 *
 * 두 가지 종류:
 * - object : 파라미터 없는 화면 (목록, 설정 등)
 * - data class : 파라미터 있는 화면 (상세 조회 등)
 *
 * 실제 경로 문자열은 route 프로퍼티로 통일해서 사용
 * NavHost/navigate() 양쪽에서 항상 .route로 접근할 것
 */
sealed class Route(val route: String) {

    // ─────────────────────────────────────────
    // Bottom Nav (BottomNavBar가 보이는 화면)
    // ─────────────────────────────────────────

    object Home : Route("home")
    object Explore : Route("explore")

    // ─────────────────────────────────────────
    // 시뮬레이션 화면
    // ─────────────────────────────────────────

    object Simulation : Route("simulation")
    object SimulationEntry : Route("simulation_entry")
    object SimulationAddStock : Route("simulation_add_stock")

    object Strategy : Route("strategy")
    object MyPage : Route("mypage")

    // ─────────────────────────────────────────
    // Auth
    // ─────────────────────────────────────────

    object Login : Route("login")
    object Join : Route("join")
    object PasswordReset : Route("password_reset")

    // ─────────────────────────────────────────
    // ETF 상세 (ticker로 단건 조회)
    // ─────────────────────────────────────────

    data class EtfDetail(val ticker: String) : Route("etf_detail/$ticker") {
        companion object {
            // NavHost 등록용 경로 패턴 (중괄호 포함)
            const val ROUTE_PATTERN = "etf_detail/{ticker}"
            const val ARG_TICKER = "ticker"
        }
    }

    // ─────────────────────────────────────────
    // 뉴스 상세 (id로 단건 조회)
    // ─────────────────────────────────────────

    data class NewsDetail(val newsId: Long) : Route("news_detail/$newsId") {
        companion object {
            const val ROUTE_PATTERN = "news_detail/{newsId}"
            const val ARG_NEWS_ID = "newsId"
        }
    }



    // ─────────────────────────────────────────
    // 나의전략 서브 화면
    // ─────────────────────────────────────────

    data class StrategyDetail(val strategyId: Long) : Route("strategy_detail/$strategyId") {
        companion object {
            const val ROUTE_PATTERN = "strategy_detail/{strategyId}"
            const val ARG_STRATEGY_ID = "strategyId"
        }
    }

    object StrategyCompare : Route("strategy_compare")

    // ─────────────────────────────────────────
    // 알림
    // ─────────────────────────────────────────

    object Notification : Route("notification")
}