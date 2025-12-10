package com.apulsetech.apuls2.platform

import com.apulsetech.apuls2.data.Ip
import java.net.SocketTimeoutException
import kotlin.math.min

expect fun getActualDevices(): Iterable<Device>

interface Device {
    companion object {
        fun all(): Iterable<Device> {
            if (getPlatform().debug) {
                return getActualDevices().plus(NoopDevice)
            }

            return getActualDevices()
        }
    }

    val name: String
    val desc: String

    fun open(): Socket
}

class TcpDevice(val ip: Ip, val port: UShort) : Device {
    override val name: String get() = "TCP/IP Device"
    override val desc: String get() = "$ip:$port"

    override fun open(): Socket = object : Socket {
        val socket = java.net.Socket(ip.toString(), port.toInt()).apply {
            // Avoid blocking indefinitely so the session loop can keep flushing the send queue
            soTimeout = 100
        }
        val input = socket.getInputStream()
        val output = socket.getOutputStream()

        override fun write(buffer: ByteArray, offset: Int, size: Int): Int {
            output.write(buffer, offset, size)
            return size
        }

        override fun read(buffer: ByteArray, offset: Int, size: Int): Int {
            val available = input.available()
            if (available == 0) return 0

            return try {
                input.read(buffer, offset, min(size, available))
            } catch (_: SocketTimeoutException) {
                0
            }
        }

        override fun flush() {
            output.flush()
        }

        override fun close() {
            input.close()
            output.close()
            socket.close()
        }
    }
}

object NoopDevice : Device {
    override val name: String
        get() = "NO-OP Device"
    override val desc: String
        get() = "For debugging; Report if it's release build"

    override fun open(): Socket = object : Socket {
        override fun write(buffer: ByteArray, offset: Int, size: Int): Int = size
        override fun read(buffer: ByteArray, offset: Int, size: Int): Int = 0
        override fun flush() {}
        override fun close() {}
    }
}
