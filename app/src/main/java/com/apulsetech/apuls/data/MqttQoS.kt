package com.apulsetech.apuls.data

enum class MqttQoS {
    AtMostOnce,
    AtLeastOnce,
    ExactlyOnce;

    override fun toString(): String {
        return when (this) {
            AtMostOnce -> "At Most Once"
            AtLeastOnce -> "At Least Once"
            ExactlyOnce -> "Exactly Once"
        }
    }
}
