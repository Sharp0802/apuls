package com.apulsetech.apuls2.net

import com.apulsetech.apuls2.command.Command
import com.apulsetech.apuls2.data.text.parse
import com.apulsetech.apuls2.platform.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class Session(val device: Device) {
    data class State(val ready: Boolean = false, val error: String? = null)

    companion object {
        @OptIn(ExperimentalAtomicApi::class)
        val ID = AtomicInt(0)
    }

    @OptIn(ExperimentalAtomicApi::class)
    val id = ID.fetchAndAdd(1)

    private val onSendRawEvent = ConcurrentLinkedQueue<suspend (String) -> Unit>()
    private val onReceiveRawEvent = ConcurrentLinkedQueue<suspend (String) -> Unit>()
    private val onReceiveEvent = ConcurrentLinkedQueue<suspend (Command) -> Unit>()
    private val onErrorEvent = ConcurrentLinkedQueue<suspend (Throwable) -> Unit>()

    private var job: Job? = null

    private val queue = Channel<String>(capacity = Channel.UNLIMITED)

    var _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    fun open(scope: CoroutineScope) {
        job = scope.launch(Dispatchers.IO) {
            _state.value = State()

            val socket = try {
                device.open()
            } catch (t: Throwable) {
                _state.value = State(error = t.toString())
                return@launch
            }

            _state.value = State(ready = true)

            val reader = SocketReader(socket)
            while (isActive) {
                while (true) {
                    val line = queue.tryReceive().getOrNull() ?: break
                    val send = (line + "\r\n").toByteArray()
                    socket.write(send, 0, send.size)
                    socket.flush()
                }

                val received = withTimeoutOrNull(100) {
                    reader.receive()
                } ?: continue
                if (received == -1) break
                if (received == 0) continue

                val line = reader.tryReadLine() ?: continue

                val command: Command = try {
                    line.parse()
                } catch (t: Throwable) {
                    raiseOnError(t)
                    continue
                }

                raiseOnReceive(command)
            }

            _state.value = State()
        }
    }

    private suspend fun raiseOnReceive(c: Command) {
        for (cb in onReceiveEvent) {
            try {
                cb(c)
            } catch (t: Throwable) {
                raiseOnError(t)
            }
        }
    }

    private suspend fun raiseOnError(t: Throwable) {
        for (cb in onErrorEvent) {
            try {
                cb(t)
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    suspend fun send(line: String) {
        queue.send(line)
    }

    suspend fun close() {
        job?.cancelAndJoin()
        onReceiveEvent.clear()
    }

    fun onSendRaw(cb: suspend (String) -> Unit) {
        onSendRawEvent.add(cb)
    }

    fun onReceiveRaw(cb: suspend (String) -> Unit) {
        onReceiveRawEvent.add(cb)
    }

    fun onReceive(cb: suspend (Command) -> Unit) {
        onReceiveEvent.add(cb)
    }

    fun onError(cb: suspend (Throwable) -> Unit) {
        onErrorEvent.add(cb)
    }

    fun unregisterOnSendRaw(cb: suspend (String) -> Unit) {
        onSendRawEvent.remove(cb)
    }

    fun unregisterOnReceiveRaw(cb: suspend (String) -> Unit) {
        onReceiveRawEvent.remove(cb)
    }

    fun unregisterOnReceive(cb: suspend (Command) -> Unit) {
        onReceiveEvent.remove(cb)
    }

    fun unregisterOnError(cb: suspend (Throwable) -> Unit) {
        onErrorEvent.remove(cb)
    }
}
