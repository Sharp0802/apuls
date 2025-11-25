package com.apulsetech.apuls.viewmodel

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apulsetech.apuls.App
import com.apulsetech.apuls.collection.ObservableRingBuffer
import com.apulsetech.apuls.command.Command
import com.apulsetech.apuls.command.CommandDeclarations
import com.apulsetech.apuls.data.Tag
import com.apulsetech.apuls.data.text.parse
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.device.DeviceSession
import com.apulsetech.apuls.device.DeviceSocket
import com.apulsetech.apuls.view.ConsoleLine
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
    socket: DeviceSocket,
    scope: CoroutineScope,
    val receive: suspend (Command) -> Unit,
    val dispose: suspend () -> Unit
) : DeviceSession(socket, scope) {
    val channel = Channel<ConsoleLine>(capacity = 4096)

    override suspend fun onReceived(line: String) {
        channel.send(
            ConsoleLine(
                line, DeviceCommViewModel.RX
            )
        )

        val command: Command = try {
            line.parse()
        } catch (t: Throwable) {
            Log.e("Session", "Couldn't parse line", t)
            channel.send(
                ConsoleLine(
                    t.message ?: t.toString(), DeviceCommViewModel.ERR
                )
            )
            return
        }

        receive(command)
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

    val logs = ObservableRingBuffer<ConsoleLine>(LOGS_MAX_LINE)

    val tags = mutableStateMapOf<String, Tag>()

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

        val session = Session(socket, viewModelScope, receive = {
            if (it.declaration != CommandDeclarations.tag.value) return@Session

            val value = it.state as Tag
            tags[value.value] = value

            vibrate(10)
        }, dispose = {
            viewModelScope.launch {
                state = UiState("Disconnected")
            }
        })
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

    private fun vibrate(ms: Long) {
        val man = App.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val vibrator = man.defaultVibrator

        vibrator.vibrate(VibrationEffect.createOneShot(ms, 255))
    }

    fun send(line: String) {
        val session = session
        if (session == null) {
            logs.write(ConsoleLine("session is not yet initialized", ERR))
            return
        }

        if (!session.send(line)) {
            logs.write(ConsoleLine("failed to send line to channel", ERR))
            return
        }

        // send should called in main context
        logs.write(ConsoleLine(line, TX))
    }

    override fun onCleared() {
        super.onCleared()

        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            sessionJob.cancelAndJoin()
        }
    }
}
