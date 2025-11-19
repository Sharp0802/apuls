package com.apulsetech.apuls.device

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

internal class UsbDevice(manager: UsbManager, inner: UsbDevice) : Device() {
    private val _manager: UsbManager = manager
    private val _inner: UsbDevice = inner

    override fun name(): String {
        return _inner.deviceName
    }

    override fun desc(): String {
        return "COM" + _inner.serialNumber
    }

    override fun open(): DeviceSocket? {
        if (!_manager.hasPermission(_inner)) {
            return null
        }

        val driver = UsbSerialProber.getDefaultProber().probeDevice(_inner) ?: return null
        val conn = _manager.openDevice(_inner) ?: return null

        val port = driver.ports[0]
        port.open(conn)
        port.dtr = true
        port.rts = true
        port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

        return UsbDeviceSocket(port)
    }

    companion object {
        fun get(ctx: Context): Iterable<Device> {
            val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager
            return manager.deviceList.values.map { device -> UsbDevice(manager, device) }
        }
    }
}

internal class UsbDeviceSocket(inner: UsbSerialPort) : DeviceSocket() {
    private val _inner: UsbSerialPort = inner

    override fun read(buffer: ByteArray): Int {
        return _inner.read(buffer, 100)
    }

    override fun write(buffer: ByteArray) {
        _inner.write(buffer, 0)
    }

    override fun close() {
        _inner.close()
    }
}
