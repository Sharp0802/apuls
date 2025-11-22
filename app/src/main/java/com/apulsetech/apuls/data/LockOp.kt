package com.apulsetech.apuls.data

data class LockOp(
    val user: Pair<Mask, Access>,
    val tid: Pair<Mask, Access>,
    val epc: Pair<Mask, Access>,
    val access: Pair<Mask, Access>,
    val kill: Pair<Mask, Access>,
)
