package com.statusmaker.videoapp.utils

import android.content.Context
import com.statusmaker.videoapp.data.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Fast synchronous read of the user's chosen content language, backed by
 * PreferenceManager's DataStore. Template names, category labels and the
 * fixed strings FrameRenderer draws onto video frames are plain Kotlin data
 * (not Android resource strings), so they can't ride the system per-app
 * locale mechanism (AppCompatDelegate) — every RecyclerView adapter and
 * Canvas draw call reads [current] directly instead of threading a Flow or
 * LiveData through every layer.
 *
 * [current] is populated once at process start (see StatusMakerApp) and
 * updated immediately whenever the user picks a language, so callers never
 * need to suspend for it.
 */
object AppLanguageStore {

    @Volatile
    var current: AppLanguage = AppLanguage.DEFAULT
        private set

    /** Call once from Application.onCreate — restores the persisted choice ASAP. */
    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            current = PreferenceManager(context).appLanguage.first()
        }
    }

    /**
     * Updates the in-memory value immediately (synchronous — safe to call
     * right before triggering an Activity recreate) and persists it in the
     * background.
     */
    fun select(context: Context, lang: AppLanguage) {
        current = lang
        CoroutineScope(Dispatchers.IO).launch {
            PreferenceManager(context.applicationContext).setAppLanguage(lang)
        }
    }
}
