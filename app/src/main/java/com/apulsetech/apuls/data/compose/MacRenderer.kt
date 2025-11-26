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
import com.apulsetech.apuls.command.IConstraint
import com.apulsetech.apuls.data.Mac

class MacRenderer(override val singleLine: Boolean = false) : Renderer<Mac>() {
    @Composable
    override fun TypedRender(
        value: Mac,
        constraints: Array<IConstraint>,
        onValueChanged: (Mac) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Row(modifier) {
            (0..5).forEach { index ->
                if (index != 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(":")
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
                                3 -> value.copy(b3 = segment)
                                4 -> value.copy(b4 = segment)
                                else -> value.copy(b5 = segment)
                            }
                            onValueChanged(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Decimal),
                    singleLine = true,
                    enabled = enabled,
                    modifier = Modifier.width(72.dp)
                )
            }
        }
    }
}
