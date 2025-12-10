package com.apulsetech.apuls2

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit

fun main() = application {
    FileKit.init(appId = "Apuls")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Apuls2",
    ) {
        MaterialTheme {
            App()
        }
    }
}