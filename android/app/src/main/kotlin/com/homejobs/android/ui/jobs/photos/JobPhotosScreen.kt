package com.homejobs.android.ui.jobs.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.homejobs.android.domain.model.Photo
import com.homejobs.android.ui.common.EmptyState
import com.homejobs.android.ui.common.PhotoViewerDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPhotosScreen(
    onBack: () -> Unit,
    viewModel: JobPhotosViewModel = hiltViewModel(),
) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    var viewingPhoto by remember { mutableStateOf<Photo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (photos.isEmpty()) {
            EmptyState("No photos yet.", modifier = Modifier.padding(padding))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(2.dp),
            ) {
                items(photos, key = { it.id }) { photo ->
                    AsyncImage(
                        model = File(photo.filePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clickable { viewingPhoto = photo },
                    )
                }
            }
        }
    }

    viewingPhoto?.let { photo ->
        PhotoViewerDialog(photo = photo, onDismiss = { viewingPhoto = null })
    }
}
