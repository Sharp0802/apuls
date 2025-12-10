package com.apulsetech.apuls2.platform

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual suspend fun pickFileSaveLocation(
    suggestedName: String,
    extension: String
): PlatformFile? = withContext(Dispatchers.Main) {
    val chooser = JFileChooser().apply {
        dialogTitle = "Save inventory"
        fileFilter = FileNameExtensionFilter("${extension.uppercase()} files", extension)
        selectedFile = File(System.getProperty("user.home"), "$suggestedName.$extension")
    }

    val result = chooser.showSaveDialog(null)
    if (result != JFileChooser.APPROVE_OPTION) return@withContext null

    var file = chooser.selectedFile ?: return@withContext null
    if (file.extension.equals(extension, ignoreCase = true).not()) {
        file = File(file.parentFile ?: File("."), "${file.name}.$extension")
    }

    PlatformFile(file)
}
