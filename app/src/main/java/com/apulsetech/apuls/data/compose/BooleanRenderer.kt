package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import com.apulsetech.apuls.command.IConstraint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BooleanRenderer(override val singleLine: Boolean = true) : Renderer<Boolean>() {
    @Composable
    override fun TypedRender(
        value: Boolean,
        constraints: Array<IConstraint>,
        onValueChanged: (Boolean) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        Switch(
            checked = value,
            onCheckedChange = onValueChanged,
            enabled = enabled,
            modifier = modifier
        )
    }
}
