package com.apulsetech.apuls

import java.util.concurrent.CopyOnWriteArrayList

class Event<T> {
    private val callbacks = CopyOnWriteArrayList<suspend (T) -> Unit>()

    suspend operator fun invoke(v: T) {
        callbacks.forEach { it(v) }
    }

    fun register(fn: suspend (T) -> Unit) {
        callbacks.add(fn)
    }

    fun unregister(fn: suspend (T) -> Unit) {
        callbacks.remove(fn)
    }
}
