package com.example.androidresourcerecover.database

import com.example.androidresourcerecover.data.FileType
import com.example.androidresourcerecover.data.RecoveredFile
import java.io.File

/**
 * Convert RecoveredFile to ScannedFileEntity
 */
fun RecoveredFile.toEntity(): ScannedFileEntity {
    return ScannedFileEntity(
        path = this.path,
        name = this.name,
        size = this.size,
        lastModified = this.lastModified,
        fileType = this.type.name
    )
}

/**
 * Convert ScannedFileEntity to RecoveredFile
 */
fun ScannedFileEntity.toRecoveredFile(): RecoveredFile? {
    val file = File(this.path)
    if (!file.exists()) return null

    val fileType = try {
        FileType.valueOf(this.fileType)
    } catch (e: IllegalArgumentException) {
        FileType.UNKNOWN
    }

    return RecoveredFile(
        file = file,
        name = this.name,
        path = this.path,
        size = this.size,
        lastModified = this.lastModified,
        type = fileType
    )
}

/**
 * Convert list of RecoveredFile to list of ScannedFileEntity
 */
fun List<RecoveredFile>.toEntityList(): List<ScannedFileEntity> {
    return this.map { it.toEntity() }
}

/**
 * Convert list of ScannedFileEntity to list of RecoveredFile (filtering out non-existent files)
 */
fun List<ScannedFileEntity>.toRecoveredFileList(): List<RecoveredFile> {
    return this.mapNotNull { it.toRecoveredFile() }
}
