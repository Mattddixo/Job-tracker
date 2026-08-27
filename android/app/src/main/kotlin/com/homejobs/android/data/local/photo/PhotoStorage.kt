package com.homejobs.android.data.local.photo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

data class CaptureTarget(val filePath: String, val uri: Uri)

/**
 * Photos are never referenced by their original picker/camera Uri long-term — those are only
 * guaranteed readable for a short time (no persisted permission), so every photo is copied (or,
 * for the camera, written directly) into this app's private storage the moment it's captured.
 * [PhotoEntity.filePath][com.homejobs.android.data.local.db.PhotoEntity] always points here.
 */
class PhotoStorage @Inject constructor(@ApplicationContext private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { mkdirs() }

    /** Copies the bytes behind a picked (e.g. gallery) Uri into app-private storage. */
    fun copyToAppStorage(sourceUri: Uri): String {
        val destination = File(photosDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IOException("Could not open $sourceUri")
        return destination.absolutePath
    }

    /**
     * Pre-creates the file the camera app will write a capture into, and a FileProvider Uri for
     * it. The file path is already known up front, so there's no picker Uri to resolve afterward.
     */
    fun createCaptureTarget(): CaptureTarget {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return CaptureTarget(filePath = file.absolutePath, uri = uri)
    }

    fun deleteFile(path: String) {
        runCatching { File(path).delete() }
    }
}
