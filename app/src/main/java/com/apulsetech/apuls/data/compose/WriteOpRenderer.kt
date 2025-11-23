package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.command.IConstraint
import com.apulsetech.apuls.data.WriteOp

class WriteOpRenderer(override val singleLine: Boolean = false) : Renderer<WriteOp>() {
    @Composable
    override fun TypedRender(
        value: WriteOp,
        constraints: Array<IConstraint>,
        onValueChanged: (WriteOp) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Row(modifier) {
            OutlinedTextField(
                value = value.bank.toString(),
                onValueChange = {
                    val v = it.toUIntOrNull()
                    if (v != null) {
                        onValueChanged(value.copy(bank = v))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = value.bitPtr.toString(),
                onValueChange = {
                    val v = it.toUIntOrNull()
                    if (v != null) {
                        onValueChanged(value.copy(bitPtr = v))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = value.data.toString(),
                onValueChange = {
                    val v = it.toUIntOrNull()
                    if (v != null) {
                        onValueChanged(value.copy(data = v))
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
