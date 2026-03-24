package com.d102.wye.presentation.simulation.entry

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.d102.wye.R
import com.d102.wye.domain.model.BundleType
import com.d102.wye.presentation.theme.IconBackGroundBlue
import com.d102.wye.presentation.theme.IconBackGroundGray
import com.d102.wye.presentation.theme.IconBackGroundGreen
import com.d102.wye.presentation.theme.IconBackGroundOrange
import com.d102.wye.presentation.theme.IconBackGroundPurple
import com.d102.wye.presentation.theme.IconBackGroundRed

@DrawableRes
fun BundleType.toDrawable(): Int = when (this) {
    BundleType.STABLE_INCOME -> R.drawable.ic_bundle_crown
    BundleType.HIGH_GROWTH -> R.drawable.ic_bundle_high_growth
    BundleType.BALANCED_PORTFOLIO -> R.drawable.ic_bundle_balance
    BundleType.AGGRESSIVE_PLAY -> R.drawable.ic_bundle_aggressive
    BundleType.LONG_TERM_INVESTING -> R.drawable.ic_bundle_long_term
    BundleType.UNKNOWN -> R.drawable.ic_bundle_else
}

fun BundleType.toBackgroundColor(): Color = when (this) {
    BundleType.STABLE_INCOME -> IconBackGroundOrange
    BundleType.HIGH_GROWTH -> IconBackGroundBlue
    BundleType.BALANCED_PORTFOLIO -> IconBackGroundGreen
    BundleType.AGGRESSIVE_PLAY -> IconBackGroundRed
    BundleType.LONG_TERM_INVESTING -> IconBackGroundPurple
    BundleType.UNKNOWN -> IconBackGroundGray
}
