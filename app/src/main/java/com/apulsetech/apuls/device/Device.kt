package com.apulsetech.apuls.device

import android.content.Context
import java.io.Closeable

abstract class DeviceSocket : Closeable {
    abstract fun read(buffer: ByteArray, offset: Int, length: Int): Int
    abstract fun write(buffer: ByteArray, offset: Int, length: Int)
    abstract fun flush()
}

abstract class Device {
    abstract fun name(): String
    abstract fun desc(): String
    abstract fun open(): DeviceSocket

    companion object {
        fun get(ctx: Context): Iterable<Device> {
            return BluetoothDevice.get(ctx)
        }
    }
}
