package com.apulsetech.apuls2.data

enum class Mask {
    None, PermanentOnly, PasswordRequiredOnly, All;

    override fun toString(): String {
        return when (this) {
            None -> "None"
            PermanentOnly -> "Permanent Only"
            PasswordRequiredOnly -> "Password Required Only"
            All -> "All"
        }
    }
}
