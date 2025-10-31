package com.example.androidresourcerecover.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for caching scanned files
 */
@Entity(tableName = "scanned_files")
data class ScannedFileEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val fileType: String, // "IMAGE" or "VIDEO"
    val scanDate: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false, // For soft delete (RecycleBin)
    val deletedDate: Long? = null
)

/**
 * Database entity for search history
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
