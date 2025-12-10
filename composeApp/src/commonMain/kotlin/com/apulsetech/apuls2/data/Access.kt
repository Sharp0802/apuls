package com.apulsetech.apuls2.data

enum class Access {
    Accessible, AccessiblePermanently, PasswordRequired, NotAccessible;

    override fun toString(): String {
        return when (this) {
            Accessible -> "Accessible"
            AccessiblePermanently -> "Accessible Permanently"
            PasswordRequired -> "Password Required"
            NotAccessible -> "Not Accessible"
        }
    }
}
