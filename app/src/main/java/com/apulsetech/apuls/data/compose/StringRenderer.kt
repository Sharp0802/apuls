package com.apulsetech.apuls.data.compose

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.command.IConstraint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StringRenderer(override val singleLine: Boolean = true) : Renderer<String>() {
    data class State(
        val value: String? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    private val state = _state.asStateFlow()

    @Composable
    override fun TypedRender(
        value: String,
        constraints: Array<IConstraint>,
        onValueChanged: (String) -> Unit,
        enabled: Boolean,
        modifier: Modifier
    ) {
        val state by state.collectAsState()

        val isError = state.error != null

        if (state.value == null) {
            _state.value = _state.value.copy(value = value)
        }

        OutlinedTextField(
            value = state.value ?: "",
            onValueChange = {
                _state.value = _state.value.copy(value = it)

                var noError = true
                for (constraint in constraints) {
                    if (constraint.validate(it))
                        continue

                    _state.value = _state.value.copy(error = constraint.toString())
                    noError = false
                }
                if (noError) {
                    _state.value = _state.value.copy(error = null)
                    onValueChanged(it)
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
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = modifier.heightIn(min = 44.dp)
        )
    }
}
