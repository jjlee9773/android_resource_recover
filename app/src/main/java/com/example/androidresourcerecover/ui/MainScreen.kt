package com.example.androidresourcerecover.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidresourcerecover.R
import com.example.androidresourcerecover.data.FileFilter
import com.example.androidresourcerecover.viewmodel.MainViewModel
import com.google.accompanist.permissions.*

/**
 * Main screen of the app
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Request appropriate permissions based on Android version
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions)

    // Show snackbar messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (uiState.selectedFiles.isNotEmpty()) {
                BottomActionBar(
                    selectedCount = uiState.selectedFiles.size,
                    onRecover = { viewModel.recoverSelectedFiles() },
                    onCancel = { viewModel.clearSelection() }
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
                !permissionsState.allPermissionsGranted -> {
                    PermissionRequestScreen(
                        permissionsState = permissionsState,
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                else -> {
                    MainContent(
                        uiState = uiState,
                        onScanClick = { viewModel.startScan() },
                        onFilterChange = { viewModel.setFilter(it) },
                        onFileClick = { file ->
                            if (uiState.selectedFiles.isNotEmpty()) {
                                viewModel.toggleFileSelection(file)
                            } else {
                                viewModel.showPreview(file)
                            }
                        },
                        onFileLongClick = { viewModel.toggleFileSelection(it) },
                        onSelectAll = { viewModel.selectAll() }
                    )
                }
            }

            // Show preview dialog
            if (uiState.previewFile != null) {
                FilePreviewDialog(
                    file = uiState.previewFile,
                    onDismiss = { viewModel.showPreview(null) },
                    onRecover = {
                        viewModel.recoverFile(uiState.previewFile!!)
                        viewModel.showPreview(null)
                    }
                )
            }

            // Show loading overlay when recovering
            if (uiState.isRecovering) {
                LoadingOverlay(
                    progress = uiState.recoveryProgress,
                    text = stringResource(R.string.recovering)
                )
            }
        }
    }
}

/**
 * Permission request screen
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionsState: MultiplePermissionsState,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.permission_required),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.permission_rationale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (permissionsState.shouldShowRationale) {
                    permissionsState.launchMultiplePermissionRequest()
                } else {
                    onOpenSettings()
                }
            }
        ) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

/**
 * Main content with file list
 */
@Composable
fun MainContent(
    uiState: com.example.androidresourcerecover.viewmodel.UiState,
    onScanClick: () -> Unit,
    onFilterChange: (FileFilter) -> Unit,
    onFileClick: (com.example.androidresourcerecover.data.RecoveredFile) -> Unit,
    onFileLongClick: (com.example.androidresourcerecover.data.RecoveredFile) -> Unit,
    onSelectAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter tabs
        FilterTabs(
            currentFilter = uiState.currentFilter,
            onFilterChange = onFilterChange
        )

        // Action bar
        if (uiState.files.isNotEmpty() && uiState.selectedFiles.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.files_found, uiState.files.size),
                    style = MaterialTheme.typography.bodyMedium
                )

                TextButton(onClick = onSelectAll) {
                    Text(stringResource(R.string.select_all))
                }
            }
        }

        // File grid or empty state
        when {
            uiState.isScanning -> {
                ScanningScreen(progress = uiState.scanProgress)
            }
            uiState.files.isEmpty() && !uiState.isScanning -> {
                EmptyState(onScanClick = onScanClick)
            }
            else -> {
                FileGrid(
                    files = uiState.files,
                    selectedFiles = uiState.selectedFiles,
                    onFileClick = onFileClick,
                    onFileLongClick = onFileLongClick
                )
            }
        }
    }
}

/**
 * Bottom action bar for batch operations
 */
@Composable
fun BottomActionBar(
    selectedCount: Int,
    onRecover: () -> Unit,
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

            Button(onClick = onRecover) {
                Text(stringResource(R.string.recover_selected, selectedCount))
            }
        }
    }
}

/**
 * Loading overlay
 */
@Composable
fun LoadingOverlay(
    progress: Int,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .padding(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = text)
                if (progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
