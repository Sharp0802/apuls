package com.apulsetech.apuls2.platform

import java.io.Closeable

interface Socket : Closeable {
    fun write(buffer: ByteArray, offset: Int, size: Int): Int
    fun read(buffer: ByteArray, offset: Int, size: Int): Int
    fun flush()
}
