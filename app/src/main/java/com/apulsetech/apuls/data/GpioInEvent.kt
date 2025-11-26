package com.apulsetech.apuls.data

import java.io.Serializable

data class GpioInEvent(
    val enabled: Boolean,
    val time: UInt,
    val command: String
) : Serializable
