package com.example.androidresourcerecover.scanner

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.androidresourcerecover.data.RecoveredFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * FileScanner is responsible for scanning device storage for recoverable files
 */
class FileScanner(private val context: Context) {

    companion object {
        private const val TAG = "FileScanner"

        // Common directories to scan for deleted/cached files
        private val SCAN_DIRECTORIES = listOf(
            ".thumbnails",
            "DCIM/.thumbnails",
            "Pictures/.thumbnails",
            "Android/data",
            "Android/media",
            ".cache",
            "temp",
            "tmp"
        )
    }

    /**
     * Scan device storage for recoverable media files
     * Returns list of RecoveredFile objects
     */
    suspend fun scanForFiles(onProgress: (Int) -> Unit = {}): List<RecoveredFile> = withContext(Dispatchers.IO) {
        val foundFiles = mutableListOf<RecoveredFile>()
        var processedDirs = 0

        try {
            // Get external storage directory
            val externalStorage = Environment.getExternalStorageDirectory()

            if (externalStorage == null || !externalStorage.exists()) {
                Log.e(TAG, "External storage not available")
                return@withContext emptyList()
            }

            Log.d(TAG, "Starting scan from: ${externalStorage.absolutePath}")

            // Scan each predefined directory
            SCAN_DIRECTORIES.forEach { dirPath ->
                val directory = File(externalStorage, dirPath)

                if (directory.exists() && directory.isDirectory) {
                    Log.d(TAG, "Scanning directory: ${directory.absolutePath}")
                    scanDirectory(directory, foundFiles)
                }

                processedDirs++
                onProgress((processedDirs * 100) / SCAN_DIRECTORIES.size)
            }

            // Also scan root level for common media directories
            scanMediaDirectories(externalStorage, foundFiles)

            Log.d(TAG, "Scan completed. Found ${foundFiles.size} files")

        } catch (e: Exception) {
            Log.e(TAG, "Error during scan", e)
        }

        foundFiles.toList()
    }

    /**
     * Recursively scan a directory for supported media files
     */
    private fun scanDirectory(directory: File, foundFiles: MutableList<RecoveredFile>) {
        try {
            directory.listFiles()?.forEach { file ->
                try {
                    when {
                        file.isDirectory -> {
                            // Recursively scan subdirectories (with depth limit)
                            if (shouldScanSubdirectory(file)) {
                                scanDirectory(file, foundFiles)
                            }
                        }
                        file.isFile && RecoveredFile.isSupportedFile(file) -> {
                            val fileType = RecoveredFile.getFileType(file)
                            val recoveredFile = RecoveredFile(
                                file = file,
                                type = fileType
                            )
                            foundFiles.add(recoveredFile)
                            Log.d(TAG, "Found file: ${file.name} (${recoveredFile.getFormattedSize()})")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing file: ${file.absolutePath}", e)
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied for directory: ${directory.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning directory: ${directory.absolutePath}", e)
        }
    }

    /**
     * Scan common media directories at root level
     */
    private fun scanMediaDirectories(root: File, foundFiles: MutableList<RecoveredFile>) {
        val mediaDirectories = listOf("DCIM", "Pictures", "Movies", "Download", "Downloads")

        mediaDirectories.forEach { dirName ->
            val directory = File(root, dirName)
            if (directory.exists() && directory.isDirectory) {
                scanDirectory(directory, foundFiles)
            }
        }
    }

    /**
     * Check if we should scan a subdirectory
     * Prevents scanning system directories and limits depth
     */
    private fun shouldScanSubdirectory(directory: File): Boolean {
        val path = directory.absolutePath.lowercase()

        // Skip system directories
        val skipDirs = listOf(
            "/system",
            "/proc",
            "/dev",
            "/sys"
        )

        return skipDirs.none { path.startsWith(it) }
    }

    /**
     * Scan external SD card if available
     */
    suspend fun scanExternalSdCard(): List<RecoveredFile> = withContext(Dispatchers.IO) {
        val foundFiles = mutableListOf<RecoveredFile>()

        try {
            // Get external storage volumes
            val externalDirs = context.getExternalFilesDirs(null)

            externalDirs.forEach { dir ->
                if (dir != null && dir.absolutePath.contains("emulated").not()) {
                    // This is likely an SD card
                    Log.d(TAG, "Scanning SD card: ${dir.absolutePath}")
                    val sdRoot = dir.parentFile?.parentFile?.parentFile?.parentFile
                    if (sdRoot != null && sdRoot.exists()) {
                        scanDirectory(sdRoot, foundFiles)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning SD card", e)
        }

        foundFiles.toList()
    }
}
