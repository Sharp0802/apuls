package com.apulsetech.apuls

import kotlinx.coroutines.channels.Channel


class Event<T> {
    private val callbacks = Channel<suspend (T) -> Unit>(capacity = Channel.UNLIMITED)

    suspend operator fun invoke(v: T) {
        while (true) {
            val fn = callbacks.tryReceive().getOrNull() ?: break
            fn(v)
        }
    }

    suspend fun register(fn: suspend (T) -> Unit) {
        callbacks.send(fn)
    }
}
