package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apulsetech.apuls2.data.Tag
import java.io.Serializable

data class TagViewOptions(
    val pc: Boolean = false,
    val ant: Boolean = false,
    val rssi: Boolean = false,
    val rid: Boolean = false,
    val freq: Boolean = false,
    val ip: Boolean = false,
    val date: Boolean = false,
    val cs: Boolean = false
) : Serializable

@Composable
fun TagView(
    tag: Tag,
    count: Int,
    tagViewOptions: TagViewOptions,
    multiBaseOptions: MultiBaseOptions
) {
    var letterSpacing by remember(count) { mutableStateOf(1f) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = count.toString(),
            style = typography.titleMedium,
            modifier = Modifier.width(32.dp).padding(4.dp),
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
            letterSpacing = letterSpacing.sp,
            onTextLayout = { result ->
                if (result.lineCount > 0) {
                    val available = result.layoutInput.constraints.maxWidth.toFloat()
                    val lineWidth = result.getLineRight(0) - result.getLineLeft(0)

                    if (available > 0 && lineWidth > available && letterSpacing > 0f) {
                        val ratio = (available / lineWidth).coerceAtMost(1f)
                        val newSpacing = (letterSpacing * ratio * 0.95f).coerceAtLeast(0f)

                        if (newSpacing < letterSpacing - 0.01f) {
                            letterSpacing = newSpacing
                        }
                    }
                }
            }
        )

        VerticalDivider(Modifier.height(24.dp))

        Spacer(Modifier.width(8.dp))

        Column(Modifier.weight(1f)) {
            MultiBaseView(tag.tag, multiBaseOptions, Modifier)

            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (tagViewOptions.ant) {
                    FieldCell("ANT", tag.ant)
                }
                if (tagViewOptions.rssi) {
                    FieldCell("RSSI", tag.rssi)
                }
                if (tagViewOptions.rid) {
                    FieldCell("RID", tag.rid)
                }
                if (tagViewOptions.freq) {
                    FieldCell("Freq", tag.freq)
                }
                if (tagViewOptions.ip) {
                    FieldCell("IP", tag.ip)
                }
                if (tagViewOptions.date) {
                    FieldCell("Date", tag.date)
                }
                if (tagViewOptions.cs) {
                    FieldCell("CS", tag.cs)
                }
            }
        }
    }
}
