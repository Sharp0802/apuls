package com.apulsetech.apuls2.platform

class NoPermissionException(val permissions: Array<String>) :
    Exception("Required permission ${permissions.joinToString(", ")} ${if (permissions.size == 1) "is" else "are"} missing")
