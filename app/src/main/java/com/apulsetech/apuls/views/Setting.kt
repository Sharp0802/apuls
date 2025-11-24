package com.apulsetech.apuls.views

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apulsetech.apuls.command.Command
import com.apulsetech.apuls.command.ParameterizedCommandDeclaration
import com.apulsetech.apuls.data.compose.Renderer
import com.apulsetech.apuls.data.compose.Renderers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    val comm: DeviceCommViewModel,
    val declaration: ParameterizedCommandDeclaration,
    val renderer: Renderer<*> = Renderers.of(declaration.type)
) : ViewModel() {
    data class State(
        val value: Any? = null
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var loadJob: Job? = null

    fun update(value: Any) {
        _state.value = _state.value.copy(value = value)
    }

    fun load() {
        if (loadJob != null)
            return

        loadJob = viewModelScope.launch(Dispatchers.IO) {
            var callback: ((Command) -> Unit)? = null
            callback = {
                if (it.declaration.name == declaration.name) {
                    _state.value = _state.value.copy(value = it.state)
                    comm.callbacks.remove(callback)
                }
            }
            comm.callbacks.add(callback)
            comm.send(declaration.getter())
        }
    }

    fun apply() {
        if (state.value.value == null) {
            Log.w("SettingViewModel", "Attempted to apply value before field initialized")
            return
        }

        if (declaration.readonly) {
            Log.w(
                "SettingViewModel",
                "Attempted to apply value for readonly field '${declaration.name}'"
            )
            return
        }

        val command = declaration.setter(state.value.value!!)
        comm.send(command)
    }
}

@Composable
fun Setting(vm: SettingViewModel, modifier: Modifier = Modifier) {
    vm.load()

    if (vm.renderer.singleLine) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = vm.declaration.label,
                modifier = Modifier.width(196.dp)
            )
            Spacer(Modifier.width(8.dp))
            SettingContent(vm, Modifier.weight(1f))
        }
    } else {
        Column(modifier) {
            Text(vm.declaration.label)
            Spacer(Modifier.height(8.dp))
            SettingContent(vm, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SettingContent(vm: SettingViewModel, modifier: Modifier) {
    val state by vm.state.collectAsState()

    if (state.value == null) {
        LinearProgressIndicator(modifier)
    } else {
        vm.renderer.Render(
            value = state.value!!,
            vm.declaration.constraints,
            onValueChanged = {
                vm.update(it)
            },
            enabled = !vm.declaration.readonly,
            modifier = modifier
        )
    }
}
