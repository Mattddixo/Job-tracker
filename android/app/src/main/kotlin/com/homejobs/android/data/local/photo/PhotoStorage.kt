package com.homejobs.android.data.local.photo

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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

    /** A content:// Uri suitable for handing [filePath] to another app (e.g. a share sheet). */
    fun shareUriFor(filePath: String): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(filePath))

    /**
     * Copies [filePath] into the device's public Pictures gallery via MediaStore, so it shows up
     * in the user's normal Photos app / file manager independent of this app. Blocking file I/O —
     * call off the main thread. Returns false (rather than throwing) on any failure, since this is
     * a best-effort "give me a copy" action, not a step in the app's own data flow.
     */
    fun saveToGallery(filePath: String): Boolean {
        val source = File(filePath)
        if (!source.exists()) return false

        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "HomeJobs_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/HomeJobsTracker")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val itemUri = resolver.insert(collection, values) ?: return false

        return try {
            val output = resolver.openOutputStream(itemUri) ?: throw IOException("Could not open $itemUri")
            output.use { source.inputStream().use { input -> input.copyTo(it) } }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(itemUri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
            true
        } catch (e: IOException) {
            resolver.delete(itemUri, null, null)
            false
        }
    }
}
