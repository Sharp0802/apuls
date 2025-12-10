package com.apulsetech.apuls2.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NetworkWifi3Bar
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls2.data.Ip
import com.apulsetech.apuls2.data.text.parse
import com.apulsetech.apuls2.platform.Device
import com.apulsetech.apuls2.platform.Network
import com.apulsetech.apuls2.platform.TcpDevice
import com.apulsetech.apuls2.platform.getPlatform
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
private fun DeviceSelectViewPreview() {
    DeviceSelectView {}
}

@Composable
fun DeviceSelectView(modifier: Modifier = Modifier, onDeviceSelected: (Device) -> Unit) {
    val defaultRoute = "tcp/ip"

    val nav = rememberNavController()
    var route by remember { mutableStateOf(defaultRoute) }
    var title by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            fun navigate(r: String) {
                route = r
                nav.navigate(r) {
                    launchSingleTop = true
                    popUpTo(nav.graph.startDestinationId) {
                        saveState = true
                    }
                    restoreState = true
                }
            }

            val entries = mapOf(
                "Serial" to Icons.Rounded.Usb,
                "Tcp/Ip" to Icons.Rounded.NetworkWifi3Bar,
            )

            NavigationBar {
                entries.forEach {
                    NavigationBarItem(
                        selected = route == it.key.lowercase(),
                        onClick = { navigate(it.key.lowercase()) },
                        icon = { Icon(it.value, contentDescription = it.key) },
                        label = { Text(it.key) }
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = title,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            NavHost(navController = nav, startDestination = defaultRoute) {
                composable("serial") {
                    title = "Serial Device"
                    SerialDeviceSelectView(onDeviceSelected = onDeviceSelected)
                }

                composable("tcp/ip") {
                    title = "TCP/IP Connection"
                    TcpDeviceSelectView(onDeviceSelected = onDeviceSelected)
                }
            }
        }
    }
}

@Composable
private fun TcpDeviceSelectView(onDeviceSelected: (Device) -> Unit) {
    val ip = rememberTextFieldState("192.168.0.100")
    val ipError by remember {
        derivedStateOf {
            try {
                ip.text.trim().toString().parse<Ip>()
                null
            } catch (e: Throwable) {
                e.message
            }
        }
    }

    val port = rememberTextFieldState("25000")
    val portError by remember {
        derivedStateOf {
            if (port.text.trim().toString().toUShortOrNull() == null) {
                "Invalid port number"
            } else {
                null
            }
        }
    }

    val currentIp = rememberTextFieldState("")

    LaunchedEffect(Unit) {
        currentIp.setTextAndPlaceCursorAtEnd(Network.ip ?: "IP not assigned")
    }

    Column(
        modifier = Modifier.wrapContentHeight().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                label = { Text("Current IP") },
                state = currentIp,
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                label = { Text("IP") },
                state = ip,
                modifier = Modifier.weight(1f),
                isError = ipError != null,
                supportingText = {
                    ipError?.let {
                        Text(it)
                    }
                }
            )

            OutlinedTextField(
                label = { Text("Port") },
                state = port,
                modifier = Modifier.weight(1f),
                isError = portError != null,
                supportingText = {
                    portError?.let {
                        Text(it)
                    }
                }
            )
        }

        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                val ip: Ip = try {
                    ip.text.trim().toString().parse()
                } catch (_: Throwable) {
                    return@Button
                }

                val port = port.text.trim().toString().toUShortOrNull() ?: return@Button

                onDeviceSelected(TcpDevice(ip, port))
            }) {
                Text("Connect")
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SerialDeviceSelectView(
    vm: SerialDeviceSelectViewModel = viewModel(),
    onDeviceSelected: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshing by vm.refreshing.collectAsState()
    val requiredPermission by vm.requiredPermissions.collectAsState()
    val devices by vm.devices.collectAsState()

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { vm.refresh() },
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        if (requiredPermission == null) {
            DeviceList(
                devices = devices,
                modifier = Modifier.fillMaxSize(),
                onDeviceSelected = onDeviceSelected
            )
        } else {
            DisabledView(
                text = "Permission denied",
                modifier = Modifier.fillMaxSize(),
                onActionClick = {
                    getPlatform().requestPermission(requiredPermission!!.asIterable()) {
                        vm.refresh()
                    }
                }
            )
        }
    }
}

@Composable
private fun DisabledView(
    text: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
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

@Composable
private fun DeviceList(
    devices: List<Device>,
    onDeviceSelected: (Device) -> Unit,
    modifier: Modifier = Modifier
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
                    Text(dev.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        dev.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
