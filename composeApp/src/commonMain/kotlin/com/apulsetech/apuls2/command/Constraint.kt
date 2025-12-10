package com.apulsetech.apuls2.command

interface Constraint {
    fun validate(v: Any): Boolean
}

class RangeConstraint(val range: IntRange) : Constraint {
    override fun validate(v: Any): Boolean = when (v) {
        is Int -> v in range
        else -> false
    }

    override fun toString(): String = "must be in ${range.first}..${range.last}"
}

fun range(range: IntRange): Constraint {
    return RangeConstraint(range)
}

class SizedConstraint(val max: Int) : Constraint {
    override fun validate(v: Any): Boolean = when (v) {
        is String -> v.length <= max
        else -> false
    }

    override fun toString(): String = "length must be in 0..$max"
}

fun sized(max: Int): Constraint {
    return SizedConstraint(max)
}
