package com.apulsetech.apuls.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class BluetoothDevice : Device {
    private val _inner: BluetoothDevice

    private constructor(inner: BluetoothDevice) : super() {
        this._inner = inner
    }

    @SuppressLint("MissingPermission") // already checked in BluetoothDevice.get
    override fun name(): String {
        return _inner.name
    }

    override fun desc(): String {
        return _inner.address
    }

    override fun open(): DeviceSocket? {
        val socket = _inner.createRfcommSocketToServiceRecord(SPP_UUID)
        return try {
            socket.connect()
            BluetoothDeviceSocket(socket)
        } catch (e: IOException) {
            Log.e("BluetoothDevice", e.toString())
            try {
                socket.close()
            } catch (_: IOException) {
            }
            null
        }
    }

    companion object {
        fun get(ctx: Context): Iterable<Device> {
            val perm = Manifest.permission.BLUETOOTH_CONNECT
            if (ctx.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            val bluetoothManager =
                ctx.getSystemService(BluetoothManager::class.java) ?: return emptyList()

            val adapter = bluetoothManager.adapter
            if (!adapter.isEnabled) {
                return emptyList()
            }

            return adapter.bondedDevices.map { device -> BluetoothDevice(device) }
        }
    }
}

internal class BluetoothDeviceSocket(inner: BluetoothSocket) : DeviceSocket() {
    private val _inner: BluetoothSocket = inner
    private val _input: InputStream = inner.inputStream
    private val _output: OutputStream = inner.outputStream

    override fun read(buffer: ByteArray): Int {
        return _input.read(buffer)
    }

    override fun write(buffer: ByteArray) {
        _output.write(buffer)
        _output.flush()
    }

    override fun close() {
        _inner.close()
    }
}
