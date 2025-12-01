package com.apulsetech.apuls.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.data.Tag

@Composable
fun TableCell(text: Any?, modifier: Modifier) {
    Text(
        text = text?.toString() ?: "NR",
        fontFamily = FontFamily.Monospace,
        fontStyle = if (text == null) FontStyle.Italic else null,
        color = if (text == null) colorScheme.onSurface.copy(alpha = 0.4f) else Color.Unspecified,
        modifier = modifier
            .border(1.dp, colorScheme.surfaceBright)
            .padding(8.dp)
    )
}

@Composable
fun InventoryView(
    tags: Map<Tag, Int>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier
) {
    val snapshot = tags.entries.sortedByDescending { it.value }

    val modifiers = arrayOf(
        Modifier.width(60.dp),
        Modifier.width(420.dp),
        Modifier.width(60.dp),
        Modifier.width(60.dp),
        Modifier.width(60.dp),
        Modifier.width(100.dp),
        Modifier.width(300.dp),
        Modifier.width(300.dp),
        Modifier.width(100.dp)
    )

    Column(modifier) {
        Box(
            Modifier
                .weight(1f)
                .border(1.dp, colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        ) {
            Column(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(colorScheme.surfaceContainerHigh)
                ) {
                    TableCell("CNT", modifiers[0])
                    TableCell("Tag", modifiers[1])
                    TableCell("ANT", modifiers[2])
                    TableCell("RSSI", modifiers[3])
                    TableCell("RID", modifiers[4])
                    TableCell("Freq", modifiers[5])
                    TableCell("IP", modifiers[6])
                    TableCell("Date", modifiers[7])
                    TableCell("CS", modifiers[8])
                }

                LazyColumn {
                    items(snapshot) {
                        val (tag, cnt) = it
                        Row(Modifier.fillMaxWidth()) {
                            TableCell(cnt, modifiers[0])
                            TableCell(tag.value, modifiers[1])
                            TableCell(tag.ant, modifiers[2])
                            TableCell(tag.rssi, modifiers[3])
                            TableCell(tag.rid, modifiers[4])
                            TableCell(tag.freq, modifiers[5])
                            TableCell(tag.ip, modifiers[6])
                            TableCell(tag.date, modifiers[7])
                            TableCell(tag.cs, modifiers[8])
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row {
            Button(onStart, Modifier.weight(1f)) {
                Text("Start")
            }

            Spacer(Modifier.width(8.dp))

            Button(onStop, Modifier.weight(1f)) {
                Text("Stop")
            }

            Spacer(Modifier.width(8.dp))

            Button(onClear, Modifier.weight(1f)) {
                Text("Clear")
            }
        }
    }
}
