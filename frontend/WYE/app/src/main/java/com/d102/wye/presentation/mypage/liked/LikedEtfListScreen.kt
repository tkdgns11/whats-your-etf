package com.d102.wye.presentation.mypage.liked

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.EtfListItem
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun LikedEtfListScreen(
    onBackClick: () -> Unit,
    onEtfClick: (ticker: String) -> Unit,
    viewModel: LikedEtfListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState as UiState.Error).message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            WyeTopBar(
                title = "관심있는 ETF",
                onBackClick = onBackClick
            )

            LikedEtfSortRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (val state = uiState) {
                is UiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is UiState.Success -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.data.likedEtfs, key = { it.ticker }) { etf ->
                        EtfListItem(
                            name = etf.name,
                            ticker = etf.ticker,
                            currentPrice = etf.currentPrice,
                            changeRate = etf.changeRate,
                            changeAmount = etf.changeAmount,
                            riskLevel = etf.riskLevel,
                            isLiked = etf.isLiked,
                            onLikeToggled = { viewModel.onLikeToggled(etf.ticker) },
                            onClick = { onEtfClick(etf.ticker) }
                        )
                    }
                }

                is UiState.Error -> Unit
                UiState.Idle -> Unit
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun LikedEtfSortRow(modifier: Modifier = Modifier) {
    val sortOptions = listOf("거래량 순", "등락률 순", "시가총액 순")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(sortOptions[0]) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .background(SurfaceVariant, RoundedCornerShape(20.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    text = selected,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextSecondary
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(16.dp)
            ) {
                sortOptions.forEach { option ->
                    val isSelected = selected == option
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (isSelected) PrimaryGreen else TextPrimary
                            )
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            selected = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
