package com.apulsetech.apuls.command

interface IConstraint {
    fun validate(v: Any): Boolean
}

fun range(range: IntRange): IConstraint {
    class Impl : IConstraint {
        override fun validate(v: Any): Boolean {
            return when (v) {
                is Int -> v in range
                else -> false
            }
        }
    }

    return Impl()
}

fun sized(max: Int): IConstraint {
    class Impl : IConstraint {
        override fun validate(v: Any): Boolean {
            return when (v) {
                is String -> v.length <= max
                else -> false
            }
        }
    }

    return Impl()
}
