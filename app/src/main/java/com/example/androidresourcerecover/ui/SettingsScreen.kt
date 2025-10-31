package com.example.androidresourcerecover.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidresourcerecover.R

/**
 * Settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    deepScanEnabled: Boolean,
    onDeepScanToggle: (Boolean) -> Unit,
    autoDeleteDays: Int,
    onAutoDeleteDaysChange: (Int) -> Unit,
    recoveryPath: String,
    onBack: () -> Unit
) {
    var showAutoDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scan Settings Section
            SettingsSection(title = stringResource(R.string.scan_settings)) {
                SwitchSettingItem(
                    title = stringResource(R.string.enable_deep_scan),
                    subtitle = stringResource(R.string.deep_scan_description),
                    checked = deepScanEnabled,
                    onCheckedChange = onDeepScanToggle
                )
            }

            Divider()

            // Recovery Settings Section
            SettingsSection(title = stringResource(R.string.recovery_settings)) {
                SettingItem(
                    title = stringResource(R.string.recovery_folder),
                    subtitle = recoveryPath
                )
            }

            Divider()

            // Recycle Bin Settings Section
            SettingsSection(title = stringResource(R.string.recycle_bin_settings)) {
                ClickableSettingItem(
                    title = stringResource(R.string.auto_delete_recycle_bin),
                    subtitle = getAutoDeleteText(autoDeleteDays),
                    onClick = { showAutoDeleteDialog = true }
                )
            }

            Divider()

            // About Section
            SettingsSection(title = stringResource(R.string.about)) {
                SettingItem(
                    title = stringResource(R.string.app_version),
                    subtitle = stringResource(R.string.version_name)
                )
            }
        }
    }

    // Auto delete dialog
    if (showAutoDeleteDialog) {
        AutoDeleteDialog(
            currentDays = autoDeleteDays,
            onDismiss = { showAutoDeleteDialog = false },
            onConfirm = { days ->
                onAutoDeleteDaysChange(days)
                showAutoDeleteDialog = false
            }
        )
    }
}

/**
 * Settings section with title
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

/**
 * Setting item with title and subtitle
 */
@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = modifier
    )
}

/**
 * Clickable setting item
 */
@Composable
fun ClickableSettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = modifier.clickable(onClick = onClick)
    )
}

/**
 * Switch setting item
 */
@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle != null) {
            { Text(subtitle) }
        } else null,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = modifier.clickable { onCheckedChange(!checked) }
    )
}

/**
 * Auto delete confirmation dialog
 */
@Composable
fun AutoDeleteDialog(
    currentDays: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedDays by remember { mutableStateOf(currentDays) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.auto_delete_recycle_bin)) },
        text = {
            Column {
                RadioOption(
                    text = stringResource(R.string.never),
                    selected = selectedDays == 0,
                    onClick = { selectedDays = 0 }
                )
                RadioOption(
                    text = stringResource(R.string.after_7_days),
                    selected = selectedDays == 7,
                    onClick = { selectedDays = 7 }
                )
                RadioOption(
                    text = stringResource(R.string.after_30_days),
                    selected = selectedDays == 30,
                    onClick = { selectedDays = 30 }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedDays) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Radio option in dialog
 */
@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
        )
    }
}

/**
 * Get auto delete text based on days
 */
fun getAutoDeleteText(days: Int): String {
    return when (days) {
        0 -> "从不"
        7 -> "7天后"
        30 -> "30天后"
        else -> "$days 天后"
    }
}
