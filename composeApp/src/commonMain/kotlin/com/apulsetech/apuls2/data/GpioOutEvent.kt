package com.apulsetech.apuls2.data

import java.io.Serializable

data class GpioOutEvent(
    val enabled: Boolean,
    val time: UInt
) : Serializable
