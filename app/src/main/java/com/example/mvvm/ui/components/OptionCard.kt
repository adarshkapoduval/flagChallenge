package com.example.mvvm.ui.components

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
import androidx.compose.ui.unit.dp

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
        .height(56.dp)
        .then(if (borderColor != null) Modifier.border(2.dp, borderColor, RoundedCornerShape(8.dp)) else Modifier)
        .background(bg, RoundedCornerShape(8.dp))

    Surface(modifier = finalModifier
        .padding(vertical = 4.dp)
        .then(if (!isDisabled) Modifier.clickable { onClick() } else Modifier)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
            Text(text = text)
        }
    }
}
