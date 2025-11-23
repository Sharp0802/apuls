package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.data.GpioInEvent

class GpioInEventRenderer : IRenderer<GpioInEvent> {
    @Composable
    override fun Render(
        value: GpioInEvent,
        onValueChanged: (GpioInEvent) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Row(modifier) {
            Switch(
                checked = value.enabled,
                onCheckedChange = {
                    onValueChanged(value.copy(enabled = it))
                },
                enabled = enabled
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = value.time.toString(),
                onValueChange = {
                    val v = it.toUIntOrNull()
                    if (v != null) {
                        onValueChanged(value.copy(time = v))
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = value.command,
                onValueChange = {
                    onValueChanged(value.copy(command = it))
                },
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
