package com.example.contacts42.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "DatastoreManager"

enum class SortOrder { BY_NAME, BY_NAME_REVERSE }

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class FilterPreferences(val sortOrder: SortOrder, val showOnlyFavorites: Boolean)

@Singleton
class DatastoreManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    val contactsDisplayPreferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences: ", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[DatastoreKeys.SORT_ORDER] ?: SortOrder.BY_NAME.name
            )
            val showOnlyFavorites = preferences[DatastoreKeys.SHOW_ONLY_FAVORITES] ?: false
            FilterPreferences(sortOrder, showOnlyFavorites)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[DatastoreKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateShowOnlyFavorites(showOnlyFavorites: Boolean) {
        dataStore.edit { preferences ->
            preferences[DatastoreKeys.SHOW_ONLY_FAVORITES] = showOnlyFavorites
        }
    }

    fun getTimeWhenPaused(): Flow<Long> {
        return dataStore.data
            .map { preferences ->
                preferences[DatastoreKeys.TIME_WHEN_PAUSED] ?: 0
            }
    }

    suspend fun updateTimeWhenPaused(timeWhenPaused: Long) {
        dataStore.edit { preferences ->
            preferences[DatastoreKeys.TIME_WHEN_PAUSED] = timeWhenPaused
        }
    }

    private object DatastoreKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOW_ONLY_FAVORITES = booleanPreferencesKey("show_only_favorites")
        val TIME_WHEN_PAUSED = longPreferencesKey("time_when_paused")
    }
}