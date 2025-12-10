package com.apulsetech.apuls2

import com.apulsetech.apuls2.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}