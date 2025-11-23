package com.apulsetech.apuls.data.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface IRenderer<T> {
    @Composable
    fun Render(
        value: T,
        onValueChanged: (T) -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
    )
}
