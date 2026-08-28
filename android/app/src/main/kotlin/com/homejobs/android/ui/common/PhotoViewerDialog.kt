package com.homejobs.android.ui.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Full-screen, swipeable photo viewer with share/save-to-gallery actions, reused from both the
 * job detail note timeline and the job's all-photos grid.
 *
 * @param photos the set of photos to swipe between (a single note's photos, or every photo on
 *   the job) — [initialIndex] is where the pager opens.
 * @param captionFor text shown (togglable) under each photo, e.g. that photo's note body.
 * @param onOpenGrid when non-null, shows a button that jumps to the job's full photo grid,
 *   passed the photo currently on screen so the grid can land there. Omit when the viewer was
 *   already opened from that grid.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerDialog(
    photos: List<Photo>,
    initialIndex: Int,
    captionFor: (Photo) -> String,
    onDismiss: () -> Unit,
    onOpenGrid: ((Photo) -> Unit)? = null,
) {
    if (photos.isEmpty()) return
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorage(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, photos.lastIndex)) { photos.size }
    var showCaption by remember { mutableStateOf(true) }
    var permissionRequestPending by remember { mutableStateOf(false) }

    val currentPhoto = photos[pagerState.currentPage]

    fun saveToGallery() {
        scope.launch {
            val saved = withContext(Dispatchers.IO) { photoStorage.saveToGallery(currentPhoto.filePath) }
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
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = File(photos[page].filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
                )
            }

            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (onOpenGrid != null) {
                    IconButton(onClick = { onOpenGrid(currentPhoto) }) {
                        Icon(Icons.Filled.GridView, contentDescription = "View all photos", tint = Color.White)
                    }
                }
                val caption = captionFor(currentPhoto)
                if (caption.isNotBlank()) {
                    IconButton(onClick = { showCaption = !showCaption }) {
                        Icon(
                            if (showCaption) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showCaption) "Hide caption" else "Show caption",
                            tint = Color.White,
                        )
                    }
                }
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, photoStorage.shareUriFor(currentPhoto.filePath))
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

            val caption = captionFor(currentPhoto)
            if (showCaption && caption.isNotBlank()) {
                Text(
                    text = caption,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }

            Text(
                text = "${pagerState.currentPage + 1} / ${photos.size}  ·  ${currentPhoto.createdAt.toDisplayDateTime()}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}
