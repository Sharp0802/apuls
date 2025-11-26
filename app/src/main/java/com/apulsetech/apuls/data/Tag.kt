package com.apulsetech.apuls.data

import java.io.Serializable

data class Tag(
    val value: String,
    val ant: Int?,
    val rssi: Int?,
    val rid: Int?,
    val freq: Int?,
    val ip: Ip?,
    val date: String?,
    val cs: Int?
) : Serializable {
    override fun equals(other: Any?): Boolean = other is Tag && value == other.value
    override fun hashCode(): Int = value.hashCode()
}
