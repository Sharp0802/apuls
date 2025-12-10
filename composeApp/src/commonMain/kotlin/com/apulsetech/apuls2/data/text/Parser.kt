package com.apulsetech.apuls2.data.text

interface Parser<T> {
    fun parse(text: String): T
}
