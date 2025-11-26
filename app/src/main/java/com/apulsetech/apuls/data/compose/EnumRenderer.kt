package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.command.IConstraint
import kotlin.enums.EnumEntries

open class EnumRenderer<T : Enum<T>>(
    val entries: EnumEntries<T>,
    override val singleLine: Boolean = true
) : Renderer<T>() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TypedRender(
        value: T,
        constraints: Array<IConstraint>,
        onValueChanged: (T) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = {},
                readOnly = true,
                label = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = enabled,
                singleLine = true,
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = modifier
                    .heightIn(min = 44.dp)
                    .menuAnchor(
                        if (enabled) {
                            ExposedDropdownMenuAnchorType.PrimaryEditable
                        } else {
                            ExposedDropdownMenuAnchorType.PrimaryNotEditable
                        },
                        enabled
                    )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(entry.toString()) },
                        onClick = {
                            expanded = false
                            onValueChanged(entry)
                        },
                        enabled = enabled
                    )
                }
            }
        }
    }
}
