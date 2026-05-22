package com.statusmaker.videoapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "status_maker_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val WATERMARK_REMOVED = booleanPreferencesKey("watermark_removed")
        val VIDEOS_CREATED = intPreferencesKey("videos_created")
        val LAST_AD_SHOWN = longPreferencesKey("last_ad_shown")
        val DEFAULT_MUSIC = stringPreferencesKey("default_music")
        val SHOW_ONBOARDING = booleanPreferencesKey("show_onboarding")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data
        .map { it[IS_PREMIUM] ?: false }

    val isWatermarkRemoved: Flow<Boolean> = context.dataStore.data
        .map { it[WATERMARK_REMOVED] ?: false }

    val videosCreated: Flow<Int> = context.dataStore.data
        .map { it[VIDEOS_CREATED] ?: 0 }

    val showOnboarding: Flow<Boolean> = context.dataStore.data
        .map { it[SHOW_ONBOARDING] ?: true }

    suspend fun setPremium(value: Boolean) {
        context.dataStore.edit { it[IS_PREMIUM] = value }
        if (value) context.dataStore.edit { it[WATERMARK_REMOVED] = true }
    }

    suspend fun setWatermarkRemoved(value: Boolean) {
        context.dataStore.edit { it[WATERMARK_REMOVED] = value }
    }

    suspend fun incrementVideosCreated() {
        context.dataStore.edit {
            it[VIDEOS_CREATED] = (it[VIDEOS_CREATED] ?: 0) + 1
        }
    }

    suspend fun setOnboardingShown() {
        context.dataStore.edit { it[SHOW_ONBOARDING] = false }
    }

    suspend fun recordAdShown() {
        context.dataStore.edit { it[LAST_AD_SHOWN] = System.currentTimeMillis() }
    }
}
