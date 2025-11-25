package com.apulsetech.apuls.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls.collection.ObservableRingBuffer
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.device.DeviceSession
import com.apulsetech.apuls.device.DeviceSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

object Line {
    const val TX = 0
    const val RX = 1
    const val ERR = 2
}

data class UiState(
    val indicator: String = "", val connected: Boolean = false, val loading: Boolean = false
)

class Session(
    socket: DeviceSocket, scope: CoroutineScope, val onClosed: suspend () -> Unit
) : DeviceSession(socket, scope) {
    val channel = Channel<ConsoleLine>(capacity = 4096)

    override suspend fun onReceived(line: String) {
        channel.send(ConsoleLine(line, Line.RX))
    }

    override suspend fun onClosed() {
        onClosed()
    }
}

class DeviceCommViewModel(device: Device) : ViewModel() {
    companion object {
        private const val LOGS_MAX_LINE = 256
        private const val LOGS_BATCH = 8
        private const val LOGS_BATCH_TIMEOUT = 100L
    }

    val logs = ObservableRingBuffer<ConsoleLine>(LOGS_MAX_LINE)

    var state by mutableStateOf(UiState())
        private set

    var input by mutableStateOf("")

    private var session: Session? = null
    private var sessionJob = viewModelScope.launch(Dispatchers.Main) {
        state = UiState("Connecting...", loading = true)

        val socket = try {
            withContext(Dispatchers.IO) {
                device.open()
            }
        } catch (e: Throwable) {
            state = UiState(e.message ?: "Couldn't open device")
            return@launch
        }

        val session = Session(socket, viewModelScope) {
            viewModelScope.launch {
                state = UiState("Disconnected")
            }
        }
        this@DeviceCommViewModel.session = session

        state = UiState("Connected", connected = true)

        val buffer = ArrayList<ConsoleLine>(LOGS_BATCH)

        fun flush() {
            buffer.forEach {
                logs.write(it)
            }
            buffer.clear()
        }

        while (isActive) {
            val line = withTimeoutOrNull(LOGS_BATCH_TIMEOUT) {
                session.channel.receive()
            }
            if (line == null) {
                flush()
                continue
            }

            buffer.add(line)
            if (buffer.size >= LOGS_BATCH) {
                flush()
            }
        }

        session.close()
        this@DeviceCommViewModel.session = null
    }

    fun send(line: String) {
        val session = session ?: return

        session.send(line)
        // send should called in main context
        logs.write(ConsoleLine(line, Line.TX))
    }

    override fun onCleared() {
        super.onCleared()

        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            sessionJob.cancelAndJoin()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCommView(device: Device) {
    val nav = rememberNavController()
    var title by rememberSaveable { mutableStateOf("") }

    val vm: DeviceCommViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeviceCommViewModel(device) as T
            }
        }
    )

    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = vm.state.indicator,
                    color = if (vm.state.connected) colorScheme.primary else colorScheme.error
                )
            }
        })
    }, bottomBar = {
        BottomAppBar(actions = {
            IconButton(onClick = { nav.navigate("settings") }) {
                Icon(Icons.Rounded.Settings, "Settings")
            }
            IconButton(onClick = { nav.navigate("terminal") }) {
                Icon(Icons.Rounded.Terminal, "Terminal")
            }
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate("inventory") },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Rounded.CellTower, "Inventory")
            }
        })
    }) { inner ->
        Column(modifier = Modifier.padding(inner)) {
            val padding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)

            NavHost(nav, startDestination = "inventory") {
                composable("settings") {
                    title = "Settings"
                    BackHandler(true) { }
                }

                composable("terminal") {
                    title = "Terminal"
                    BackHandler(true) { }

                    DeviceCommContent(
                        state = vm.state,
                        input = vm.input,
                        logs = vm.logs,
                        onInputChanged = {
                            vm.input = it
                        },
                        onSubmit = {
                            vm.send(vm.input)
                            vm.input = ""
                        },
                        modifier = Modifier.padding(padding).fillMaxSize()
                    )
                }

                composable("inventory") {
                    title = "Inventory"
                    BackHandler(true) { }
                }
            }
        }
    }
}


@Composable
fun DeviceCommContent(
    state: UiState,
    input: String,
    logs: ObservableRingBuffer<ConsoleLine>,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        val colorScheme = colorScheme

        ConsoleView(
            lines = logs,
            mapColor = {
                when (it) {
                    Line.TX -> colorScheme.primary
                    Line.RX -> colorScheme.secondary
                    Line.ERR -> colorScheme.error
                    else -> error("unreachable!")
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = if (state.connected) input else "Not Connected...",
                enabled = state.connected,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            val enabled = state.connected && input.isNotEmpty()

            val background = if (enabled) {
                buttonColors().containerColor
            } else {
                buttonColors().disabledContainerColor
            }

            val foreground = if (enabled) {
                buttonColors().contentColor
            } else {
                buttonColors().disabledContentColor
            }

            val height = TextFieldDefaults.MinHeight

            IconButton(
                onClick = onSubmit,
                enabled = enabled,
                modifier = Modifier
                    .background(background, RoundedCornerShape(4.dp))
                    .size(height),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = foreground)
            }
        }
    }
}

