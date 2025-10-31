package com.example.androidresourcerecover.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidresourcerecover.data.FileFilter
import com.example.androidresourcerecover.data.FileRepository
import com.example.androidresourcerecover.data.RecoveredFile
import com.example.androidresourcerecover.scanner.FileScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the main screen state
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val fileScanner = FileScanner(application)
    private val fileRepository = FileRepository(application)

    // UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // All scanned files
    private var allFiles = listOf<RecoveredFile>()

    /**
     * Start scanning for files
     */
    fun startScan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                scanProgress = 0
            )

            try {
                allFiles = fileScanner.scanForFiles { progress ->
                    _uiState.value = _uiState.value.copy(scanProgress = progress)
                }

                updateFilteredFiles()

                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    scanProgress = 100
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    errorMessage = "扫描失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Change the active filter tab
     */
    fun setFilter(filter: FileFilter) {
        _uiState.value = _uiState.value.copy(currentFilter = filter)
        updateFilteredFiles()
    }

    /**
     * Update filtered files based on current filter
     */
    private fun updateFilteredFiles() {
        val filtered = when (_uiState.value.currentFilter) {
            FileFilter.ALL -> allFiles
            FileFilter.IMAGES_ONLY -> allFiles.filter { it.isImage() }
            FileFilter.VIDEOS_ONLY -> allFiles.filter { it.isVideo() }
        }
        _uiState.value = _uiState.value.copy(files = filtered)
    }

    /**
     * Toggle file selection
     */
    fun toggleFileSelection(file: RecoveredFile) {
        val currentSelection = _uiState.value.selectedFiles.toMutableSet()
        if (currentSelection.contains(file)) {
            currentSelection.remove(file)
        } else {
            currentSelection.add(file)
        }
        _uiState.value = _uiState.value.copy(selectedFiles = currentSelection)
    }

    /**
     * Select all files
     */
    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            selectedFiles = _uiState.value.files.toSet()
        )
    }

    /**
     * Clear selection
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedFiles = emptySet())
    }

    /**
     * Recover a single file
     */
    fun recoverFile(file: RecoveredFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecovering = true)

            try {
                val recoveredPath = fileRepository.recoverFile(file)
                if (recoveredPath != null) {
                    _uiState.value = _uiState.value.copy(
                        isRecovering = false,
                        successMessage = "文件已恢复到: $recoveredPath"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRecovering = false,
                        errorMessage = "恢复失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRecovering = false,
                    errorMessage = "恢复失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Recover selected files
     */
    fun recoverSelectedFiles() {
        val selectedFiles = _uiState.value.selectedFiles.toList()
        if (selectedFiles.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRecovering = true,
                recoveryProgress = 0
            )

            try {
                val results = fileRepository.recoverFiles(selectedFiles) { current, total ->
                    val progress = (current * 100) / total
                    _uiState.value = _uiState.value.copy(recoveryProgress = progress)
                }

                val successCount = results.values.count { it != null }
                val totalCount = results.size

                _uiState.value = _uiState.value.copy(
                    isRecovering = false,
                    recoveryProgress = 100,
                    selectedFiles = emptySet(),
                    successMessage = "已恢复 $successCount/$totalCount 个文件"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRecovering = false,
                    errorMessage = "恢复失败: ${e.message}"
                )
            }
        }
    }

    /**
     * Show file preview
     */
    fun showPreview(file: RecoveredFile?) {
        _uiState.value = _uiState.value.copy(previewFile = file)
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * UI State data class
 */
data class UiState(
    val isScanning: Boolean = false,
    val scanProgress: Int = 0,
    val isRecovering: Boolean = false,
    val recoveryProgress: Int = 0,
    val files: List<RecoveredFile> = emptyList(),
    val currentFilter: FileFilter = FileFilter.ALL,
    val selectedFiles: Set<RecoveredFile> = emptySet(),
    val previewFile: RecoveredFile? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
