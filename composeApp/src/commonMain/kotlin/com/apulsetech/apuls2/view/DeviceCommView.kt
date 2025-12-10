package com.apulsetech.apuls2.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PowerInput
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls2.net.Session
import com.apulsetech.apuls2.platform.Device
import com.apulsetech.apuls2.platform.NoopDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun DeviceCommViewPreview() {
    DeviceCommView(NoopDevice)
}

@Composable
fun DeviceCommView(device: Device, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val session = remember(device) { Session(device) }

    val nav = rememberNavController()
    var route by rememberSaveable { mutableIntStateOf(4) }

    val state by session.state.collectAsState()

    LaunchedEffect(session) {
        session.open(coroutineScope)
    }

    DisposableEffect(session) {
        onDispose {
            coroutineScope.launch(Dispatchers.IO) {
                session.close()
            }
        }
    }

    Scaffold(modifier = modifier) { padding ->
        if (state.error != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Error, contentDescription = "Error")

                    Text(state.error!!)
                }
            }
        } else if (!state.ready) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                val entries = listOf(
                    "Settings" to Icons.Rounded.Settings,
                    "Terminal" to Icons.Rounded.Terminal,
                    "Read/Write" to Icons.Rounded.QrCodeScanner,
                    "GPIO" to Icons.Rounded.PowerInput,
                    "Inventory" to Icons.Rounded.CellTower
                )

                PrimaryTabRow(selectedTabIndex = route) {
                    entries.forEachIndexed { i, entry ->
                        Tab(
                            selected = route == i,
                            onClick = {
                                route = i
                                nav.navigate(entry.first) {
                                    launchSingleTop = true
                                    popUpTo(nav.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                            },
                            text = { Text(entry.first) },
                            icon = {
                                Icon(entry.second, contentDescription = entry.first)
                            }
                        )
                    }
                }

                NavHost(
                    navController = nav,
                    startDestination = "Inventory",
                    modifier = Modifier.fillMaxHeight()
                ) {
                    composable("Settings") { SettingsView(session) }
                    composable("Terminal") { TerminalView(session) }
                    composable("Read/Write") { ReadWriteView(session) }
                    composable("GPIO") { GpioView(session) }
                    composable("Inventory") { InventoryView(session) }
                }
            }
        }
    }
}
