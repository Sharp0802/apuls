package com.apulsetech.apuls.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class UiState(
    val indicator: String = "", val connected: Boolean = false, val loading: Boolean = false
)

private class Session(
    socket: DeviceSocket, scope: CoroutineScope, val dispose: suspend () -> Unit
) : DeviceSession(socket, scope) {
    val channel = Channel<com.apulsetech.apuls.views.ConsoleLine>(capacity = 4096)

    override suspend fun onReceived(line: String) {
        channel.send(
            _root_ide_package_.com.apulsetech.apuls.views.ConsoleLine(
                line,
                DeviceCommViewModel.RX
            )
        )
    }

    override suspend fun onClosed() {
        this.dispose()
    }
}

class DeviceCommViewModel(device: Device) : ViewModel() {
    companion object {
        private const val LOGS_MAX_LINE = 256
        private const val LOGS_BATCH = 8
        private const val LOGS_BATCH_TIMEOUT = 100L

        const val TX = 0
        const val RX = 1
        const val ERR = 2
    }

    val logs = ObservableRingBuffer<com.apulsetech.apuls.views.ConsoleLine>(LOGS_MAX_LINE)

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

        val buffer = ArrayList<com.apulsetech.apuls.views.ConsoleLine>(LOGS_BATCH)

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
        logs.write(_root_ide_package_.com.apulsetech.apuls.views.ConsoleLine(line, TX))
    }

    override fun onCleared() {
        super.onCleared()

        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            sessionJob.cancelAndJoin()
        }
    }
}
