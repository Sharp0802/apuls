package com.apulsetech.apuls.data

import java.io.Serializable

data class SelectQuery(
    val bank: UInt,
    val bitPtr: UInt,
    val mask: UInt
) : Serializable
