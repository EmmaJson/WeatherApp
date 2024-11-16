package com.emmajson.weatherapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DataStoreManager(context: Context) {
    val Context.dataStore by preferencesDataStore(name = "settings")

    private val dataStore = context.dataStore

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    fun getUsername(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }
    }

    companion object {
        private val USERNAME_KEY = stringPreferencesKey("username")
    }
}

