package com.apulsetech.apuls.data

data class WriteOp(
    val bank: UInt,
    val bitPtr: UInt,
    val data: UInt
)
