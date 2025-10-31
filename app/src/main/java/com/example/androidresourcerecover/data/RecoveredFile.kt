package com.example.androidresourcerecover.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a recoverable file
 */
data class RecoveredFile(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val size: Long = file.length(),
    val lastModified: Long = file.lastModified(),
    val type: FileType
) {
    /**
     * Get formatted file size (KB, MB, GB)
     */
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    /**
     * Get formatted date string
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(lastModified))
    }

    /**
     * Check if this is an image file
     */
    fun isImage(): Boolean = type == FileType.IMAGE

    /**
     * Check if this is a video file
     */
    fun isVideo(): Boolean = type == FileType.VIDEO

    companion object {
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mov", "mkv", "3gp")

        /**
         * Determine file type from extension
         */
        fun getFileType(file: File): FileType {
            val extension = file.extension.lowercase()
            return when {
                extension in IMAGE_EXTENSIONS -> FileType.IMAGE
                extension in VIDEO_EXTENSIONS -> FileType.VIDEO
                else -> FileType.UNKNOWN
            }
        }

        /**
         * Check if file is a supported media file
         */
        fun isSupportedFile(file: File): Boolean {
            val extension = file.extension.lowercase()
            return extension in IMAGE_EXTENSIONS || extension in VIDEO_EXTENSIONS
        }
    }
}

/**
 * Enum representing file types
 */
enum class FileType {
    IMAGE,
    VIDEO,
    UNKNOWN
}

/**
 * Enum representing filter tabs
 */
enum class FileFilter {
    ALL,
    IMAGES_ONLY,
    VIDEOS_ONLY
}
