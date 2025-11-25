package com.apulsetech.apuls.data

data class Tag(
    val value: String,
    val ant: Int?,
    val rssi: Int?,
    val rid: Int?,
    val freq: Int?,
    val ip: Ip?,
    val date: String?,
    val cs: Int?
) {
    override fun equals(other: Any?): Boolean = other is Tag && value == other.value
    override fun hashCode(): Int = value.hashCode()
}
