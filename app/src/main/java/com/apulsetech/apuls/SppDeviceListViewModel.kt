package com.apulsetech.apuls

import android.app.Application
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

data class UiState(
    val status: String = "Idle",
    val chunks: List<ConsoleChunk> = emptyList(),
    val input: String = "",
    val connected: Boolean = false
)

class DeviceCommViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(UiState())
    internal val state: StateFlow<UiState> = _state

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

                    rxPending += chunk

                    while (true) {
                        val idx = rxPending.indexOf("\r\n")
                        if (idx < 0) {
                            break
                        }

                        val lineWithCrLf = rxPending.substring(0, idx + 2)
                        rxPending = rxPending.substring(idx + 2)

                        withContext(Dispatchers.Main) {
                            _state.value = _state.value.let { current ->
                                current.copy(
                                    chunks = current.chunks + ConsoleChunk(
                                        dir = MsgDir.RX,
                                        text = lineWithCrLf
                                    )
                                )
                            }
                        }

                        withContext(Dispatchers.Main) {
                            flushPendingTx()
                        }
                    }
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

        val toSend = text + "\r\n"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = toSend.encodeToByteArray()
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
            txPending += toSend
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

@Composable
fun DeviceCommView(device: Device, onBack: () -> Unit) {
    val vm: DeviceCommViewModel = viewModel()
    val uiState by vm.state.collectAsState()

    val commands = remember {
        listOf(
            CommandItem("Inventory", ":inventory"),
            CommandItem("Stop", ":stop"),
        )
    }

    LaunchedEffect(device) {
        vm.connect(device)
    }

    DisposableEffect(Unit) {
        onDispose { vm.stop() }
    }

    DeviceCommContent(
        uiState = uiState,
        onBack = onBack,
        onInputChange = { vm.updateInput(it) },
        onSendClick = { vm.send(uiState.input) },
        commands = commands,
        onCommandClick = { cmd ->
            vm.send(cmd.command)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceCommContent(
    uiState: UiState,
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
