package com.apulsetech.apuls.data.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.apulsetech.apuls.command.IConstraint

abstract class Renderer<T> {
    abstract val singleLine: Boolean

    @Composable
    abstract fun TypedRender(
        value: T,
        constraints: Array<IConstraint>,
        onValueChanged: (T) -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
    )

    @Composable
    fun Render(
        value: Any,
        constraints: Array<IConstraint>,
        onValueChanged: (Any) -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
    ) {
        @Suppress("UNCHECKED_CAST")
        TypedRender(value as T, constraints, { onValueChanged(it as Any) }, enabled, modifier)
    }
}
