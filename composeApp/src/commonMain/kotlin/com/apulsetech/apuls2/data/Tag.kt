package com.apulsetech.apuls2.data

import java.io.Serializable

data class Tag(
    val tag: String,
    val ant: Int? = null,
    val rssi: Int? = null,
    val rid: Int? = null,
    val freq: Int? = null,
    val ip: Ip? = null,
    val date: String? = null,
    val cs: Int? = null
) : Serializable {
    override fun toString(): String =
        "$tag,${ant ?: ""},${rssi ?: ""},${rid ?: ""},${freq ?: ""},${ip ?: ""},${date ?: ""},${cs ?: ""}"
}
