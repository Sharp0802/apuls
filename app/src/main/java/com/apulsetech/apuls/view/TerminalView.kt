package com.apulsetech.apuls.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.collection.ObservableRingBuffer
import com.apulsetech.apuls.viewmodel.DeviceCommViewModel

@Composable
fun TerminalView(
    state: com.apulsetech.apuls.viewmodel.UiState,
    input: String,
    logs: ObservableRingBuffer<ConsoleLine>,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        val colorScheme = colorScheme

        ConsoleView(
            lines = logs,
            mapColor = {
                when (it) {
                    DeviceCommViewModel.TX -> colorScheme.primary
                    DeviceCommViewModel.RX -> colorScheme.secondary
                    DeviceCommViewModel.ERR -> colorScheme.error
                    else -> error("unreachable!")
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = if (state.connected) input else "Not Connected...",
                enabled = state.connected,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            val enabled = state.connected && input.isNotEmpty()

            val background = if (enabled) {
                buttonColors().containerColor
            } else {
                buttonColors().disabledContainerColor
            }

            val foreground = if (enabled) {
                buttonColors().contentColor
            } else {
                buttonColors().disabledContentColor
            }

            val height = TextFieldDefaults.MinHeight

            IconButton(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier
                    .background(background, RoundedCornerShape(4.dp))
                    .size(height),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = foreground)
            }
        }
    }
}
