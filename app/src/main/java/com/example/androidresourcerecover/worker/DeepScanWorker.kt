package com.example.androidresourcerecover.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.androidresourcerecover.database.AppDatabase
import com.example.androidresourcerecover.database.toEntityList
import com.example.androidresourcerecover.scanner.FileScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for performing deep scan in background
 */
class DeepScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "DeepScanWorker"
        const val KEY_FILES_FOUND = "files_found"
        const val KEY_PROGRESS = "progress"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting deep scan...")

            val scanner = FileScanner(applicationContext)
            val database = AppDatabase.getDatabase(applicationContext)
            val fileDao = database.fileDao()

            var totalFilesFound = 0

            // Perform scan with progress updates
            val files = scanner.scanForFiles { progress ->
                // Update progress
                setProgressAsync(workDataOf(KEY_PROGRESS to progress))
            }

            Log.d(TAG, "Scan completed. Found ${files.size} files")

            // Save to database
            if (files.isNotEmpty()) {
                val entities = files.toEntityList()
                fileDao.insertFiles(entities)
                totalFilesFound = files.size
            }

            // Also scan external SD card
            val sdCardFiles = scanner.scanExternalSdCard()
            if (sdCardFiles.isNotEmpty()) {
                val sdCardEntities = sdCardFiles.toEntityList()
                fileDao.insertFiles(sdCardEntities)
                totalFilesFound += sdCardFiles.size
            }

            Log.d(TAG, "Deep scan completed successfully. Total files: $totalFilesFound")

            // Return success with result data
            Result.success(workDataOf(KEY_FILES_FOUND to totalFilesFound))

        } catch (e: Exception) {
            Log.e(TAG, "Deep scan failed", e)
            Result.failure()
        }
    }
}
