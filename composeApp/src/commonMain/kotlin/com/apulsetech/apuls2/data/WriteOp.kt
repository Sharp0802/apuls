package com.apulsetech.apuls2.data

import java.io.Serializable

data class WriteOp(
    val bank: UInt,
    val bitPtr: UInt,
    val data: UInt
) : Serializable
