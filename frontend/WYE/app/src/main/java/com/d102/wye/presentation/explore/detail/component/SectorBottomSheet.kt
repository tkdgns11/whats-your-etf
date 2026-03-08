package com.d102.wye.presentation.explore.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.EtfSector
import com.d102.wye.domain.model.SectorStock
import com.d102.wye.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorBottomSheet(
    sector: EtfSector,
    onDismiss: () -> Unit,
    onStockClick: (String) -> Unit = {},
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Background) {
        SectorBottomSheetContent(sector = sector, onStockClick = onStockClick)
    }
}

@Composable
fun SectorBottomSheetContent(
    sector: EtfSector,
    onStockClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("${sector.name} 산업", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        HorizontalDivider(color = Divider)
        Text("주요 구성 종목", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        sector.stocks.forEach { stock ->
            StockProgressItem(
                stock = stock,
                onClick = if (stock.ticker.isNotBlank()) { { onStockClick(stock.ticker) } } else null,
            )
        }
        if (sector.aiAnalysis.isNotBlank()) AiAnalysisBox(sector.aiAnalysis)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StockProgressItem(stock: SectorStock, onClick: (() -> Unit)? = null) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceVariant),
                ) {
                    Text(stock.name.take(1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
                Text(stock.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            Text("${"%.1f".format(stock.percentage)}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
        }
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(SurfaceVariant)) {
            Box(modifier = Modifier.fillMaxWidth((stock.percentage / 100.0).toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(PrimaryGreen))
        }
    }
}

@Composable
private fun AiAnalysisBox(analysis: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceVariant).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("?", fontSize = 16.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)
        Column {
            Text("AI 분석 결과: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
            Text(analysis, fontSize = 13.sp, color = TextSecondary, lineHeight = 20.sp)
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SectorBottomSheetPreview() {
    SectorBottomSheetContent(
        sector = EtfSector(
            name = "반도체", percentage = 28.4,
            stocks = listOf(
                SectorStock("삼성전자", 25.0, "005930"), SectorStock("SK하이닉스", 15.2, "000660"),
                SectorStock("LG에너지솔루션", 8.4), SectorStock("삼성바이오로직스", 5.8), SectorStock("현대차", 4.2),
            ),
            aiAnalysis = "반도체 섹터의 높은 기여도로 인해 IT 업황 회복 시 강한 반등이 예상됩니다.",
        )
    )
}
