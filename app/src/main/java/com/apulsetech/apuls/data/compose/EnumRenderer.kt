package com.apulsetech.apuls.data.compose

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.enums.EnumEntries

open class EnumRenderer<T : Enum<T>>(val entries: EnumEntries<T>) : IRenderer<T> {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Render(
        value: T,
        onValueChanged: (T) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = value.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fruit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(
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
                        }
                    )
                }
            }
        }
    }
}
