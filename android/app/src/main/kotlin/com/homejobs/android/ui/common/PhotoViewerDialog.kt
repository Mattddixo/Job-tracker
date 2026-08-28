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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
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

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

/**
 * Like [androidx.compose.foundation.gestures.detectTransformGestures], but only reacts to
 * two-finger pinch/pan — a plain one-finger drag is left completely untouched (never consumed),
 * so it still reaches the HorizontalPager's own swipe-between-photos gesture instead of being
 * mistaken for a pan.
 */
private suspend fun PointerInputScope.detectPinchToZoom(onGesture: (pan: Offset, zoom: Float) -> Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            if (event.changes.size > 1) {
                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()
                if (zoomChange != 1f || panChange != Offset.Zero) {
                    onGesture(panChange, zoomChange)
                    event.changes.forEach { it.consume() }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

/**
 * Full-screen, swipeable, pinch-to-zoom photo viewer with share/save-to-gallery actions, reused
 * from both the job detail note timeline and the job's all-photos grid.
 *
 * @param photos the set of photos to swipe between (a single note's photos, or every photo on
 *   the job) — [initialIndex] is where the pager opens.
 * @param onOpenGrid when non-null, shows a button that jumps to the job's full photo grid,
 *   passed the photo currently on screen so the grid can land there. Omit when the viewer was
 *   already opened from that grid.
 * @param onGoToNote when non-null, shows a button that closes the viewer and jumps to the note
 *   the photo currently on screen is attached to.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerDialog(
    photos: List<Photo>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onOpenGrid: ((Photo) -> Unit)? = null,
    onGoToNote: ((Photo) -> Unit)? = null,
) {
    if (photos.isEmpty()) return
    val context = LocalContext.current
    val photoStorage = remember { PhotoStorage(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, photos.lastIndex)) { photos.size }
    var permissionRequestPending by remember { mutableStateOf(false) }

    // Pinch-zoom/pan state for whichever photo is currently on screen — reset whenever the user
    // swipes to a different one, and swiping itself is disabled while zoomed in so a pan gesture
    // isn't fought over by the pager.
    var zoomScale by remember { mutableStateOf(MIN_ZOOM) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(pagerState.currentPage) {
        zoomScale = MIN_ZOOM
        zoomOffset = Offset.Zero
    }

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
        // usePlatformDefaultWidth = false only fixes the dialog's width — its window still
        // defaults to wrap-content height, which can leave content positioned past the window's
        // actual bounds on some screens. Force true full-screen sizing and let the photo draw
        // edge-to-edge (the info bar below handles its own status-bar inset).
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = zoomScale <= MIN_ZOOM,
            ) { page ->
                AsyncImage(
                    model = File(photos[page].filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale,
                            translationX = zoomOffset.x,
                            translationY = zoomOffset.y,
                        )
                        .pointerInput(page) {
                            detectPinchToZoom { pan, zoom ->
                                val newScale = (zoomScale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                zoomOffset = if (newScale > MIN_ZOOM) zoomOffset + pan else Offset.Zero
                                zoomScale = newScale
                            }
                        },
                )
            }

            // Everything lives in one panel anchored under the status bar: the action buttons,
            // then the position/date line. Putting the info bar here — rather than at the
            // bottom, under the gesture/navigation bar —
            // sidesteps a real gotcha: a Dialog is its own separate window, and WindowInsets for
            // the navigation bar are not reliably delivered into it (unlike the status bar, which
            // came through fine), so bottom-anchored content could end up computing zero inset
            // and land under, or flush against, the system nav bar.
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    )
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
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
                        if (onGoToNote != null) {
                            IconButton(onClick = { onGoToNote(currentPhoto) }) {
                                Icon(Icons.Filled.Notes, contentDescription = "Go to note", tint = Color.White)
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

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.White.copy(alpha = 0.16f),
                )

                Text(
                    text = if (photos.size > 1) {
                        "${pagerState.currentPage + 1} / ${photos.size}  ·  ${currentPhoto.createdAt.toDisplayDateTime()}"
                    } else {
                        currentPhoto.createdAt.toDisplayDateTime()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}
