package com.apulsetech.apuls.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls.command.CommandDeclarations
import com.apulsetech.apuls.command.TypeParameterizedCommandDeclaration
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.viewmodel.DeviceCommViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCommView(device: Device) {
    val nav = rememberNavController()
    var title by rememberSaveable { mutableStateOf("") }

    val vm: DeviceCommViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceCommViewModel(device) as T
        }
    })

    Scaffold(topBar = {
        TopAppBar(title = { Text(title) }, actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = vm.state.indicator,
                    color = if (vm.state.connected) colorScheme.primary else colorScheme.error
                )
            }
        })
    }, bottomBar = {
        BottomAppBar(
            actions = {
                IconButton(onClick = { nav.navigate("settings") }) {
                    Icon(Icons.Rounded.Settings, "Settings")
                }
                IconButton(onClick = { nav.navigate("terminal") }) {
                    Icon(Icons.Rounded.Terminal, "Terminal")
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { nav.navigate("inventory") },
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Rounded.CellTower, "Inventory")
                }
            }
        )
    }) { inner ->
        Column(modifier = Modifier.padding(inner)) {
            val padding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)

            NavHost(nav, startDestination = "inventory") {
                composable("settings") {
                    title = "Settings"
                    BackHandler(true) { }

                    if (vm.state.connected) {
                        SettingsView(vm)
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                }

                composable("terminal") {
                    title = "Terminal"
                    BackHandler(true) { }

                    TerminalView(
                        state = vm.state,
                        input = vm.input,
                        logs = vm.logs,
                        onInputChanged = {
                            vm.input = it
                        },
                        onSubmit = {
                            vm.send(vm.input)
                            vm.input = ""
                        },
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    )
                }

                composable("inventory") {
                    title = "Inventory"
                    BackHandler(true) { }

                    InventoryView(
                        tags = vm.tags,
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        onStart = {
                            vm.send(CommandDeclarations.inventory.value.build())
                        },
                        onStop = {
                            @Suppress("UNCHECKED_CAST") val command =
                                CommandDeclarations.stop.value as TypeParameterizedCommandDeclaration<String>
                            vm.send(command.setter("inventory"))
                        },
                        onClear = {
                            vm.tags.clear()
                        }
                    )
                }
            }
        }
    }
}
