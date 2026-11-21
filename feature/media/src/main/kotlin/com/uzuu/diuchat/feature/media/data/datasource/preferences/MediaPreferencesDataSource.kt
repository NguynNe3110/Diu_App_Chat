package com.uzuu.diuchat.feature.media.data.datasource.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val autoDownloadPolicyFlow: Flow<AutoDownloadPolicy> = dataStore.data.map { preferences ->
        val raw = preferences[KEY_AUTO_DOWNLOAD_POLICY] ?: AutoDownloadPolicy.WIFI_ONLY.name
        runCatching { AutoDownloadPolicy.valueOf(raw) }.getOrDefault(AutoDownloadPolicy.WIFI_ONLY)
    }

    suspend fun updateAutoDownloadPolicy(policy: AutoDownloadPolicy) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_DOWNLOAD_POLICY] = policy.name
        }
    }

    companion object {
        val KEY_AUTO_DOWNLOAD_POLICY = stringPreferencesKey("diu_media_auto_download_policy")
    }
}
