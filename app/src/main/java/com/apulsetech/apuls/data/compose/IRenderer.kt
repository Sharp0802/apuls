package com.apulsetech.apuls.data.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apulsetech.apuls.command.IConstraint

interface IRenderer<T> {
    @Composable
    fun Render(
        value: T,
        constraints: Array<IConstraint>,
        onValueChanged: (T) -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
    )
}
