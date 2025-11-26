package com.apulsetech.apuls.data

import java.io.Serializable

data class WriteOp(
    val bank: UInt,
    val bitPtr: UInt,
    val data: UInt
) : Serializable
