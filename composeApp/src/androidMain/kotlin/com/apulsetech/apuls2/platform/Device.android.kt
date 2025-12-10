package com.apulsetech.apuls2.platform

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.apulsetech.apuls2.MainActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

actual fun getActualDevices(): Iterable<Device> {
    return com.apulsetech.apuls2.platform.BluetoothDevice.all()
}

private class BluetoothDevice(private val actual: BluetoothDevice) : Device {
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        fun all(): Iterable<Device> {
            val missing = mutableListOf<String>()
            if (!MainActivity.isPermitted(BLUETOOTH_SCAN, 31)) {
                missing.add(BLUETOOTH_SCAN)
            }
            if (!MainActivity.isPermitted(BLUETOOTH_CONNECT, 31)) {
                missing.add(BLUETOOTH_CONNECT)
            }
            if (missing.isNotEmpty()) {
                throw NoPermissionException(missing.toTypedArray())
            }

            val ctx = MainActivity.app.applicationContext

            val manager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = manager.adapter ?: return emptyList()

            @SuppressLint("MissingPermission")
            return adapter.bondedDevices.map { dev -> BluetoothDevice(dev) }
        }
    }

    @SuppressLint("MissingPermission")
    override val name: String = actual.name
    override val desc: String = actual.address

    override fun open(): Socket {
        val socket = actual.createRfcommSocketToServiceRecord(SPP_UUID)
        return try {
            socket.connect()
            BluetoothSocket(socket)
        } catch (e: Throwable) {
            try {
                socket.close()
            } catch (_: IOException) {
            }

            throw e
        }
    }
}

private class BluetoothSocket(private val actual: BluetoothSocket) : Socket {
    private val input: InputStream = actual.inputStream
    private val output: OutputStream = actual.outputStream

    override fun write(buffer: ByteArray, offset: Int, size: Int): Int {
        return try {
            output.write(buffer, offset, size)
            size
        } catch (t: Throwable) {
            Log.e("BluetoothSocket", "IO error during writing", t)
            -1
        }
    }

    override fun read(buffer: ByteArray, offset: Int, size: Int): Int {
        return try {
            if (input.available() == 0) return 0
            input.read(buffer, offset, size)
        } catch (t: Throwable) {
            Log.e("BluetoothSocket", "IO error during writing", t)
            return -1
        }
    }

    override fun flush() {
        try {
            output.flush()
        } catch (t: Throwable) {
            Log.e("BluetoothSocket", "IO error during flushing", t)
        }
    }

    override fun close() {
        input.close()
        output.close()
        actual.close()
    }
}
