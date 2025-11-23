package com.apulsetech.apuls.command

interface IConstraint {
    fun validate(v: Any): Boolean
}

class RangeConstraint(val range: IntRange) : IConstraint {
    override fun validate(v: Any): Boolean = when (v) {
        is Int -> v in range
        else -> false
    }

    override fun toString(): String = "must be in ${range.first}..${range.last}"
}

fun range(range: IntRange): IConstraint {
    return RangeConstraint(range)
}

class SizedConstraint(val max: Int) : IConstraint {
    override fun validate(v: Any): Boolean = when (v) {
        is String -> v.length <= max
        else -> false
    }

    override fun toString(): String = "length must be in 0..$max"
}

fun sized(max: Int): IConstraint {
    return SizedConstraint(max)
}
