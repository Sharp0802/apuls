package com.apulsetech.apuls.data

data class ReadOp(
    val bank: UInt,
    val bitPtr: UInt,
    val len: UInt
)
