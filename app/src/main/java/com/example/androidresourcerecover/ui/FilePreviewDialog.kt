package com.example.androidresourcerecover.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.androidresourcerecover.R
import com.example.androidresourcerecover.data.RecoveredFile

/**
 * Dialog for previewing files
 */
@Composable
fun FilePreviewDialog(
    file: RecoveredFile?,
    onDismiss: () -> Unit,
    onRecover: () -> Unit
) {
    if (file == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White
                )
            }

            // Preview content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    file.isImage() -> {
                        ImagePreview(file = file)
                    }
                    file.isVideo() -> {
                        VideoPreview(file = file)
                    }
                }
            }

            // File info and action buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.file_size, file.getFormattedSize()),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = stringResource(R.string.file_date, file.getFormattedDate()),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRecover,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.recover))
                }
            }
        }
    }
}

/**
 * Image preview
 */
@Composable
fun ImagePreview(file: RecoveredFile) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(file.file)
            .crossfade(true)
            .build(),
        contentDescription = file.name,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

/**
 * Video preview
 */
@Composable
fun VideoPreview(file: RecoveredFile) {
    val context = LocalContext.current

    // Create ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.fromFile(file.file))
            setMediaItem(mediaItem)
            prepare()
        }
    }

    // Cleanup player when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
