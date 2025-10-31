package com.example.androidresourcerecover.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidresourcerecover.R
import com.example.androidresourcerecover.data.FileFilter

/**
 * Filter tabs for All/Images/Videos
 */
@Composable
fun FilterTabs(
    currentFilter: FileFilter,
    onFilterChange: (FileFilter) -> Unit
) {
    TabRow(selectedTabIndex = currentFilter.ordinal) {
        Tab(
            selected = currentFilter == FileFilter.ALL,
            onClick = { onFilterChange(FileFilter.ALL) },
            text = { Text(stringResource(R.string.tab_all)) }
        )
        Tab(
            selected = currentFilter == FileFilter.IMAGES_ONLY,
            onClick = { onFilterChange(FileFilter.IMAGES_ONLY) },
            text = { Text(stringResource(R.string.tab_images)) }
        )
        Tab(
            selected = currentFilter == FileFilter.VIDEOS_ONLY,
            onClick = { onFilterChange(FileFilter.VIDEOS_ONLY) },
            text = { Text(stringResource(R.string.tab_videos)) }
        )
    }
}

/**
 * Empty state when no files found
 */
@Composable
fun EmptyState(onScanClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_files_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(stringResource(R.string.scan_button))
        }
    }
}

/**
 * Scanning screen with progress
 */
@Composable
fun ScanningScreen(progress: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.scanning),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$progress%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
