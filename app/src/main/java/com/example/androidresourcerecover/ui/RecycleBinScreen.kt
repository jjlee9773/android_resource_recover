package com.example.androidresourcerecover.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidresourcerecover.R
import com.example.androidresourcerecover.data.RecoveredFile

/**
 * Recycle Bin screen showing deleted files
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    files: List<RecoveredFile>,
    selectedFiles: Set<RecoveredFile>,
    onFileClick: (RecoveredFile) -> Unit,
    onFileLongClick: (RecoveredFile) -> Unit,
    onRestore: (RecoveredFile) -> Unit,
    onRestoreSelected: () -> Unit,
    onDeletePermanently: (RecoveredFile) -> Unit,
    onDeleteSelectedPermanently: () -> Unit,
    onEmptyRecycleBin: () -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBack: () -> Unit
) {
    var showEmptyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recycle_bin)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (files.isNotEmpty()) {
                        IconButton(onClick = { showEmptyDialog = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Empty Recycle Bin")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (selectedFiles.isNotEmpty()) {
                RecycleBinBottomBar(
                    selectedCount = selectedFiles.size,
                    onRestore = onRestoreSelected,
                    onDelete = onDeleteSelectedPermanently,
                    onCancel = onClearSelection
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                files.isEmpty() -> {
                    EmptyRecycleBin()
                }
                else -> {
                    Column {
                        // Action bar
                        if (selectedFiles.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.items_in_recycle_bin, files.size),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                TextButton(onClick = onSelectAll) {
                                    Text(stringResource(R.string.select_all))
                                }
                            }
                        }

                        // File grid
                        FileGrid(
                            files = files,
                            selectedFiles = selectedFiles,
                            onFileClick = onFileClick,
                            onFileLongClick = onFileLongClick
                        )
                    }
                }
            }
        }
    }

    // Empty recycle bin confirmation dialog
    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text(stringResource(R.string.empty_recycle_bin)) },
            text = { Text(stringResource(R.string.empty_recycle_bin_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEmptyRecycleBin()
                        showEmptyDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Empty recycle bin state
 */
@Composable
fun EmptyRecycleBin() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.recycle_bin_empty),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Bottom bar for recycle bin actions
 */
@Composable
fun RecycleBinBottomBar(
    selectedCount: Int,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel_selection))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.restore))
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}
