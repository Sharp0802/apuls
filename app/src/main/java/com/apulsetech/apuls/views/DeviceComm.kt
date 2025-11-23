package com.apulsetech.apuls.views

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls.command.Command
import com.apulsetech.apuls.data.text.parse
import com.apulsetech.apuls.device.BluetoothDeviceSocket
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.device.DeviceSocket
import com.apulsetech.apuls.device.UsbDeviceSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.ParseException
import java.util.concurrent.CopyOnWriteArrayList

object Line {
    const val TX = 0
    const val RX = 1
    const val ERR = 2
}

data class UiState(
    val state: String = "", val connected: Boolean = false, val loading: Boolean = false
)

enum class ConnectionType {
    Usb,
    Bluetooth,
}

class DeviceCommViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    var input by mutableStateOf("")
        private set

    private val _logs = mutableStateListOf<ConsoleLine>()
    val logs: List<ConsoleLine> get() = _logs

    private var openJob: Job? = null
    private var socket: DeviceSocket? = null

    var callbacks = CopyOnWriteArrayList<(Command) -> Unit>()

    val connectionType: ConnectionType
        get() {
            return when (socket) {
                is BluetoothDeviceSocket -> ConnectionType.Bluetooth
                is UsbDeviceSocket -> ConnectionType.Usb
                else -> error("unreachable!")
            }
        }

    fun open(device: Device) {
        openJob = viewModelScope.launch(Dispatchers.IO) {
            Dispatchers.Main.run {
                _state.value = _state.value.copy(state = "Connecting...", loading = true)
            }

            val socket = try {
                device.open()
            } catch (e: Throwable) {
                Dispatchers.Main.run {
                    _state.value = _state.value.copy(state = e.toString(), loading = false)
                }
                return@launch
            }

            if (socket == null) {
                Dispatchers.Main.run {
                    _state.value = _state.value.copy(state = "Cannot open device", loading = false)
                }
                return@launch
            }

            this@DeviceCommViewModel.socket = socket

            Dispatchers.Main.run {
                _state.value = _state.value.copy(
                    state = "Connected", loading = false, connected = true
                )
            }

            val lineBuffer = mutableListOf<Byte>()
            val readBuffer = ByteArray(8192)

            while (isActive) {
                val read = socket.read(readBuffer)
                if (read == -1) break
                if (read == 0) continue

                for (i in 0 until read) {
                    if (lineBuffer.lastOrNull() == '\r'.code.toByte() && readBuffer[i] == '\n'.code.toByte()) {
                        val line = lineBuffer.subList(0, lineBuffer.lastIndex).toByteArray()
                            .toString(Charsets.UTF_8)
                        Log.i("DeviceLoop", line)
                        onReceived(line)
                        lineBuffer.clear()
                    } else {
                        lineBuffer.add(readBuffer[i])
                    }
                }
            }

            stop()
        }
    }

    private fun onReceived(line: String) {
        _logs.add(ConsoleLine(line, Line.RX))

        val com: Command = try {
            line.parse()
        } catch (e: ParseException) {
            _logs.add(ConsoleLine("error: ${e.message}", Line.ERR))
            return
        }

        for (callback in callbacks) {
            try {
                callback(com)
            } catch (t: Throwable) {
                Log.e("DeviceComm", "Error thrown in callback", t)
                _logs.add(ConsoleLine("error: ${t.message}", Line.ERR))
            }
        }
    }

    fun send(line: String) {
        val socket = socket ?: return

        socket.write((line + "\r\n").toByteArray())
        _logs.add(ConsoleLine(line, Line.TX))
    }

    fun updateInput(input: String) {
        this.input = input
    }

    fun stop() {
        socket?.close()

        Dispatchers.Main.run {
            _state.value = _state.value.copy(
                state = "Disconnected", loading = false, connected = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCommView(
    device: Device, vm: DeviceCommViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val nav = rememberNavController()

    var title by rememberSaveable { mutableStateOf("") }

    vm.open(device)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = state.state,
                            color = if (state.connected) colorScheme.primary else colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Rounded.Settings, "Settings")
                    }
                    IconButton(onClick = { nav.navigate("terminal") }) {
                        Icon(Icons.Rounded.Terminal, "Terminal")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { nav.navigate("inventory") },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Rounded.CellTower, "Inventory")
                    }
                }
            )
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner)) {
            val padding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)

            NavHost(nav, startDestination = "inventory") {
                composable("settings") {
                    @Suppress("AssignedValueIsNeverRead")
                    title = "Settings"
                    BackHandler(true) { }

                    Settings(
                        comm = vm,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }

                composable("terminal") {
                    @Suppress("AssignedValueIsNeverRead")
                    title = "Terminal"
                    BackHandler(true) { }

                    DeviceCommContent(
                        state = state,
                        input = vm.input,
                        logs = vm.logs,
                        onInputChanged = {
                            vm.updateInput(it)
                        },
                        onSubmit = {
                            vm.send(vm.input)
                            vm.updateInput("")
                        },
                        modifier = Modifier.padding(padding)
                    )
                }

                composable("inventory") {
                    @Suppress("AssignedValueIsNeverRead")
                    title = "Inventory"
                    BackHandler(true) { }

                    // TODO
                }
            }
        }
    }
}


@Composable
fun DeviceCommContent(
    state: UiState,
    input: String,
    logs: List<ConsoleLine>,
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
                ButtonDefaults.buttonColors().containerColor
            } else {
                ButtonDefaults.buttonColors().disabledContainerColor
            }

            val foreground = if (enabled) {
                ButtonDefaults.buttonColors().contentColor
            } else {
                ButtonDefaults.buttonColors().disabledContentColor
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

