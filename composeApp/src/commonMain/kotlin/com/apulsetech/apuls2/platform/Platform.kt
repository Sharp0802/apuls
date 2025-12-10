package com.apulsetech.apuls2.platform

interface Platform {
    val name: String
    val debug: Boolean

    fun requestPermission(permissions: Iterable<String>, cb: (Boolean) -> Unit)
    fun hasPermission(permissions: Iterable<String>): Boolean
}

expect fun getPlatform(): Platform
