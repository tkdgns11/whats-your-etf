package com.d102.wye.presentation.home.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.News
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<NewsListData>>(UiState.Loading)
    val uiState: StateFlow<UiState<NewsListData>> = _uiState.asStateFlow()

    private var selectedCategoryCode: String? = null

    init {
        loadNews()
    }

    /** 선택한 카테고리로 뉴스 목록을 다시 불러온다. */
    fun onCategorySelected(categoryCode: String?) {
        if (selectedCategoryCode == categoryCode) return
        selectedCategoryCode = categoryCode
        loadNews()
    }

    /** 현재 선택된 카테고리 기준으로 목록을 새로고침한다. */
    fun refresh() {
        loadNews()
    }

    /** Repository에서 뉴스 목록을 받아 화면 상태로 변환한다. */
    private fun loadNews() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            when (val result = newsRepository.getNewsList(category = selectedCategoryCode)) {
                is BaseResult.Success -> {
                    _uiState.update {
                        UiState.Success(
                            NewsListData(
                                categories = newsCategories,
                                selectedCategoryCode = selectedCategoryCode,
                                newsList = result.data.map { it.toUiModel() }
                            )
                        )
                    }
                }
                is BaseResult.Error -> {
                    _uiState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }

    /** 도메인 뉴스 모델을 목록 화면용 UI 모델로 변환한다. */
    private fun News.toUiModel() = NewsListItemUiModel(
        id = id,
        category = categoryName,
        title = title,
        timeAgo = publishedAt.toTimeAgo(),
        source = source,
        thumbnailUrl = thumbnailUrl
    )

    /** 서버 시간을 "n분 전" 형식으로 변환한다. */
    private fun String.toTimeAgo(): String {
        return runCatching {
            val publishedAt = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
            val duration = Duration.between(publishedAt, LocalDateTime.now())
            when {
                duration.toMinutes() < 1 -> "방금 전"
                duration.toHours() < 1 -> "${duration.toMinutes()}분 전"
                duration.toDays() < 1 -> "${duration.toHours()}시간 전"
                duration.toDays() < 7 -> "${duration.toDays()}일 전"
                else -> publishedAt.toLocalDate().toString()
            }
        }.getOrDefault(this)
    }
}

data class NewsListData(
    val categories: List<NewsCategoryUiModel>,
    val selectedCategoryCode: String?,
    val newsList: List<NewsListItemUiModel>
)

data class NewsCategoryUiModel(
    val code: String?,
    val label: String
)

data class NewsListItemUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val timeAgo: String,
    val source: String,
    val thumbnailUrl: String? = null
)

private val newsCategories = listOf(
    NewsCategoryUiModel(code = null, label = "전체"),
    NewsCategoryUiModel(code = "NEWS_MARKET", label = "시장/경제"),
    NewsCategoryUiModel(code = "NEWS_SEMI", label = "반도체"),
    NewsCategoryUiModel(code = "NEWS_IT", label = "IT/전자"),
    NewsCategoryUiModel(code = "NEWS_BIO", label = "바이오/의약"),
    NewsCategoryUiModel(code = "NEWS_AUTO", label = "자동차"),
    NewsCategoryUiModel(code = "NEWS_CHEM", label = "화학/소재"),
    NewsCategoryUiModel(code = "NEWS_ENERGY", label = "에너지"),
    NewsCategoryUiModel(code = "NEWS_FINANCE", label = "금융"),
    NewsCategoryUiModel(code = "NEWS_CONSTRUCT", label = "건설/부동산"),
    NewsCategoryUiModel(code = "NEWS_CONSUMER", label = "소비재"),
    NewsCategoryUiModel(code = "NEWS_TELECOM", label = "통신/미디어"),
    NewsCategoryUiModel(code = "NEWS_TRANSPORT", label = "운송/물류"),
    NewsCategoryUiModel(code = "NEWS_INDUSTRY", label = "산업재"),
    NewsCategoryUiModel(code = "NEWS_ETC", label = "기타")
)
