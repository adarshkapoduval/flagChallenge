package com.adarsh.flag.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.adarsh.flag.ui.theme.Dimens

@Composable
fun WarningAlert(
    title: String ,
    message: String,
    buttonText: String = "Got it",
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.PaddingMedium),
            shape = RoundedCornerShape(Dimens.CardCornerRadiusLarge),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.CardElevationMedium),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimens.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSizeExtraLarge),
                    tint = Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                // Title
                Text(
                    text = title,
                    fontSize = Dimens.TextSizeLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(Dimens.SpacerSmall))

                // Message
                Text(
                    text = message,
                    fontSize = Dimens.TextSizeSmall,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

                // Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.ButtonHeight),
                    shape = RoundedCornerShape(Dimens.CardCornerRadiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = Dimens.TextSizeMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}