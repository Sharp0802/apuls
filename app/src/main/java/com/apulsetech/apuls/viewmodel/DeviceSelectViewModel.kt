package com.apulsetech.apuls.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apulsetech.apuls.device.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class DisabledReason {
    NONE,
    BLUETOOTH_OFF,
    NO_PERMISSION
}

data class DeviceSelectViewState(
    val disabledReason: DisabledReason = DisabledReason.NONE,
    val devices: List<Device> = emptyList()
)

class DeviceSelectViewModel(app: Application) : AndroidViewModel(app) {
    private val _app = app
    private val _refreshing = MutableStateFlow(false)
    private val _state = MutableStateFlow(DeviceSelectViewState())
    private val _selectedDevice = MutableStateFlow<Device?>(null)

    internal val state: StateFlow<DeviceSelectViewState> = _state
    internal val refreshing: StateFlow<Boolean> = _refreshing
    internal val selectedDevice: StateFlow<Device?> = _selectedDevice

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _refreshing.value = true
            _state.value = queryState()
            _refreshing.value = false
        }
    }

    private fun queryState(): DeviceSelectViewState {
        val ctx = _app.applicationContext

        val perm = Manifest.permission.BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(ctx, perm) != PackageManager.PERMISSION_GRANTED) {
            return DeviceSelectViewState(
                disabledReason = DisabledReason.NO_PERMISSION
            )
        }

        val bluetoothManager = ctx.getSystemService(BluetoothManager::class.java) ?: return DeviceSelectViewState(disabledReason = DisabledReason.BLUETOOTH_OFF)
        val adapter = bluetoothManager.adapter

        if (!adapter.isEnabled) {
            return DeviceSelectViewState(disabledReason = DisabledReason.BLUETOOTH_OFF)
        }

        return DeviceSelectViewState(
            disabledReason = DisabledReason.NONE,
            devices = Device.get(ctx).toList()
        )
    }

    fun selectDevice(device: Device) {
        _selectedDevice.value = device
    }

    fun clearSelectedDevice() {
        _selectedDevice.value = null
    }
}
