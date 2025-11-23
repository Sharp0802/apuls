package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.data.Ip

class IpRenderer : IRenderer<Ip> {
    @Composable
    override fun Render(
        value: Ip,
        onValueChanged: (Ip) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Row(modifier) {
            (0..3).forEach { index ->
                if (index != 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(".")
                    Spacer(Modifier.width(4.dp))
                }

                OutlinedTextField(
                    value = value[index].toString(),
                    onValueChange = {
                        val segment = it.toUByteOrNull()
                        if (segment != null) {
                            val newValue = when (index) {
                                0 -> value.copy(b0 = segment)
                                1 -> value.copy(b1 = segment)
                                2 -> value.copy(b2 = segment)
                                else -> value.copy(b3 = segment)
                            }
                            onValueChanged(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = enabled,
                    modifier = Modifier.width(72.dp)
                )
            }
        }
    }
}
