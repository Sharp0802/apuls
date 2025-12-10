package com.apulsetech.apuls2.platform

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.openFileSaver

actual suspend fun pickFileSaveLocation(
    suggestedName: String,
    extension: String
): PlatformFile? = FileKit.openFileSaver(
    suggestedName = suggestedName,
    extension = extension
)
