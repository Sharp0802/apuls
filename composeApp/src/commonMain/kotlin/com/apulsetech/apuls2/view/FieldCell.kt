package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@Composable
fun FieldCell(name: String, value: Any?) {
    val density = LocalDensity.current
    val defaultSize = with(density) { typography.labelMedium.fontSize.toDp() }

    Card {
        Row(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, style = typography.labelMedium)

            VerticalDivider(Modifier.height(defaultSize))

            val base = LocalTextStyle.current.color

            Text(
                text = value?.toString() ?: "NR",
                fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                color = if (value == null) base.copy(alpha = 0.4f) else base,
                style = typography.labelMedium
            )
        }
    }
}
