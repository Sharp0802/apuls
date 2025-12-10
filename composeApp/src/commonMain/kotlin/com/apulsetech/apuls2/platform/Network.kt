package com.apulsetech.apuls2.platform

import java.net.NetworkInterface

object Network {
    val ip: String?
        get() {
            for (card in NetworkInterface.getNetworkInterfaces()) {
                for (address in card.inetAddresses) {
                    if (address.isLoopbackAddress) continue

                    val host = address.hostAddress ?: continue
                    if (host.contains(':')) continue

                    return host
                }
            }

            return null
        }
}
