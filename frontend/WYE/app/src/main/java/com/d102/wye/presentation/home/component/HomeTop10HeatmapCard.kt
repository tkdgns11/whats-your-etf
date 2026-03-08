package com.d102.wye.presentation.home.component

import androidx.compose.runtime.Composable
import com.d102.wye.presentation.designsystem.EtfHeatmapGrid
import com.d102.wye.presentation.home.Top10EtfUiModel

@Composable
fun HomeTop10HeatmapCard(
    top10Etfs: List<Top10EtfUiModel>,
    onEtfClick: (ticker: String) -> Unit
) {
    EtfHeatmapGrid(
        items = top10Etfs.map { it.name to it.changeRate },
        onCardClick = { etfName ->
            val ticker = top10Etfs.firstOrNull { it.name == etfName }?.ticker ?: return@EtfHeatmapGrid
            onEtfClick(ticker)
        }
    )
}
