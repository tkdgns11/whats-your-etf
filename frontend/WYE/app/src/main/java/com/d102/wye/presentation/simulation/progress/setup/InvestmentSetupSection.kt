package com.d102.wye.presentation.simulation.progress.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.components.InvestmentGuideText
import com.d102.wye.presentation.simulation.progress.components.InvestmentInputField
import com.d102.wye.presentation.simulation.progress.components.InvestmentTypeToggle
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.SurfaceCard
import com.d102.wye.presentation.theme.SurfaceVariant


@Composable
fun InvestmentSetupSection(
    formState: SimulationFormState,
    onInvestmentTypeSelected: (InvestmentType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onPeriodChanged: (String) -> Unit
) {
    Column(modifier = Modifier.background(BackGroundLightGreen2).padding(20.dp)) {
        Text(text = "투자 설정", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // 적립형 / 거치형 칩 선택
        InvestmentTypeToggle(
            selectedType = formState.investmentType,
            onTypeSelected = onInvestmentTypeSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            InvestmentInputField(
                label = "투자 금액(원)",
                value = formState.investmentAmount,
                onValueChange = onAmountChanged,
                placeholder = "금액 입력",
                modifier = Modifier.weight(1f)
            )

            InvestmentInputField(
                label = "투자 기간(월)",
                value = formState.investmentPeriod,
                onValueChange = onPeriodChanged,
                placeholder = "최대 36개월",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!formState.investmentAmount.isBlank() && !formState.investmentPeriod.isBlank()){
            WyeCard(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SurfaceVariant),
                containerColor = SurfaceCard, elevation = 0.dp
            ) {

                InvestmentGuideText(
                    type = formState.investmentType,
                    amountStr = formState.investmentAmount,
                    periodStr = formState.investmentPeriod
                )
            }
        }
    }
}