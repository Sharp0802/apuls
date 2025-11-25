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
)
