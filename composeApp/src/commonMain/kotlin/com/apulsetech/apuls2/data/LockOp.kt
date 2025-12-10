package com.apulsetech.apuls2.data

import com.apulsetech.apuls2.data.Access
import java.io.Serializable

data class LockOp(
    val user: Pair<Mask, Access>,
    val tid: Pair<Mask, Access>,
    val epc: Pair<Mask, Access>,
    val access: Pair<Mask, Access>,
    val kill: Pair<Mask, Access>,
) : Serializable
