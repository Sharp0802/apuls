package com.apulsetech.apuls2.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apulsetech.apuls2.platform.Device
import com.apulsetech.apuls2.platform.NoPermissionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SerialDeviceSelectViewModel : ViewModel() {
    val requiredPermissions = MutableStateFlow<Array<String>?>(null)
    val refreshing = MutableStateFlow(false)
    val devices = MutableStateFlow<List<Device>>(emptyList())

    fun refresh() = viewModelScope.launch(Dispatchers.Main) {
        refreshing.value = true

        requiredPermissions.value = try {
            withContext(Dispatchers.IO) {
                devices.value = Device.all().toList()
            }
            null
        } catch (e: NoPermissionException) {
            e.permissions
        }

        refreshing.value = false
    }
}
