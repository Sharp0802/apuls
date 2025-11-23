package com.apulsetech.apuls.views

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow

@Stable
@Immutable
data class ConsoleLine(
    val text: String,
    val type: Int
)

@Composable
fun ConsoleView(lines: List<ConsoleLine>, mapColor: (Int) -> Color?, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            state.animateScrollToItem(lines.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        state = state
    ) {
        itemsIndexed(lines) { _, line ->
            Text(
                text = line.text,
                color = mapColor(line.type) ?: LocalContentColor.current,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}
