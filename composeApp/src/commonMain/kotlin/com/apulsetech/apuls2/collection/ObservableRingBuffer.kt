package com.apulsetech.apuls2.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue

class ObservableRingBuffer<T>(val capacity: Int) : Iterable<T> {
    private val buffer: Array<T?> = arrayOfNulls(capacity)
    private var head: Int = 0
    private var tail: Int = 0

    var size by mutableIntStateOf(0)
        private set

    var version by mutableLongStateOf(0L)
        private set

    fun write(item: T) {
        if (size == capacity) {
            tail++
        } else {
            size++
        }

        buffer[head % capacity] = item
        head++

        version++
    }

    operator fun get(i: Int): T {
        if (i !in 0 until size)
            throw IndexOutOfBoundsException(i)

        @Suppress("UNCHECKED_CAST")
        return buffer[(tail + i) % capacity] as T
    }

    fun keyFor(i: Int): Long {
        if (i !in 0 until size)
            throw IndexOutOfBoundsException(i)

        return (tail + i).toLong()
    }

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        private var i = 0
        override fun next(): T = get(i++)
        override fun hasNext(): Boolean = i < size
    }
}
