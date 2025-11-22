package com.apulsetech.apuls.data

data class Ip(val b0: UByte, val b1: UByte, val b2: UByte, val b3: UByte) {
    override fun toString(): String {
        return "$b0.$b1.$b2.$b3"
    }

    operator fun get(index: Int): UByte {
        return when (index) {
            0 -> b0
            1 -> b1
            2 -> b2
            3 -> b3
            else -> throw ArrayIndexOutOfBoundsException(index)
        }
    }
}
