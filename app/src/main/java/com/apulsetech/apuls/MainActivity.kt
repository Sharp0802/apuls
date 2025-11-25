package com.apulsetech.apuls

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls.ui.theme.ApulsTheme
import com.apulsetech.apuls.view.DeviceCommView
import com.apulsetech.apuls.view.DeviceSelectView
import com.apulsetech.apuls.viewmodel.DeviceSelectViewModel

private const val ROUTE_DEVICE_SELECT = "device_select"
private const val ROUTE_DEVICE_COMM = "device_comm"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.context = applicationContext

        enableEdgeToEdge()
        setContent {
            ApulsTheme {
                val navController = rememberNavController()
                val vm: DeviceSelectViewModel = viewModel()
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        vm.refresh()
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DEVICE_SELECT
                ) {
                    composable(ROUTE_DEVICE_SELECT) {
                        DeviceSelectView(
                            vm = vm,
                            onDeviceSelected = { dev ->
                                vm.selectDevice(dev)
                                navController.navigate(ROUTE_DEVICE_COMM)
                            },
                            onRequestPermission = {
                                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                            }
                        )
                    }

                    composable(ROUTE_DEVICE_COMM) {
                        DeviceCommView(vm.selectedDevice.value!!)
                    }
                }
            }
        }
    }
}
