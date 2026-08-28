package com.homejobs.android.ui.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.homejobs.android.data.local.photo.PhotoStorage
import com.homejobs.android.domain.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Full-screen photo viewer with share/save-to-gallery actions, reused from both the job detail
 * timeline and the job's all-photos grid.
 */
@Composable
fun PhotoViewerDialog(photo: Photo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorage(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var permissionRequestPending by remember { mutableStateOf(false) }

    fun saveToGallery() {
        scope.launch {
            val saved = withContext(Dispatchers.IO) { photoStorage.saveToGallery(photo.filePath) }
            val message = if (saved) "Saved to Photos" else "Couldn't save photo"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            saveToGallery()
        } else {
            Toast.makeText(context, "Storage permission needed to save photos", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(permissionRequestPending) {
        if (permissionRequestPending) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissionRequestPending = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(
                model = File(photo.filePath),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
            )

            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, photoStorage.shareUriFor(photo.filePath))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share photo"))
                }) {
                    Icon(Icons.Filled.Share, contentDescription = "Share photo", tint = Color.White)
                }
                IconButton(onClick = {
                    val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    if (needsPermission) permissionRequestPending = true else saveToGallery()
                }) {
                    Icon(Icons.Filled.Download, contentDescription = "Save to gallery", tint = Color.White)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Text(
                text = photo.createdAt.toDisplayDateTime(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}
