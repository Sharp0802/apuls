package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.text.KeyboardOptions
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

class IntRenderer(override val singleLine: Boolean = true) : Renderer<Int>() {
    data class State(
        val value: Int? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    private val state = _state.asStateFlow()

    @Composable
    override fun TypedRender(
        value: Int,
        constraints: Array<IConstraint>,
        onValueChanged: (Int) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        val state by state.collectAsState()

        val isError = state.error != null

        if (state.value == null) {
            _state.value = _state.value.copy(value = value)
        }

        TextField(
            value = state.value.toString(),
            onValueChange = {
                val v = it.toIntOrNull()
                if (v != null) {
                    _state.value = _state.value.copy(value = v)

                    var noError = true
                    for (constraint in constraints) {
                        if (constraint.validate(v))
                            continue

                        _state.value = _state.value.copy(error = constraint.toString())
                        noError = false
                    }
                    if (noError) {
                        _state.value = _state.value.copy(error = null)
                        onValueChanged(v)
                    }
                } else {
                    _state.value = _state.value.copy(error = "Invalid number format")
                }
            },
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = state.error ?: "",
                        modifier = Modifier.semantics {
                            error(state.error ?: "")
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}
