package com.apulsetech.apuls.device

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

abstract class DeviceSession(val socket: DeviceSocket, scope: CoroutineScope) {
    private val queue = Channel<String>(capacity = Channel.UNLIMITED)

    private val thread = scope.launch(Dispatchers.IO) {
        val buffer = DeviceSocketBuffer(socket)
        while (isActive) {
            while (true) {
                val line = queue.tryReceive().getOrNull() ?: break
                val send = (line + "\r\n").toByteArray()
                socket.write(send, 0, send.size)
                socket.flush()
            }

            val received = withTimeoutOrNull(100) {
                buffer.receive()
            } ?: continue
            if (received == -1) break
            if (received == 0) continue

            val line = buffer.tryReadLine() ?: continue
            onReceived(line)
        }
    }

    private val closed = AtomicBoolean(false)

    fun send(line: String): Boolean {
        if (closed.get()) {
            Log.w("DeviceSession", "Send requested after closing")
            return false
        }

        return queue.trySend(line).isSuccess
    }

    abstract suspend fun onReceived(line: String)

    abstract suspend fun onClosed()

    suspend fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        try {
            onClosed()
        } catch (t: Throwable) {
            Log.e("DeviceSession", "Error occurred during onClosed", t)
        }

        try {
            thread.cancelAndJoin()
        } catch (t: Throwable) {
            Log.e("DeviceSession", "Error occurred during cancelAndJoin", t)
        } finally {
            socket.close()
        }
    }
}
