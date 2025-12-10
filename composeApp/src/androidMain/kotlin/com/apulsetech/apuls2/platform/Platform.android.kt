package com.apulsetech.apuls2.platform

import android.content.pm.ApplicationInfo
import android.os.Build
import com.apulsetech.apuls2.MainActivity
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val debug: Boolean
        get() = MainActivity.app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    @OptIn(ExperimentalAtomicApi::class)
    override fun requestPermission(permissions: Iterable<String>, cb: (Boolean) -> Unit) {
        MainActivity.requestPermission(permissions.toList().toTypedArray(), cb)
    }

    override fun hasPermission(permissions: Iterable<String>): Boolean =
        permissions.all { MainActivity.isPermitted(it) }
}

actual fun getPlatform(): Platform = AndroidPlatform()