package com.adarsh.flag.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.adarsh.flag.ui.theme.Dimens

@Composable
fun OptionCard(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isDisabled: Boolean = false,
    borderColor: Color? = null,
    onClick: () -> Unit = {}
) {
    val bg = when {
        isSelected -> Color(0xFFE3F2FD)
        else -> Color.White
    }
    val finalModifier = modifier
        .fillMaxWidth()
        .height(Dimens.OptionCardHeight)
        .then(if (borderColor != null) Modifier.border(Dimens.BorderWidth, borderColor, RoundedCornerShape(Dimens.CardCornerRadiusSmall)) else Modifier)
        .background(bg, RoundedCornerShape(Dimens.CardCornerRadiusSmall))

    Surface(modifier = finalModifier
        .padding(vertical = Dimens.PaddingExtraSmall)
        .then(if (!isDisabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.PaddingMedium), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
            Text(text = text)
        }
    }
}
