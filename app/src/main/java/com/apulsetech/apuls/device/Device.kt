package com.apulsetech.apuls.device

import android.content.Context
import kotlinx.serialization.Serializable
import java.io.Closeable

abstract class DeviceSocket : Closeable {
    abstract fun read(buffer: ByteArray): Int
    abstract fun write(buffer: ByteArray)
}

@Serializable
abstract class Device {
    abstract fun name(): String
    abstract fun desc(): String
    abstract fun open(): DeviceSocket?

    companion object {
        fun get(ctx: Context): Iterable<Device> {
            return BluetoothDevice.get(ctx)//.plus(UsbDevice.get(ctx))
        }
    }
}
