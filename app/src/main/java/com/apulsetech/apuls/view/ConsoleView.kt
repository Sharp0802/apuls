package com.apulsetech.apuls.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
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
import com.apulsetech.apuls.collection.ObservableRingBuffer

@Stable
@Immutable
data class ConsoleLine(
    val text: String,
    val type: Int
)

@Composable
fun ConsoleView(
    lines: ObservableRingBuffer<ConsoleLine>,
    mapColor: (Int) -> Color?,
    modifier: Modifier = Modifier
) {
    val version = lines.version
    val size = lines.size
    val state = rememberLazyListState()

    LaunchedEffect(version) {
        if (size > 0) {
            state.animateScrollToItem(size - 1)
        }
    }

    LazyColumn(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        state = state
    ) {
        items(
            count = size,
            key = { index -> lines.keyFor(index) }
        ) { index ->
            val line = lines[index]
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
