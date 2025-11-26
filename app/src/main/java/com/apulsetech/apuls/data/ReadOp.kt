package com.apulsetech.apuls.data

import java.io.Serializable

data class ReadOp(
    val bank: UInt,
    val bitPtr: UInt,
    val len: UInt
) : Serializable
