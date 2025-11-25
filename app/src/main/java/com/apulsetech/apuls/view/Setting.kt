package com.apulsetech.apuls.view

/*
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
*/