package com.apulsetech.apuls2

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40
)

class MainActivity : ComponentActivity() {
    companion object {
        private var _app: Application? = null

        var app: Application
            get() = _app ?: throw NullPointerException("Application is not yet initialized")
            internal set(value) {
                _app = value
            }

        fun isPermitted(permission: String, minSdk: Int = 0): Boolean {
            return Build.VERSION.SDK_INT < minSdk || app.applicationContext.checkSelfPermission(
                permission
            ) == PERMISSION_GRANTED
        }

        @SuppressLint("StaticFieldLeak")
        private var _activity: Activity? = null

        var activity: Activity
            get() = _activity ?: throw NullPointerException("Activity is not yet initialized")
            private set(value) {
                _activity = value
            }

        @OptIn(ExperimentalAtomicApi::class)
        private val callbackId = AtomicInt(100)
        private val callbacks = ConcurrentHashMap<Int, (Boolean) -> Unit>()

        @OptIn(ExperimentalAtomicApi::class)
        fun requestPermission(perm: Array<String>, cb: (Boolean) -> Unit) {
            if (perm.all{ isPermitted(it) }) {
                cb(true)
                return
            }

            val id = callbackId.incrementAndFetch()
            callbacks[id] = cb
            activity.requestPermissions(perm, id)
        }
    }

    init {
        activity = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FileKit.manualFileKitCoreInitialization(this)

        app = application

        setContent {
            val scheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }

                isSystemInDarkTheme() -> DarkColorScheme
                else -> LightColorScheme
            }

            MaterialTheme(colorScheme = scheme) {
                App()
            }
        }
    }

    override fun onDestroy() {
        _app = null
        _activity = null
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String?>, grantResults: IntArray, deviceId: Int
    ) {
        val cb = callbacks[requestCode]
        if (cb != null) {
            callbacks.remove(requestCode)

            if (grantResults.isNotEmpty()) {
                cb.invoke(grantResults[0] == PERMISSION_GRANTED)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
