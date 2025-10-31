package com.example.androidresourcerecover.data

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.androidresourcerecover.database.AppDatabase
import com.example.androidresourcerecover.database.toEntityList
import com.example.androidresourcerecover.database.toRecoveredFileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Calendar

/**
 * Enhanced repository for managing file operations with database and recycle bin support
 */
class EnhancedFileRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val fileDao = database.fileDao()

    companion object {
        private const val TAG = "EnhancedFileRepository"
        private const val RECOVERY_FOLDER = "DCIM/Recovered"
    }

    /**
     * Save scanned files to database
     */
    suspend fun saveScannedFiles(files: List<RecoveredFile>) {
        withContext(Dispatchers.IO) {
            fileDao.insertFiles(files.toEntityList())
        }
    }

    /**
     * Get all files from database
     */
    fun getAllFiles(): Flow<List<RecoveredFile>> {
        return fileDao.getAllFiles().map { it.toRecoveredFileList() }
    }

    /**
     * Get files by type from database
     */
    fun getFilesByType(type: FileType): Flow<List<RecoveredFile>> {
        return fileDao.getFilesByType(type.name).map { it.toRecoveredFileList() }
    }

    /**
     * Search files by query
     */
    fun searchFiles(query: String): Flow<List<RecoveredFile>> {
        return fileDao.searchFiles(query).map { it.toRecoveredFileList() }
    }

    /**
     * Search files by query and type
     */
    fun searchFilesByType(query: String, type: FileType): Flow<List<RecoveredFile>> {
        return fileDao.searchFilesByType(query, type.name).map { it.toRecoveredFileList() }
    }

    /**
     * Get files by date range
     */
    fun getFilesByDateFilter(dateFilter: DateFilter): Flow<List<RecoveredFile>> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        val startDate = when (dateFilter) {
            DateFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            DateFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }

        return fileDao.getFilesByDateRange(startDate, endDate).map { it.toRecoveredFileList() }
    }

    /**
     * Get files by size filter
     */
    fun getFilesBySizeFilter(sizeFilter: SizeFilter): Flow<List<RecoveredFile>> {
        val (minSize, maxSize) = when (sizeFilter) {
            SizeFilter.SMALL -> Pair(0L, 1024 * 1024L) // < 1MB
            SizeFilter.MEDIUM -> Pair(1024 * 1024L, 10 * 1024 * 1024L) // 1MB - 10MB
            SizeFilter.LARGE -> Pair(10 * 1024 * 1024L, Long.MAX_VALUE) // > 10MB
            else -> Pair(0L, Long.MAX_VALUE)
        }

        return fileDao.getFilesBySizeRange(minSize, maxSize).map { it.toRecoveredFileList() }
    }

    /**
     * Get files sorted by option
     */
    fun getFilesSorted(sortOption: SortOption): Flow<List<RecoveredFile>> {
        return when (sortOption) {
            SortOption.DATE_DESC, SortOption.DATE_ASC ->
                fileDao.getFilesSortedByDate().map {
                    val list = it.toRecoveredFileList()
                    if (sortOption == SortOption.DATE_ASC) list.reversed() else list
                }
            SortOption.NAME_ASC ->
                fileDao.getFilesSortedByName().map { it.toRecoveredFileList() }
            SortOption.NAME_DESC ->
                fileDao.getFilesSortedByName().map { it.toRecoveredFileList().reversed() }
            SortOption.SIZE_DESC ->
                fileDao.getFilesSortedBySize().map { it.toRecoveredFileList() }
            SortOption.SIZE_ASC ->
                fileDao.getFilesSortedBySize().map { it.toRecoveredFileList().reversed() }
        }
    }

    /**
     * Move file to recycle bin (soft delete)
     */
    suspend fun moveToRecycleBin(file: RecoveredFile) {
        withContext(Dispatchers.IO) {
            fileDao.moveToRecycleBin(file.path)
        }
    }

    /**
     * Restore file from recycle bin
     */
    suspend fun restoreFromRecycleBin(file: RecoveredFile) {
        withContext(Dispatchers.IO) {
            fileDao.restoreFromRecycleBin(file.path)
        }
    }

    /**
     * Delete file permanently
     */
    suspend fun deleteFilePermanently(file: RecoveredFile): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                fileDao.deleteFileByPath(file.path)
                file.file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file permanently", e)
                false
            }
        }
    }

    /**
     * Empty recycle bin
     */
    suspend fun emptyRecycleBin() {
        withContext(Dispatchers.IO) {
            fileDao.emptyRecycleBin()
        }
    }

    /**
     * Delete old recycle bin items based on days
     */
    suspend fun deleteOldRecycleBinItems(days: Int) {
        if (days <= 0) return

        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val beforeDate = calendar.timeInMillis
            fileDao.deleteOldRecycleBinItems(beforeDate)
        }
    }

    /**
     * Get deleted files (recycle bin)
     */
    fun getRecycleBinFiles(): Flow<List<RecoveredFile>> {
        return fileDao.getDeletedFiles().map { it.toRecoveredFileList() }
    }

    /**
     * Get recycle bin count
     */
    fun getRecycleBinCount(): Flow<Int> {
        return fileDao.getRecycleBinCount()
    }

    /**
     * Recover a file by copying it to the recovery folder
     */
    suspend fun recoverFile(recoveredFile: RecoveredFile, recoveryPath: String = RECOVERY_FOLDER): String? {
        return withContext(Dispatchers.IO) {
            try {
                val sourceFile = recoveredFile.file

                if (!sourceFile.exists()) {
                    Log.e(TAG, "Source file does not exist: ${sourceFile.absolutePath}")
                    return@withContext null
                }

                val recoveryDir = File(Environment.getExternalStorageDirectory(), recoveryPath)
                if (!recoveryDir.exists()) {
                    recoveryDir.mkdirs()
                }

                var destinationFile = File(recoveryDir, sourceFile.name)
                var counter = 1
                while (destinationFile.exists()) {
                    val nameWithoutExt = sourceFile.nameWithoutExtension
                    val extension = sourceFile.extension
                    destinationFile = File(recoveryDir, "${nameWithoutExt}_$counter.$extension")
                    counter++
                }

                copyFile(sourceFile, destinationFile)
                Log.d(TAG, "File recovered successfully: ${destinationFile.absolutePath}")
                destinationFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Error recovering file: ${recoveredFile.name}", e)
                null
            }
        }
    }

    /**
     * Recover multiple files
     */
    suspend fun recoverFiles(
        files: List<RecoveredFile>,
        recoveryPath: String = RECOVERY_FOLDER,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Map<String, String?> {
        return withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, String?>()
            files.forEachIndexed { index, file ->
                val recoveredPath = recoverFile(file, recoveryPath)
                results[file.path] = recoveredPath
                onProgress(index + 1, files.size)
            }
            results
        }
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
     * Clear all cached data
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            fileDao.clearAll()
        }
    }
}
