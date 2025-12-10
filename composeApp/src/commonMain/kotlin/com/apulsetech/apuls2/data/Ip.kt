package com.apulsetech.apuls2.data

import java.io.Serializable

data class Ip(
    val b0: UByte, val b1: UByte, val b2: UByte, val b3: UByte
) : Serializable, Comparable<Ip> {
    override fun toString(): String = "$b0.$b1.$b2.$b3"

    override fun compareTo(other: Ip): Int {
        fun toInt(ip: Ip): Int =
            (ip.b0.toInt() shl 24) or (ip.b1.toInt() shl 16) or (ip.b2.toInt() shl 8) or ip.b3.toInt()

        return toInt(this) - toInt(other)
    }
}
