package com.d102.wye.presentation.simulation.progress.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun PortfolioSection(
    formState: SimulationFormState,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(BackGroundLightGreen2)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "포트폴리오 구성", style = MaterialTheme.typography.titleSmall)

            val totalWeight = formState.portfolioItems.sumOf { it.weight }
            Box(
                modifier = Modifier.background(
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "합계: $totalWeight%",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .requiredWidthIn(min = 78.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (formState.portfolioItems.isEmpty()) {
            DashedContainer(
                modifier = Modifier.clickable { onAddClick() },
                strokeWidth = 2.dp,
                borderColor = Border
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = null,
                        tint = IconInactive
                    )
                    Text(
                        text = "ETF 종목 추가하기",
                        style = MaterialTheme.typography.titleSmall,
                        color = IconInactive
                    )
                }
            }
        } else {
            formState.portfolioItems.forEach { item ->
                // TODO: 포트폴리오 아이템 UI (ticker, name, weight)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}