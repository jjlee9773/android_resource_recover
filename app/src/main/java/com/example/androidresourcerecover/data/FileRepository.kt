package com.example.androidresourcerecover.data

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Repository for managing file operations
 */
class FileRepository(private val context: Context) {

    companion object {
        private const val TAG = "FileRepository"
        private const val RECOVERY_FOLDER = "DCIM/Recovered"
    }

    /**
     * Recover a file by copying it to the recovery folder
     * Returns the new file path if successful, null otherwise
     */
    suspend fun recoverFile(recoveredFile: RecoveredFile): String? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = recoveredFile.file

            if (!sourceFile.exists()) {
                Log.e(TAG, "Source file does not exist: ${sourceFile.absolutePath}")
                return@withContext null
            }

            // Create recovery directory
            val recoveryDir = File(
                Environment.getExternalStorageDirectory(),
                RECOVERY_FOLDER
            )

            if (!recoveryDir.exists()) {
                recoveryDir.mkdirs()
            }

            // Generate unique filename if file already exists
            var destinationFile = File(recoveryDir, sourceFile.name)
            var counter = 1
            while (destinationFile.exists()) {
                val nameWithoutExt = sourceFile.nameWithoutExtension
                val extension = sourceFile.extension
                destinationFile = File(recoveryDir, "${nameWithoutExt}_$counter.$extension")
                counter++
            }

            // Copy file
            copyFile(sourceFile, destinationFile)

            Log.d(TAG, "File recovered successfully: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error recovering file: ${recoveredFile.name}", e)
            null
        }
    }

    /**
     * Recover multiple files
     * Returns a map of original file path to recovered file path
     */
    suspend fun recoverFiles(
        files: List<RecoveredFile>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Map<String, String?> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<String, String?>()

        files.forEachIndexed { index, file ->
            val recoveredPath = recoverFile(file)
            results[file.path] = recoveredPath
            onProgress(index + 1, files.size)
        }

        results
    }

    /**
     * Copy file from source to destination
     */
    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Get the recovery folder path
     */
    fun getRecoveryFolderPath(): String {
        return File(Environment.getExternalStorageDirectory(), RECOVERY_FOLDER).absolutePath
    }

    /**
     * Check if recovery folder exists
     */
    fun isRecoveryFolderExists(): Boolean {
        val recoveryDir = File(Environment.getExternalStorageDirectory(), RECOVERY_FOLDER)
        return recoveryDir.exists()
    }

    /**
     * Get size of recovery folder
     */
    suspend fun getRecoveryFolderSize(): Long = withContext(Dispatchers.IO) {
        val recoveryDir = File(Environment.getExternalStorageDirectory(), RECOVERY_FOLDER)
        if (!recoveryDir.exists()) return@withContext 0L

        var size = 0L
        recoveryDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                size += file.length()
            }
        }
        size
    }

    /**
     * Delete a file
     */
    suspend fun deleteFile(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${file.absolutePath}", e)
            false
        }
    }
}
