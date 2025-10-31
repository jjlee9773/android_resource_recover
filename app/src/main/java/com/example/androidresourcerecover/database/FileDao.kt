package com.example.androidresourcerecover.database

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for scanned files
 */
@Dao
interface FileDao {

    // Insert operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ScannedFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ScannedFileEntity>)

    // Query operations
    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 ORDER BY lastModified DESC")
    fun getAllFilesPaging(): PagingSource<Int, ScannedFileEntity>

    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 ORDER BY lastModified DESC")
    fun getAllFiles(): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 AND fileType = :type ORDER BY lastModified DESC")
    fun getFilesByType(type: String): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE isDeleted = 1 ORDER BY deletedDate DESC")
    fun getDeletedFiles(): Flow<List<ScannedFileEntity>>

    // Search operations
    @Query("""
        SELECT * FROM scanned_files
        WHERE isDeleted = 0
        AND name LIKE '%' || :query || '%'
        ORDER BY lastModified DESC
    """)
    fun searchFiles(query: String): Flow<List<ScannedFileEntity>>

    @Query("""
        SELECT * FROM scanned_files
        WHERE isDeleted = 0
        AND name LIKE '%' || :query || '%'
        AND fileType = :type
        ORDER BY lastModified DESC
    """)
    fun searchFilesByType(query: String, type: String): Flow<List<ScannedFileEntity>>

    // Filter by date range
    @Query("""
        SELECT * FROM scanned_files
        WHERE isDeleted = 0
        AND lastModified BETWEEN :startDate AND :endDate
        ORDER BY lastModified DESC
    """)
    fun getFilesByDateRange(startDate: Long, endDate: Long): Flow<List<ScannedFileEntity>>

    // Filter by size range
    @Query("""
        SELECT * FROM scanned_files
        WHERE isDeleted = 0
        AND size BETWEEN :minSize AND :maxSize
        ORDER BY lastModified DESC
    """)
    fun getFilesBySizeRange(minSize: Long, maxSize: Long): Flow<List<ScannedFileEntity>>

    // Sorting operations
    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 ORDER BY name ASC")
    fun getFilesSortedByName(): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 ORDER BY size DESC")
    fun getFilesSortedBySize(): Flow<List<ScannedFileEntity>>

    @Query("SELECT * FROM scanned_files WHERE isDeleted = 0 ORDER BY lastModified DESC")
    fun getFilesSortedByDate(): Flow<List<ScannedFileEntity>>

    // Soft delete (move to recycle bin)
    @Query("UPDATE scanned_files SET isDeleted = 1, deletedDate = :deletedDate WHERE path = :path")
    suspend fun moveToRecycleBin(path: String, deletedDate: Long = System.currentTimeMillis())

    // Restore from recycle bin
    @Query("UPDATE scanned_files SET isDeleted = 0, deletedDate = NULL WHERE path = :path")
    suspend fun restoreFromRecycleBin(path: String)

    // Permanent delete
    @Delete
    suspend fun deleteFile(file: ScannedFileEntity)

    @Query("DELETE FROM scanned_files WHERE path = :path")
    suspend fun deleteFileByPath(path: String)

    @Query("DELETE FROM scanned_files WHERE isDeleted = 1")
    suspend fun emptyRecycleBin()

    // Clear old recycle bin items
    @Query("DELETE FROM scanned_files WHERE isDeleted = 1 AND deletedDate < :beforeDate")
    suspend fun deleteOldRecycleBinItems(beforeDate: Long)

    // Clear all
    @Query("DELETE FROM scanned_files")
    suspend fun clearAll()

    // Count operations
    @Query("SELECT COUNT(*) FROM scanned_files WHERE isDeleted = 0")
    fun getFileCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scanned_files WHERE isDeleted = 1")
    fun getRecycleBinCount(): Flow<Int>
}

/**
 * Data Access Object for search history
 */
@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
