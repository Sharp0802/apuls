package com.apulsetech.apuls2.platform

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override val debug: Boolean
        get() = true // TODO: Use conditional build

    override fun requestPermission(permissions: Iterable<String>, cb: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun hasPermission(permissions: Iterable<String>): Boolean = true
}

actual fun getPlatform(): Platform = JVMPlatform()