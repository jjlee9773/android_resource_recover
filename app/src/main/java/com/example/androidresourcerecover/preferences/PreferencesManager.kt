package com.example.androidresourcerecover.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.androidresourcerecover.data.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Manager for app preferences using DataStore
 */
class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        private val RECYCLE_BIN_AUTO_DELETE_DAYS = intPreferencesKey("recycle_bin_auto_delete_days")
        private val DEEP_SCAN_ENABLED = booleanPreferencesKey("deep_scan_enabled")
        private val SORT_OPTION = stringPreferencesKey("sort_option")
        private val RECOVERY_FOLDER_PATH = stringPreferencesKey("recovery_folder_path")

        // Default values
        const val DEFAULT_AUTO_DELETE_DAYS = 7
        const val DEFAULT_RECOVERY_FOLDER = "DCIM/Recovered"
    }

    /**
     * Flow of recycle bin auto-delete setting (in days)
     * 0 = never auto delete
     * 7, 30 = auto delete after N days
     */
    val recycleBinAutoDeleteDays: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[RECYCLE_BIN_AUTO_DELETE_DAYS] ?: DEFAULT_AUTO_DELETE_DAYS
        }

    /**
     * Save recycle bin auto-delete setting
     */
    suspend fun setRecycleBinAutoDeleteDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[RECYCLE_BIN_AUTO_DELETE_DAYS] = days
        }
    }

    /**
     * Flow of deep scan enabled setting
     */
    val deepScanEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEEP_SCAN_ENABLED] ?: false
        }

    /**
     * Save deep scan enabled setting
     */
    suspend fun setDeepScanEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEEP_SCAN_ENABLED] = enabled
        }
    }

    /**
     * Flow of sort option setting
     */
    val sortOption: Flow<SortOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortString = preferences[SORT_OPTION] ?: SortOption.DATE_DESC.name
            try {
                SortOption.valueOf(sortString)
            } catch (e: IllegalArgumentException) {
                SortOption.DATE_DESC
            }
        }

    /**
     * Save sort option setting
     */
    suspend fun setSortOption(option: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[SORT_OPTION] = option.name
        }
    }

    /**
     * Flow of recovery folder path setting
     */
    val recoveryFolderPath: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[RECOVERY_FOLDER_PATH] ?: DEFAULT_RECOVERY_FOLDER
        }

    /**
     * Save recovery folder path setting
     */
    suspend fun setRecoveryFolderPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[RECOVERY_FOLDER_PATH] = path
        }
    }

    /**
     * Clear all preferences
     */
    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
