package com.apulsetech.apuls2.platform

import io.github.vinceglb.filekit.PlatformFile

expect suspend fun pickFileSaveLocation(
    suggestedName: String,
    extension: String
): PlatformFile?
