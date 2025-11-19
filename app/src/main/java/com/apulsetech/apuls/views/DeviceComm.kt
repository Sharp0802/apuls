package com.apulsetech.apuls.views

import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.device.DeviceSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MsgDir { RX, TX }

data class ConsoleChunk(
    val dir: MsgDir,
    val text: String
)

data class DeviceCommViewState(
    val status: String = "Idle",
    val chunks: List<ConsoleChunk> = emptyList(),
    val input: String = "",
    val connected: Boolean = false
)

class DeviceCommViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(DeviceCommViewState())
    internal val state: StateFlow<DeviceCommViewState> = _state

    private var _socket: DeviceSocket? = null
    private var readJob: Job? = null

    private var rxPending: String = ""
    private var txPending: String = ""
    private var txFlushJob: Job? = null
    private val txFlushDelayMs = 200L

    fun connect(device: Device) {
        if (readJob != null) return

        _state.value = _state.value.copy(
            status = "Connecting...",
            connected = false
        )

        readJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val socket = device.open()
                if (socket == null) {
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            status = "No permission to open device",
                            connected = false
                        )
                    }

                    return@launch
                }

                _socket = socket

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        status = "Connected",
                        connected = true
                    )
                }

                val buffer = ByteArray(1024)

                while (isActive) {
                    val len = _socket!!.read(buffer)
                    if (len == -1) break

                    val chunk = buffer.copyOf(len).decodeToString()

                    chunk.forEach { c -> when (c) {
                        '\r' -> {
                            // ignore
                        }

                        '\n' -> {
                            // flush line
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.let { current ->
                                    current.copy(
                                        chunks = current.chunks + ConsoleChunk(
                                            dir = MsgDir.RX,
                                            text = rxPending
                                        )
                                    )
                                }

                                rxPending = ""
                                flushPendingTx()
                            }
                        }

                        else -> rxPending += c
                    } }
                }

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        status = "End Of Stream",
                        connected = false
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        status = "Error: ${e.message}",
                        connected = false
                    )
                }
            } finally {
                closeSocketInternal()
            }
        }
    }

    fun send(text: String) {
        val s = _socket ?: return
        if (!_state.value.connected) return
        if (text.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = (text + "\r\n").encodeToByteArray()
                s.write(payload)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        status = "Send error: ${e.message}",
                        connected = false
                    )
                }
                stop()
            }
        }

        synchronized(this) {
            txPending += text
        }

        scheduleTxFlushTimeout()

        _state.value = _state.value.copy(input = "")
    }

    private fun flushPendingTx() {
        val text: String
        synchronized(this) {
            if (txPending.isEmpty()) return
            text = txPending
            txPending = ""
        }

        txFlushJob?.cancel()
        txFlushJob = null

        _state.value = _state.value.let { current ->
            current.copy(
                chunks = current.chunks + ConsoleChunk(
                    dir = MsgDir.TX,
                    text = text
                )
            )
        }
    }

    private fun scheduleTxFlushTimeout() {
        if (txFlushJob?.isActive == true) return

        txFlushJob = viewModelScope.launch {
            delay(txFlushDelayMs)
            withContext(Dispatchers.Main) {
                flushPendingTx()
            }
        }
    }


    fun updateInput(newInput: String) {
        _state.value = _state.value.copy(input = newInput)
    }

    fun stop() {
        readJob?.cancel()
        readJob = null
        closeSocketInternal()
        _state.value = _state.value.copy(
            status = "Disconnected",
            connected = false
        )
    }

    private fun closeSocketInternal() {
        try {
            _socket?.close()
        } catch (_: Exception) {
        } finally {
            _socket = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}

data class CommandItem(
    val label: String,
    val command: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceNotSelectedContent(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SPP Communication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No device selected",
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeviceCommView(vm: DeviceSelectViewModel, onBack: () -> Unit) {
    val commVm: DeviceCommViewModel = viewModel()
    val uiState by commVm.state.collectAsState()
    val selectedDevice by vm.selectedDevice.collectAsState()

    LaunchedEffect(selectedDevice) {
        selectedDevice?.let { commVm.connect(it) } ?: commVm.stop()
    }

    DisposableEffect(Unit) {
        onDispose { commVm.stop() }
    }

    if (selectedDevice == null) {
        DeviceNotSelectedContent(onBack = onBack)
        return
    }

    BackHandler(onBack = onBack)

    val commands = remember {
        listOf(
            CommandItem("Inventory", ":inventory"),
            CommandItem("Stop", ":stop"),
            CommandItem("Service serial", ":ser_serial 1"),
            CommandItem("No serial", ":ser_serial 0"),
        )
    }

    DeviceCommContent(
        uiState = uiState,
        onBack = onBack,
        onInputChange = { commVm.updateInput(it) },
        onSendClick = { commVm.send(uiState.input) },
        commands = commands,
        onCommandClick = { cmd ->
            commVm.send(cmd.command)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceCommContent(
    uiState: DeviceCommViewState,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    commands: List<CommandItem>,
    onCommandClick: (CommandItem) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SPP Communication") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(
                text = uiState.status,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = if (uiState.connected)
                    colorScheme.primary
                else
                    colorScheme.error
            )

            HorizontalDivider()

            val scrollState = rememberScrollState()
            val colorScheme = colorScheme
            val chunks = uiState.chunks

            val consoleText: AnnotatedString = remember(chunks, colorScheme) {
                buildAnnotatedString {
                    chunks.forEach { chunk ->
                        val style = when (chunk.dir) {
                            MsgDir.RX -> SpanStyle(
                                color = colorScheme.secondary
                            )
                            MsgDir.TX -> SpanStyle(
                                color = colorScheme.primary
                            )
                        }
                        withStyle(style) {
                            append(chunk.text)
                            append("\n")
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = consoleText,
                    fontFamily = FontFamily.Monospace
                )
            }

            LaunchedEffect(consoleText.length) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            HorizontalDivider()

            CommandPalette(
                commands = commands,
                enabled = uiState.connected,
                onCommandClick = onCommandClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = uiState.input,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.connected
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSendClick,
                    enabled = uiState.connected && uiState.input.isNotEmpty()
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun CommandPalette(
    commands: List<CommandItem>,
    enabled: Boolean,
    onCommandClick: (CommandItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (commands.isEmpty()) return

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        items(commands) { cmd ->
            Text(
                text = cmd.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { onCommandClick(cmd) }
                    .padding(8.dp)
            )
        }
    }
}
