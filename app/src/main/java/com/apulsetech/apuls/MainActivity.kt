package com.apulsetech.apuls

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apulsetech.apuls.device.Device
import com.apulsetech.apuls.ui.theme.ApulsTheme
import com.apulsetech.apuls.views.DeviceSelectView
import com.apulsetech.apuls.views.DeviceSelectViewModel
import kotlinx.serialization.Serializable

@Serializable
object DeviceSelect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApulsTheme {
                val navController = rememberNavController()
                val vm: DeviceSelectViewModel = viewModel()


                NavHost(
                    navController = navController,
                    startDestination = DeviceSelect
                ) {
                    composable<DeviceSelect> {
                        DeviceSelectView(
                            vm = vm,
                            onDeviceSelected = { dev ->
                                navController.navigate(dev)
                            },
                            onRequestPermission = {
                                // BLUETOOTH_CONNECT 권한 런처 호출 자리
                            }
                        )
                    }

                    composable<Device> { backStackEntry ->
                        DeviceCommView(
                            device = backStackEntry.toRoute<Device>(),
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
