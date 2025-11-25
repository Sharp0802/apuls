package com.apulsetech.apuls.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.viewmodel.DeviceSelectViewModel
import com.apulsetech.apuls.viewmodel.DisabledReason


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectView(
    vm: DeviceSelectViewModel,
    onDeviceSelected: (Device) -> Unit,
    onRequestPermission: (() -> Unit)? = null,
) {
    val state by vm.state.collectAsState()
    val refreshing by vm.refreshing.collectAsState()

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Scaffold { innerPadding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state.disabledReason) {
                DisabledReason.NONE -> {
                    DeviceList(
                        devices = state.devices,
                        modifier = Modifier.fillMaxSize(),
                        onDeviceSelected = onDeviceSelected
                    )
                }
                DisabledReason.BLUETOOTH_OFF -> {
                    DisabledView(
                        text = "Bluetooth disabled",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DisabledReason.NO_PERMISSION -> {
                    DisabledView(
                        text = "Bluetooth permission denied",
                        modifier = Modifier.fillMaxSize(),
                        showAction = onRequestPermission != null,
                        onActionClick = onRequestPermission
                    )
                }
            }
        }
    }
}

@Composable
private fun DisabledView(
    text: String,
    modifier: Modifier = Modifier,
    showAction: Boolean = false,
    onActionClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                color = colorScheme.onSurface.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodyLarge
            )

            if (showAction && onActionClick != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant permission",
                    color = colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clickable { onActionClick() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<Device>,
    modifier: Modifier = Modifier,
    onDeviceSelected: (Device) -> Unit
) {
    if (devices.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No devices", color = colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(devices) { dev ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeviceSelected(dev) }
                        .padding(16.dp)
                ) {
                    Text(dev.name(), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        dev.desc(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
