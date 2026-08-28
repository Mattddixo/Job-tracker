package com.homejobs.android.ui.common

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
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
    val caption = captionFor(currentPhoto)

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
        // usePlatformDefaultWidth = false only fixes the dialog's width — its window still
        // defaults to wrap-content height, which can leave bottom-aligned content (the caption
        // bar) positioned past the window's actual bounds on some screens. Force true full-screen
        // sizing and let the content draw edge-to-edge (the top/bottom bars handle insets below).
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
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

            // Top bar: close on the left (standard exit spot), grouped actions on the right —
            // inset for the status bar so nothing sits under it.
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onOpenGrid != null) {
                        IconButton(onClick = { onOpenGrid(currentPhoto) }) {
                            Icon(Icons.Filled.GridView, contentDescription = "View all photos", tint = Color.White)
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
                }
            }

            // Bottom info bar: photo position + date always shown, caption (with its own
            // hide/show toggle right beside it) only when this photo has one — inset for the
            // gesture/navigation bar so it's never clipped by it.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (photos.size > 1) {
                            "${pagerState.currentPage + 1} / ${photos.size}  ·  ${currentPhoto.createdAt.toDisplayDateTime()}"
                        } else {
                            currentPhoto.createdAt.toDisplayDateTime()
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (caption.isNotBlank()) {
                        IconButton(onClick = { showCaption = !showCaption }, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(
                                if (showCaption) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showCaption) "Hide caption" else "Show caption",
                                tint = Color.White,
                            )
                        }
                    }
                }
                if (showCaption && caption.isNotBlank()) {
                    Text(
                        text = caption,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
