package com.apulsetech.apuls2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apulsetech.apuls2.platform.Device
import com.apulsetech.apuls2.view.DeviceCommView
import com.apulsetech.apuls2.view.DeviceSelectView
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val nav = rememberNavController()

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
    ) {
        var device by remember { mutableStateOf<Device?>(null) }

        NavHost(navController = nav, startDestination = "select") {
            composable("select") {
                DeviceSelectView {
                    device = it
                    nav.navigate("comm")
                }
            }

            composable("comm") {
                if (device == null) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    DeviceCommView(device!!)
                }
            }
        }
    }
}
