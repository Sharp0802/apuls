package com.apulsetech.apuls.data

data class GpioInEvent(
    val enabled: Boolean, val time: UInt, val command: String
)
